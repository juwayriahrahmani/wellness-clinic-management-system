package com.clinic.client.service;

import com.clinic.client.external.ExternalClientApi;
import com.clinic.client.model.Client;
import com.clinic.client.repository.ClientRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ClientService {

    private static final Logger logger = LoggerFactory.getLogger(ClientService.class);

    private final ClientRepository clientRepository;
    private final ExternalClientApi externalClientApi;

    public ClientService(ClientRepository clientRepository, ExternalClientApi externalClientApi) {
        this.clientRepository = clientRepository;
        this.externalClientApi = externalClientApi;
    }

    public List<Client> getAllClients() {
        logger.info("Fetching all clients from database");
        return clientRepository.findAll();
    }

    public Optional<Client> getClientById(String id) {
        logger.info("Fetching client by id: {}", id);
        return clientRepository.findById(id);
    }

    public Optional<Client> getClientByEmail(String email) {
        logger.info("Fetching client by email: {}", email);
        return clientRepository.findByEmail(email);
    }

    public List<Client> searchClientsByName(String name) {
        logger.info("Searching clients by name: {}", name);
        return clientRepository.findByNameContainingIgnoreCase(name);
    }

    public Client saveClient(Client client) {
        logger.info("Saving client: {}", client.getName());
        return clientRepository.save(client);
    }

    public void deleteClient(String id) {
        logger.info("Deleting client with id: {}", id);
        clientRepository.deleteById(id);
    }

    public boolean existsByEmail(String email) {
        return clientRepository.existsByEmail(email);
    }

    public boolean existsByPhone(String phone) {
        return clientRepository.existsByPhone(phone);
    }

    // Scheduled method to sync clients from external API every 5 minutes
    @Scheduled(fixedDelay = 300000) // 5 minutes = 300,000 milliseconds
    public void syncClients() {
        logger.info("Starting scheduled sync of clients from external API");
        
        try {
            if (!externalClientApi.isApiAvailable()) {
                logger.warn("External API is not available, skipping sync");
                return;
            }

            List<Client> externalClients = externalClientApi.fetchClients();
            
            if (externalClients.isEmpty()) {
                logger.warn("No clients received from external API");
                return;
            }

            int syncedCount = 0;
            int updatedCount = 0;

            for (Client externalClient : externalClients) {
                Optional<Client> existingClient = clientRepository.findById(externalClient.getId());
                
                if (existingClient.isPresent()) {
                    // Update existing client if data has changed
                    Client existing = existingClient.get();
                    if (!existing.getName().equals(externalClient.getName()) ||
                        !existing.getEmail().equals(externalClient.getEmail()) ||
                        !existing.getPhone().equals(externalClient.getPhone())) {
                        
                        existing.setName(externalClient.getName());
                        existing.setEmail(externalClient.getEmail());
                        existing.setPhone(externalClient.getPhone());
                        clientRepository.save(existing);
                        updatedCount++;
                        logger.debug("Updated client: {}", existing.getName());
                    }
                } else {
                    // Save new client
                    clientRepository.save(externalClient);
                    syncedCount++;
                    logger.debug("Synced new client: {}", externalClient.getName());
                }
            }

            logger.info("Sync completed. New clients: {}, Updated clients: {}", syncedCount, updatedCount);

        } catch (Exception e) {
            logger.error("Error during client sync: {}", e.getMessage(), e);
        }
    }

    // Manual sync method that can be called via API
    public void manualSync() {
        logger.info("Manual sync requested");
        syncClients();
    }
}
