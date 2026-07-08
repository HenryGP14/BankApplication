package com.devsu.hackerearth.backend.account.communication.rest.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClientResponseDto {

	private Long id;
	private String dni;
	private String name;
	private boolean isActive;
}
