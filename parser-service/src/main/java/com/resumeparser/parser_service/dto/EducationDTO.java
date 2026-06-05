package com.resumeparser.parser_service.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class EducationDTO {
	private String degree;
	private String institution;
	private String passingYear;
	private String percentage;
	private String branch;
}
