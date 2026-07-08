package com.devsu.hackerearth.backend.account.model.dto;

import java.util.Date;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TransactionDto {

	private Long id;
	private Date date;
	@NotBlank(message = "El tipo de transacción no puede estar vacío")
	private String type;

	private double amount;
	private Double balance;
	
	@NotNull(message = "El id de la cuenta es obligatorio")
	private Long accountId;
}
