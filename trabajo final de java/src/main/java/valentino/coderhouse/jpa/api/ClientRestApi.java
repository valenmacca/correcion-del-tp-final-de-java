package valentino.coderhouse.jpa.api;

import valentino.coderhouse.jpa.entities.Client;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class ClientRestApi {

    final String url = "https://jsonplaceholder.typicode.com/clients";

    public ResponseEntity<Client[]> getClients() {
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.getForEntity(
                this.url,
                Client[].class
        );
    }

    public Client getClientById(String id) {
        RestTemplate restTemplate = new RestTemplate();
        Map<String, Object> params = new HashMap<>();
        params.put("id", id.toString());
        try {
            return restTemplate.getForObject(
                    this.url + "/{id}",
                    Client.class,
                    params
            );
        } catch (HttpClientErrorException.NotFound e) {
            return null;
        } catch (Exception e) {
            throw new RuntimeException("Error to obtein a client", e);
        }
    }

    public Client saveClient(Client client) {
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.postForObject(
                this.url,
                client,
                Client.class
        );
    }

    public Client updateClient(String id, Client client) {
        RestTemplate restTemplate = new RestTemplate();
        Map<String, Object> params = new HashMap<>();
        params.put("id", id.toString());
        restTemplate.put(
                this.url + "/{id}",
                client,
                params
        );
        return client;
    }

    public Client deleteClient(String id) {
        RestTemplate restTemplate = new RestTemplate();
        Map<String, Object> params = new HashMap<>();
        params.put("id", id.toString());
        Client client = restTemplate.getForObject(
                this.url + "/{id}",
                Client.class,
                params);
        if (client != null) {
            restTemplate.delete(
                    this.url + "/{id}",
                    params
            );
        } else {
            throw new RuntimeException("Client not found for deletion");
        }
        return client;
    }

}
