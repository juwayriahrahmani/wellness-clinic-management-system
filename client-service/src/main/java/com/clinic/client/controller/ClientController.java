package com.clinic.client.controller;

import com.clinic.client.model.Client;
import com.clinic.client.service.ClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/clients")
@CrossOrigin(origins = "*") // Allow CORS for frontend
public class ClientController {

    private static final Logger logger = LoggerFactory.getLogger(ClientController.class);

    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @GetMapping
    public ResponseEntity<List<Client>> getAllClients() {
        logger.info("GET /clients - Fetching all clients");
        try {
            List<Client> clients = clientService.getAllClients();
            logger.info("Successfully retrieved {} clients", clients.size());
            return ResponseEntity.ok(clients);
        } catch (Exception e) {
            logger.error("Error fetching clients: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Client> getClientById(@PathVariable String id) {
        logger.info("GET /clients/{} - Fetching client by id", id);
        try {
            Optional<Client> client = clientService.getClientById(id);
            if (client.isPresent()) {
                logger.info("Successfully retrieved client: {}", client.get().getName());
                return ResponseEntity.ok(client.get());
            } else {
                logger.warn("Client not found with id: {}", id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Error fetching client by id {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<Client>> searchClientsByName(@RequestParam String name) {
        logger.info("GET /clients/search?name={} - Searching clients by name", name);
        try {
            List<Client> clients = clientService.searchClientsByName(name);
            logger.info("Found {} clients matching name: {}", clients.size(), name);
            return ResponseEntity.ok(clients);
        } catch (Exception e) {
            logger.error("Error searching clients by name {}: {}", name, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    public ResponseEntity<Client> createClient(@Valid @RequestBody Client client) {
        logger.info("POST /clients - Creating new client: {}", client.getName());
        try {
            // Check if client already exists by email or phone
            if (clientService.existsByEmail(client.getEmail())) {
                logger.warn("Client already exists with email: {}", client.getEmail());
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }
            
            if (clientService.existsByPhone(client.getPhone())) {
                logger.warn("Client already exists with phone: {}", client.getPhone());
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }

            Client savedClient = clientService.saveClient(client);
            logger.info("Successfully created client: {}", savedClient.getName());
            return ResponseEntity.status(HttpStatus.CREATED).body(savedClient);
        } catch (Exception e) {
            logger.error("Error creating client: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Client> updateClient(@PathVariable String id, @Valid @RequestBody Client client) {
        logger.info("PUT /clients/{} - Updating client", id);
        try {
            Optional<Client> existingClient = clientService.getClientById(id);
            if (!existingClient.isPresent()) {
                logger.warn("Client not found with id: {}", id);
                return ResponseEntity.notFound().build();
            }

            client.setId(id);
            Client updatedClient = clientService.saveClient(client);
            logger.info("Successfully updated client: {}", updatedClient.getName());
            return ResponseEntity.ok(updatedClient);
        } catch (Exception e) {
            logger.error("Error updating client {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClient(@PathVariable String id) {
        logger.info("DELETE /clients/{} - Deleting client", id);
        try {
            Optional<Client> existingClient = clientService.getClientById(id);
            if (!existingClient.isPresent()) {
                logger.warn("Client not found with id: {}", id);
                return ResponseEntity.notFound().build();
            }

            clientService.deleteClient(id);
            logger.info("Successfully deleted client with id: {}", id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            logger.error("Error deleting client {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/sync")
    public ResponseEntity<String> manualSync() {
        logger.info("POST /clients/sync - Manual sync requested");
        try {
            clientService.manualSync();
            return ResponseEntity.ok("Sync completed successfully");
        } catch (Exception e) {
            logger.error("Error during manual sync: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Sync failed: " + e.getMessage());
        }
    }
}
