package com.devsu.hackerearth.backend.client.model.dto;

import javax.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ClientDto {

	private Long id;

	@NotBlank(message = "El dni no puede estar vacío")
	private String dni;

	@NotBlank(message = "El nombre no puede estar vacío")
	private String name;

	@NotBlank(message = "La contraseña no puede estar vacía")
	private String password;

	@NotBlank(message = "El género no puede estar vacío")
	private String gender;

	private int age;

	@NotBlank(message = "La dirección no puede estar vacía")
	private String address;

	@NotBlank(message = "El teléfono no puede estar vacío")
	private String phone;

	private boolean isActive;
}
