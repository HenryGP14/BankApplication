package com.devsu.hackerearth.backend.client.controller;

import java.util.List;

import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.devsu.hackerearth.backend.client.model.dto.BasicResponse;
import com.devsu.hackerearth.backend.client.model.dto.ClientDto;
import com.devsu.hackerearth.backend.client.model.dto.PartialClientDto;
import com.devsu.hackerearth.backend.client.service.ClientService;

@RestController
@RequestMapping("/api/clients")
public class ClientController {

	private final ClientService clientService;

	public ClientController(ClientService clientService) {
		this.clientService = clientService;
	}

	@GetMapping
	public BasicResponse<List<ClientDto>> getAll() {
		List<ClientDto> clients = this.clientService.getAll();
		return BasicResponse.of(HttpStatus.OK, "Clientes obtenidos correctamente", clients);
	}

	@GetMapping("/{id}")
	public BasicResponse<ClientDto> get(@PathVariable Long id) {
		ClientDto client = this.clientService.getById(id);
		return BasicResponse.of(HttpStatus.OK, "Cliente obtenido correctamente", client);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public BasicResponse<ClientDto> create(@Valid @RequestBody ClientDto clientDto) {
		ClientDto created = this.clientService.create(clientDto);
		return BasicResponse.of(HttpStatus.CREATED, "Cliente creado correctamente", created);
	}

	@PutMapping("/{id}")
	public BasicResponse<ClientDto> update(@PathVariable Long id, @Valid @RequestBody ClientDto clientDto) {
		clientDto.setId(id);
		ClientDto updated = this.clientService.update(clientDto);
		return BasicResponse.of(HttpStatus.OK, "Cliente actualizado correctamente", updated);
	}

	@PatchMapping("/{id}")
	public BasicResponse<ClientDto> partialUpdate(@PathVariable Long id,
			@RequestBody PartialClientDto partialClientDto) {
		ClientDto updated = this.clientService.partialUpdate(id, partialClientDto);
		return BasicResponse.of(HttpStatus.OK, "Cliente actualizado correctamente", updated);
	}

	@DeleteMapping("/{id}")
	public BasicResponse<Void> delete(@PathVariable Long id) {
		this.clientService.deleteById(id);
		return BasicResponse.of(HttpStatus.OK, "Cliente eliminado correctamente", null);
	}
}
