package com.clinic.client.external;

import com.clinic.client.model.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component
public class ExternalClientApi {

    private static final Logger logger = LoggerFactory.getLogger(ExternalClientApi.class);

    private final RestTemplate restTemplate;

    @Value("${external.api.url.clients}")
    private String clientsApiUrl;

    @Value("${external.api.key}")
    private String apiKey;

    public ExternalClientApi(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<Client> fetchClients() {
        try {
            logger.info("Fetching clients from external API: {}", clientsApiUrl);

            // Create headers with authorization
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + apiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            // Make the API call
            ResponseEntity<Client[]> response = restTemplate.exchange(
                    clientsApiUrl,
                    HttpMethod.GET,
                    entity,
                    Client[].class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                List<Client> clients = Arrays.asList(response.getBody());
                logger.info("Successfully fetched {} clients from external API", clients.size());
                return clients;
            } else {
                logger.warn("External API returned status: {}", response.getStatusCode());
                return Collections.emptyList();
            }

        } catch (Exception e) {
            logger.error("Error fetching clients from external API: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    public boolean isApiAvailable() {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + apiKey);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    clientsApiUrl,
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            logger.warn("External API is not available: {}", e.getMessage());
            return false;
        }
    }
}
