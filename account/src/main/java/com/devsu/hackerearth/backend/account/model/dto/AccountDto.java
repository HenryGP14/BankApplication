package com.devsu.hackerearth.backend.account.model.dto;

import javax.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AccountDto {

	private Long id;
	@NotBlank(message = "El número de cuenta no puede estar vacío")
	private String number;

	@NotBlank(message = "El tipo de cuenta no puede estar vacío")
	private String type;
	private double initialAmount;
	private boolean isActive;
	private Long clientId;
}
