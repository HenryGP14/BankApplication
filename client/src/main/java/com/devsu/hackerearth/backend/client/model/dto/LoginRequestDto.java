package com.devsu.hackerearth.backend.client.model.dto;

import javax.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequestDto {

	@NotBlank(message = "El dni no puede estar vacío")
	private String dni;

	@NotBlank(message = "La contraseña no puede estar vacía")
	private String password;
}
