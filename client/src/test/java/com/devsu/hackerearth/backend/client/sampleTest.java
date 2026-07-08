package com.devsu.hackerearth.backend.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;

import com.devsu.hackerearth.backend.client.controller.ClientController;
import com.devsu.hackerearth.backend.client.model.Client;
import com.devsu.hackerearth.backend.client.model.Person;
import com.devsu.hackerearth.backend.client.model.dto.BasicResponse;
import com.devsu.hackerearth.backend.client.model.dto.ClientDto;
import com.devsu.hackerearth.backend.client.service.ClientService;

@SpringBootTest
public class sampleTest {

    private ClientService clientService = mock(ClientService.class);
    private ClientController clientController = new ClientController(clientService);

    @Test
    void createClientTest() {
        // Arrange
        ClientDto newClient = new ClientDto(1L, "Dni", "Name", "Password", "Gender", 1, "Address", "9999999999", true);
        ClientDto createdClient = new ClientDto(1L, "Dni", "Name", "Password", "Gender", 1, "Address", "9999999999",
                true);
        when(clientService.create(newClient)).thenReturn(createdClient);

        // Act
        BasicResponse<ClientDto> response = clientController.create(newClient);

        // Assert
        assertEquals(HttpStatus.CREATED.value(), response.getCode());
        assertEquals(createdClient, response.getData());
    }

    @Test
    void clientEntityShouldStoreOwnAndInheritedPersonFields() {
        // Arrange + Act: Client is a domain entity that extends Person
        Client client = new Client();
        client.setName("Maria Perez");
        client.setDni("0102030405");
        client.setGender("F");
        client.setAge(28);
        client.setAddress("Av. Principal 123");
        client.setPhone("0999999999");
        client.setPassword("encoded-password");
        client.setActive(true);

        // Assert: fields declared directly on Client
        assertEquals("encoded-password", client.getPassword());
        assertTrue(client.isActive());

        // Assert: fields inherited from Person are also available on Client
        assertEquals("Maria Perez", client.getName());
        assertEquals("0102030405", client.getDni());
        assertEquals("F", client.getGender());
        assertEquals(28, client.getAge());
        assertEquals("Av. Principal 123", client.getAddress());
        assertEquals("0999999999", client.getPhone());
        assertTrue(client instanceof Person);
    }
}
