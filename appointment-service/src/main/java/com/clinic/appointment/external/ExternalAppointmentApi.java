package com.clinic.appointment.external;

import com.clinic.appointment.model.Appointment;
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
public class ExternalAppointmentApi {

    private static final Logger logger = LoggerFactory.getLogger(ExternalAppointmentApi.class);

    private final RestTemplate restTemplate;

    @Value("${external.api.url.appointments}")
    private String appointmentsApiUrl;

    @Value("${external.api.key}")
    private String apiKey;

    public ExternalAppointmentApi(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<Appointment> fetchAppointments() {
        try {
            logger.info("Fetching appointments from external API: {}", appointmentsApiUrl);

            // Create headers with authorization
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + apiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            // Make the API call
            ResponseEntity<Appointment[]> response = restTemplate.exchange(
                    appointmentsApiUrl,
                    HttpMethod.GET,
                    entity,
                    Appointment[].class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                List<Appointment> appointments = Arrays.asList(response.getBody());
                logger.info("Successfully fetched {} appointments from external API", appointments.size());
                return appointments;
            } else {
                logger.warn("External API returned status: {}", response.getStatusCode());
                return Collections.emptyList();
            }

        } catch (Exception e) {
            logger.error("Error fetching appointments from external API: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    public Appointment createAppointment(Appointment appointment) {
        try {
            logger.info("Creating appointment via external API for client: {}", appointment.getClientId());

            // Create headers with authorization
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + apiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Create request body
            AppointmentRequest requestBody = new AppointmentRequest(
                    appointment.getClientId(),
                    appointment.getTime().toString()
            );

            HttpEntity<AppointmentRequest> entity = new HttpEntity<>(requestBody, headers);

            // Make the API call
            ResponseEntity<Appointment> response = restTemplate.exchange(
                    appointmentsApiUrl,
                    HttpMethod.POST,
                    entity,
                    Appointment.class
            );

            if (response.getStatusCode() == HttpStatus.CREATED && response.getBody() != null) {
                logger.info("Successfully created appointment via external API: {}", response.getBody().getId());
                return response.getBody();
            } else {
                logger.warn("External API returned status: {}", response.getStatusCode());
                return null;
            }

        } catch (Exception e) {
            logger.error("Error creating appointment via external API: {}", e.getMessage(), e);
            return null;
        }
    }

    public boolean isApiAvailable() {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + apiKey);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    appointmentsApiUrl,
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

    // Inner class for appointment creation request
    public static class AppointmentRequest {
        private String client_id;
        private String time;

        public AppointmentRequest() {}

        public AppointmentRequest(String client_id, String time) {
            this.client_id = client_id;
            this.time = time;
        }

        public String getClient_id() {
            return client_id;
        }

        public void setClient_id(String client_id) {
            this.client_id = client_id;
        }

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }
    }
}
