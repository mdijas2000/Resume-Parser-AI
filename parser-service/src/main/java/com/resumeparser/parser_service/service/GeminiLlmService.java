package com.resumeparser.parser_service.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.resumeparser.parser_service.dto.ParsedResumeDTO;

@Service
public class GeminiLlmService {
	@Value("${gemini.api.key}")
	private String apiKey;
	
	public ParsedResumeDTO parseResumeWithLlm(MultipartFile file) {
		
		Tika tika=new Tika();
		String rawText;
		try {
			rawText = tika.parseToString(file.getInputStream());
		
		
		String url="https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key="+apiKey;
		
		String prompt = "You are an expert resume parser. Extract the candidate's details from the text below. " +
			    "Return ONLY a valid JSON object strictly matching this exact structure (fill in the values or leave null/empty if not found):\n" +
			    "{\n" +
			    "  \"name\": \"\",\n" +
			    "  \"email\": \"\",\n" +
			    "  \"phnNo\": \"\",\n" +
			    "  \"skills\": [\"\"],\n" +
			    "  \"certifications\": [\"\"],\n" +
			    "  \"totalExperience\": 0.0,\n" +
			    "  \"education\": [{ \"degree\": \"\", \"institution\": \"\", \"passingYear\": \"\", \"percentage\": \"\", \"branch\": \"\" }],\n" +
			    "  \"experience\": [{ \"company\": \"\", \"role\": \"\", \"duration\": \"\", \"isCurrentJob\": false }],\n" +
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
			
			System.out.println(extractedJson);
			
			ParsedResumeDTO parsedResume= mapper.readValue(extractedJson, ParsedResumeDTO.class);
			
			return parsedResume;
			
		}catch(Exception e) {
			throw new RuntimeException("Failed to parse LLM response",e);
		}
		
	}
}
