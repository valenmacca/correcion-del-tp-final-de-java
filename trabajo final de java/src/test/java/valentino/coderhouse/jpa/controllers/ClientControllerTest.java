/* package valentino.coderhouse.jpa.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import valentino.coderhouse.jpa.entities.Client;
import org.springframework.http.ResponseEntity;
import static org.junit.jupiter.api.Assertions.*;
import java.util.UUID;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ClientControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void testCreateClient() {
        Client client = new Client();
        client.setName("valentino");
        client.setLastName("maccaroni");
        client.setDocNumber("46573166");

        ResponseEntity<Client> response = restTemplate.postForEntity("/api/clientes", client, Client.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody().getId());
    }

    @Test
    void testGetClientNotFound() {
        ResponseEntity<Client> response = restTemplate.getForEntity("/api/clientes/" + UUID.randomUUID(), Client.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

}*/
