package com.devsu.hackerearth.backend.client.service;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.devsu.hackerearth.backend.client.exception.GenericException;
import com.devsu.hackerearth.backend.client.model.Client;
import com.devsu.hackerearth.backend.client.model.dto.ClientDto;
import com.devsu.hackerearth.backend.client.model.dto.PartialClientDto;
import com.devsu.hackerearth.backend.client.repository.ClientRepository;

@Service
public class ClientServiceImpl implements ClientService {

	private static final Pattern PASSWORD_PATTERN = Pattern.compile("^.{8,}$");
	private static final String PASSWORD_RULE_MESSAGE = "La contraseña debe tener al menos 8 caracteres";

	private final ClientRepository clientRepository;
	private final PasswordEncoder passwordEncoder;

	public ClientServiceImpl(ClientRepository clientRepository, PasswordEncoder passwordEncoder) {
		this.clientRepository = clientRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public List<ClientDto> getAll() {
		return clientRepository.findAll().stream()
				.map(this::mapClientToDto)
				.collect(Collectors.toList());
	}

	@Override
	public ClientDto getById(Long id) {
		Client client = findClientOrThrow(id);
		return mapClientToDto(client);
	}

	@Override
	@Transactional
	public ClientDto create(ClientDto clientDto) {
		if (clientRepository.existsByDni(clientDto.getDni())) {
			throw new GenericException(HttpStatus.CONFLICT, "Ya existe un cliente con el dni " + clientDto.getDni());
		}
		validatePassword(clientDto.getPassword());

		Client newClient = new Client();
		newClient.setDni(clientDto.getDni());
		mapDtoToClient(newClient, clientDto);
		newClient.setPassword(passwordEncoder.encode(clientDto.getPassword()));

		Client savedClient = clientRepository.save(newClient);
		return mapClientToDto(savedClient);
	}

	@Override
	@Transactional
	public ClientDto update(ClientDto clientDto) {
		Client existingClient = findClientOrThrow(clientDto.getId());

		if (clientDto.getDni() != null && !clientDto.getDni().equals(existingClient.getDni())) {
			throw new GenericException(HttpStatus.BAD_REQUEST, "El dni no se puede modificar una vez registrado el cliente");
		}

		mapDtoToClient(existingClient, clientDto);
		if (clientDto.getPassword() != null && !clientDto.getPassword().isBlank()) {
			validatePassword(clientDto.getPassword());
			existingClient.setPassword(passwordEncoder.encode(clientDto.getPassword()));
		}

		Client updatedClient = clientRepository.save(existingClient);
		return mapClientToDto(updatedClient);
	}

	@Override
	@Transactional
	public ClientDto partialUpdate(Long id, PartialClientDto partialClientDto) {
		Client existingClient = findClientOrThrow(id);

		if (partialClientDto.getName() != null) {
			existingClient.setName(partialClientDto.getName());
		}
		if (partialClientDto.getGender() != null) {
			existingClient.setGender(partialClientDto.getGender());
		}
		if (partialClientDto.getAge() != null) {
			existingClient.setAge(partialClientDto.getAge());
		}
		if (partialClientDto.getAddress() != null) {
			existingClient.setAddress(partialClientDto.getAddress());
		}
		if (partialClientDto.getPhone() != null) {
			existingClient.setPhone(partialClientDto.getPhone());
		}
		if (partialClientDto.getPassword() != null && !partialClientDto.getPassword().isBlank()) {
			validatePassword(partialClientDto.getPassword());
			existingClient.setPassword(passwordEncoder.encode(partialClientDto.getPassword()));
		}
		if (partialClientDto.getActive() != null) {
			existingClient.setActive(partialClientDto.getActive());
		}

		Client updatedClient = clientRepository.save(existingClient);
		return mapClientToDto(updatedClient);
	}

	@Override
	@Transactional
	public void deleteById(Long id) {
		findClientOrThrow(id);
		clientRepository.deleteById(id);
	}

	private Client findClientOrThrow(Long id) {
		return clientRepository.findById(id)
				.orElseThrow(() -> new GenericException(HttpStatus.NOT_FOUND, "No se encontró el cliente con id " + id));
	}

	private void validatePassword(String password) {
		if (password == null || !PASSWORD_PATTERN.matcher(password).matches()) {
			throw new GenericException(HttpStatus.BAD_REQUEST, PASSWORD_RULE_MESSAGE);
		}
	}

	private void mapDtoToClient(Client client, ClientDto clientDto) {
		client.setName(clientDto.getName());
		client.setGender(clientDto.getGender());
		client.setAge(clientDto.getAge());
		client.setAddress(clientDto.getAddress());
		client.setPhone(clientDto.getPhone());
		client.setActive(clientDto.isActive());
	}

	private ClientDto mapClientToDto(Client client) {
		ClientDto dto = new ClientDto(client.getId(), client.getDni(), client.getName(), null, client.getGender(),
				client.getAge(), client.getAddress(), client.getPhone(), client.isActive());
		return dto;
	}
}
