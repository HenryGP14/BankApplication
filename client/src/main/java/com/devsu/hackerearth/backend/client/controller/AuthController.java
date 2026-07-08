package com.devsu.hackerearth.backend.client.controller;

import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.devsu.hackerearth.backend.client.model.Client;
import com.devsu.hackerearth.backend.client.model.dto.LoginRequestDto;
import com.devsu.hackerearth.backend.client.model.dto.LoginResponseDto;
import com.devsu.hackerearth.backend.client.repository.ClientRepository;
import com.devsu.hackerearth.backend.client.security.JwtService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final ClientRepository clientRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;

	public AuthController(ClientRepository clientRepository, PasswordEncoder passwordEncoder,
			JwtService jwtService) {
		this.clientRepository = clientRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
	}

	@PostMapping("/login")
	public ResponseEntity<LoginResponseDto> login(@RequestBody @Valid LoginRequestDto loginRequest) {
		Client client = clientRepository.findByDni(loginRequest.getDni())
				.filter(c -> passwordEncoder.matches(loginRequest.getPassword(), c.getPassword()))
				.orElseThrow(() -> new BadCredentialsException("Credenciales inválidas"));

		if (!client.isActive()) {
			throw new BadCredentialsException("El cliente se encuentra inactivo");
		}

		String token = jwtService.generateToken(client.getId(), client.getDni());
		LoginResponseDto data = new LoginResponseDto(token, "Bearer");
		return ResponseEntity.status(HttpStatus.OK).body(data);
	}
}
