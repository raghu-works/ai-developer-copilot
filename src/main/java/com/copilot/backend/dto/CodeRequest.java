package com.copilot.backend.dto;


	import lombok.AllArgsConstructor;
	import lombok.Data;
	import lombok.NoArgsConstructor;

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public class CodeRequest {

	    private String code;

	    private String language;

	    private String operation;

	    private Long sessionId;
	}


