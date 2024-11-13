package valentino.coderhouse.jpa.services;

import valentino.coderhouse.jpa.entities.Client;
import valentino.coderhouse.jpa.repositories.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ClientService {

    @Autowired
    private ClientRepository clientRepository;

    public Client createClient(Client client) {
        return clientRepository.save(client);
    }

    public List<Client> getClients() {
        return clientRepository.findAll();
    }

    public void updateClient(String id, Client client) {
        Client existingClient = clientRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El cliente con el ID proporcionado no fue encontrado"));
        existingClient.setName(client.getName());
        existingClient.setLastName(client.getLastName());
        existingClient.setDocNumber(client.getDocNumber());
        clientRepository.save(existingClient);
    }

    public void deleteClient(String id) {
        if (clientRepository.existsById(id)) {
            clientRepository.deleteById(id);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Client not found");
        }
    }

    public Client getClientById(String id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente no encontrado"));
    }

    public Client findClientByDocNumber(String docNumber) {
        return clientRepository.findByDocNumber(docNumber)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Client not found"));
    }

}
