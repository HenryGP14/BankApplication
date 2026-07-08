package com.devsu.hackerearth.backend.account.communication.rest;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.devsu.hackerearth.backend.account.communication.rest.dto.ClientResponseDto;
import com.devsu.hackerearth.backend.account.exception.GenericException;

@Component
public class ClientRestClient {
    private final RestTemplate restTemplate;

    @Value("${services.client.base-url}")
	private String clientServiceBaseUrl;

    public ClientRestClient(RestTemplate restTemplate){
        this.restTemplate = restTemplate;
    }

    public CompletableFuture<ClientResponseDto> getClientById(Long clientId, String authorizationHeader){
        HttpHeaders headers = new HttpHeaders();

        if(authorizationHeader != null){
            headers.set(HttpHeaders.AUTHORIZATION, authorizationHeader);
        }

        try {
            ClientResponseDto client = restTemplate.exchange(
                this.clientServiceBaseUrl + "/api/clients/{id}",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                ClientResponseDto.class,
                clientId
            ).getBody();

            return CompletableFuture.completedFuture(client);
        } catch (HttpClientErrorException.NotFound ex) {
			throw new GenericException(HttpStatus.NOT_FOUND, "No se encontró el cliente con id " + clientId);
		} catch (HttpClientErrorException ex) {
			if (ex.getStatusCode() == HttpStatus.UNAUTHORIZED || ex.getStatusCode() == HttpStatus.FORBIDDEN) {
				throw ex;
			}
			throw new GenericException(HttpStatus.NOT_FOUND, "No se encontró el cliente con id " + clientId);
		}
    }

    public ClientResponseDto getClientByIdBlocking(Long clientId, String authorizationHeader) {
		try {
			return getClientById(clientId, authorizationHeader).get();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException("La validación del cliente fue interrumpida", e);
		} catch (ExecutionException e) {
			if (e.getCause() instanceof RuntimeException) {
				throw (RuntimeException) e.getCause();
			}
			throw new IllegalStateException("No se pudo validar el cliente", e);
		}
	}
}
