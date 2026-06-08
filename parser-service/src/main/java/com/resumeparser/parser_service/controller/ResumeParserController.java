package com.resumeparser.parser_service.controller;

import java.util.concurrent.CompletableFuture;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.resumeparser.parser_service.dto.ParsedResumeDTO;
import com.resumeparser.parser_service.service.GeminiLlmService;
//import com.resumeparser.parser_service.service.ResumeParserService;
import com.resumeparser.parser_service.service.ResumeParserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/parser")
@RequiredArgsConstructor
public class ResumeParserController {
	private final ResumeParserService resumeParserService;
	private final GeminiLlmService geminiLlmService;

//	@PostMapping("/upload")
//	public ResponseEntity<ParsedResumeDTO> uploadResume(@RequestParam("file") MultipartFile file) {
//		ParsedResumeDTO result = resumeParserService.parseResume(file);
//		return ResponseEntity.ok(result);
//	}
	@PostMapping("/upload")
	public CompletableFuture<ResponseEntity<ParsedResumeDTO>> parseResumeLLM(@RequestParam("file") MultipartFile file) {
			System.out.println("Attempting AI parsing...");
			return geminiLlmService.parseResumeWithLlm(file)
					.thenApply(aiResult->{
						return ResponseEntity.ok(aiResult);
					})
					.exceptionally(e->{
						System.out.println("AI parsing failed! Falling back to Regex Parser.Error "+e.getMessage());
						ParsedResumeDTO regexResult=resumeParserService.parseResume(file);
						
						regexResult.setSummary("AI service is temporarily unavailable. Data parsed via standard texxt extraction.");
						
						return ResponseEntity.ok(regexResult);
					});
	}
}
