package com.resumeparser.parser_service.service;

import java.io.IOException;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.resumeparser.parser_service.dto.ExperienceDTO;
import com.resumeparser.parser_service.dto.ParsedResumeDTO;

@Service
public class GeminiLlmService {
	@Value("${gemini.api.key}")
	private String apiKey;
	
	@Async
	public CompletableFuture<ParsedResumeDTO> parseResumeWithLlm(MultipartFile file) {
		
		Tika tika=new Tika();
		String rawText;
		try {
			rawText = tika.parseToString(file.getInputStream());
		
		
		String url="https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key="+apiKey;
		
		String prompt = "You are an expert resume parser. Extract the candidate's details from the text below. " +
			    "Return ONLY a valid JSON object strictly matching this exact structure (fill in the values or leave null/empty if not found):\n" +
			    "{\n" +
			    "  \"summary\": \"Write a concise 2-sentence professional summary highlighting the candidate's core strengths, years of experience, and primary technologies.\",\n" +
			    "  \"name\": \"\",\n" +
			    "  \"email\": \"\",\n" +
			    "  \"phnNo\": \"\",\n" +
			    "  \"skills\": [\"\"],\n" +
			    "  \"certifications\": [\"\"],\n" +
			    "  \"education\": [{ \"degree\": \"\", \"institution\": \"\", \"passingYear\": \"\", \"percentage\": \"\", \"branch\": \"\" }],\n" +
			    "  \"experience\": [{ \"company\": \"\", \"role\": \"\", \"startDate\": \"YYYY-MM\", \\\"endDate\\\": \\\"YYYY-MM\\\",, \"isCurrentJob\": false }],\n" +
			    "  \"projects\": [{ \"title\": \"\", \"description\": \"\", \"technologies\": \"\" }]\n" +
			    "}\n\n" +
			    "Do not include markdown formatting or explanations. Raw Text:\n\n" + rawText;
		Map<String, Object> requestBody = Map.of(
				"contents",List.of(
						Map.of("parts",List.of(
								Map.of("text",prompt)
								))
						),
				"generationConfig",Map.of(
					"responseMimeType","application/json"
					)
				);
		
		HttpHeaders headers=new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		
		HttpEntity<Map<String,Object>> requestEntity =new HttpEntity<>(requestBody,headers);
		
		RestTemplate restTemplate = new RestTemplate();
		
		
		String response = restTemplate.postForObject(url, requestEntity, String.class);
		
		

			ObjectMapper mapper=new ObjectMapper();
			
			JsonNode rootNode = mapper.readTree(response);
			
			String extractedJson = rootNode
					.path("candidates").get(0)
					.path("content")
					.path("parts").get(0)
					.path("text").asText();
			
//			System.out.println(extractedJson);
			
			ParsedResumeDTO parsedResume= mapper.readValue(extractedJson, ParsedResumeDTO.class);
			
			double realTotalExp = calculateTotalExperience(parsedResume.getExperience());
			parsedResume.setTotalExperience(realTotalExp);
			
			return CompletableFuture.completedFuture(parsedResume);
			
		}catch(Exception e) {
			throw new RuntimeException("Failed to parse LLM response",e);
		}
		
		
	}
	private double calculateTotalExperience(List<ExperienceDTO> experiences) {
	    if (experiences == null || experiences.isEmpty()) return 0.0;
	    double totalMonths = 0;
	    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
	    for (ExperienceDTO exp : experiences) {
	        try {
	            YearMonth start = YearMonth.parse(exp.getStartDate(), formatter);
	            YearMonth end;
	            
	            if (exp.isCurrentJob() || "PRESENT".equalsIgnoreCase(exp.getEndDate())) {
	                end = YearMonth.now(); // If they still work there, use today's date
	            } else {
	                end = YearMonth.parse(exp.getEndDate(), formatter);
	            }
	            // Calculate months between start and end
	            long months = ChronoUnit.MONTHS.between(start, end);
	            totalMonths += months;
	        } catch (Exception e) {
	            // If the LLM messed up a date format, we skip it rather than crashing
	            System.out.println("Could not parse dates for company: " + exp.getCompany());
	        }
	    }
	    // Convert total months to years (e.g., 26 months / 12 = 2.16 years)
	    double totalYears = totalMonths / 12.0;
	    
	    // Round to 1 decimal place (e.g., 2.2)
	    return Math.round(totalYears * 10.0) / 10.0;
	}
}
