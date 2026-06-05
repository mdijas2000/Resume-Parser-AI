package com.resumeparser.parser_service.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExperienceDTO {
	private String company;
	private String role;
	private String duration;
	@JsonProperty("isCurrentJob")
	private boolean isCurrentJob;
}
