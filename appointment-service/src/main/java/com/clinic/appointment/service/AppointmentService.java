package com.clinic.appointment.service;

import com.clinic.appointment.external.ExternalAppointmentApi;
import com.clinic.appointment.model.Appointment;
import com.clinic.appointment.repository.AppointmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class AppointmentService {

    private static final Logger logger = LoggerFactory.getLogger(AppointmentService.class);

    private final AppointmentRepository appointmentRepository;
    private final ExternalAppointmentApi externalAppointmentApi;

    public AppointmentService(AppointmentRepository appointmentRepository, ExternalAppointmentApi externalAppointmentApi) {
        this.appointmentRepository = appointmentRepository;
        this.externalAppointmentApi = externalAppointmentApi;
    }

    public List<Appointment> getAllAppointments() {
        logger.info("Fetching all appointments from database");
        return appointmentRepository.findAll();
    }

    public List<Appointment> getUpcomingAppointments() {
        logger.info("Fetching upcoming appointments");
        return appointmentRepository.findUpcomingAppointments(LocalDateTime.now());
    }

    public List<Appointment> getAppointmentsByClientId(String clientId) {
        logger.info("Fetching appointments for client: {}", clientId);
        return appointmentRepository.findByClientId(clientId);
    }

    public List<Appointment> getAppointmentsByStatus(Appointment.AppointmentStatus status) {
        logger.info("Fetching appointments by status: {}", status);
        return appointmentRepository.findByStatus(status);
    }

    public List<Appointment> getTodaysAppointments() {
        logger.info("Fetching today's appointments");
        return appointmentRepository.findAppointmentsByDate(LocalDateTime.now());
    }

    public List<Appointment> getAppointmentsBetween(LocalDateTime startTime, LocalDateTime endTime) {
        logger.info("Fetching appointments between {} and {}", startTime, endTime);
        return appointmentRepository.findAppointmentsBetween(startTime, endTime);
    }

    public Optional<Appointment> getAppointmentById(String id) {
        logger.info("Fetching appointment by id: {}", id);
        return appointmentRepository.findById(id);
    }

    public Appointment createAppointment(Appointment appointment) {
        logger.info("Creating new appointment for client: {}", appointment.getClientId());
        
        try {
            // Generate ID if not provided
            if (appointment.getId() == null || appointment.getId().isEmpty()) {
                appointment.setId(UUID.randomUUID().toString());
            }

            // Check for time conflicts
            if (appointmentRepository.existsByTimeAndNotCancelled(appointment.getTime())) {
                throw new IllegalArgumentException("Appointment slot is already booked");
            }

            // Validate appointment time is in the future
            if (appointment.getTime().isBefore(LocalDateTime.now())) {
                throw new IllegalArgumentException("Appointment time must be in the future");
            }

            // Set default status if not provided
            if (appointment.getStatus() == null) {
                appointment.setStatus(Appointment.AppointmentStatus.SCHEDULED);
            }

            // Save to local database first
            Appointment savedAppointment = appointmentRepository.save(appointment);
            logger.info("Appointment saved locally: {}", savedAppointment.getId());

            // Try to sync with external API
            try {
                if (externalAppointmentApi.isApiAvailable()) {
                    Appointment externalAppointment = externalAppointmentApi.createAppointment(appointment);
                    if (externalAppointment != null) {
                        logger.info("Appointment synced with external API: {}", externalAppointment.getId());
                    }
                }
            } catch (Exception e) {
                logger.warn("Failed to sync appointment with external API: {}", e.getMessage());
                // Continue with local appointment even if external sync fails
            }

            return savedAppointment;

        } catch (Exception e) {
            logger.error("Error creating appointment: {}", e.getMessage(), e);
            throw e;
        }
    }

    public Appointment updateAppointment(String id, Appointment appointmentDetails) {
        logger.info("Updating appointment: {}", id);
        
        Optional<Appointment> existingAppointment = appointmentRepository.findById(id);
        if (!existingAppointment.isPresent()) {
            throw new IllegalArgumentException("Appointment not found with id: " + id);
        }

        Appointment appointment = existingAppointment.get();
        
        // Update fields
        if (appointmentDetails.getTime() != null) {
            // Check for time conflicts (excluding current appointment)
            if (!appointment.getTime().equals(appointmentDetails.getTime()) &&
                appointmentRepository.existsByTimeAndNotCancelled(appointmentDetails.getTime())) {
                throw new IllegalArgumentException("Appointment slot is already booked");
            }
            appointment.setTime(appointmentDetails.getTime());
        }
        
        if (appointmentDetails.getNotes() != null) {
            appointment.setNotes(appointmentDetails.getNotes());
        }
        
        if (appointmentDetails.getStatus() != null) {
            appointment.setStatus(appointmentDetails.getStatus());
        }

        appointment.setUpdatedAt(LocalDateTime.now());
        
        Appointment updatedAppointment = appointmentRepository.save(appointment);
        logger.info("Successfully updated appointment: {}", updatedAppointment.getId());
        
        return updatedAppointment;
    }

    public void cancelAppointment(String id) {
        logger.info("Cancelling appointment: {}", id);
        
        Optional<Appointment> existingAppointment = appointmentRepository.findById(id);
        if (!existingAppointment.isPresent()) {
            throw new IllegalArgumentException("Appointment not found with id: " + id);
        }

        Appointment appointment = existingAppointment.get();
        appointment.setStatus(Appointment.AppointmentStatus.CANCELLED);
        appointment.setUpdatedAt(LocalDateTime.now());
        
        appointmentRepository.save(appointment);
        logger.info("Successfully cancelled appointment: {}", id);
    }

    public void deleteAppointment(String id) {
        logger.info("Deleting appointment: {}", id);
        
        if (!appointmentRepository.existsById(id)) {
            throw new IllegalArgumentException("Appointment not found with id: " + id);
        }
        
        appointmentRepository.deleteById(id);
        logger.info("Successfully deleted appointment: {}", id);
    }

    public long countAppointmentsByStatus(Appointment.AppointmentStatus status) {
        return appointmentRepository.countByStatus(status);
    }

    // Scheduled method to sync appointments from external API every 5 minutes
    @Scheduled(fixedDelay = 300000) // 5 minutes = 300,000 milliseconds
    public void syncAppointments() {
        logger.info("Starting scheduled sync of appointments from external API");
        
        try {
            if (!externalAppointmentApi.isApiAvailable()) {
                logger.warn("External API is not available, skipping sync");
                return;
            }

            List<Appointment> externalAppointments = externalAppointmentApi.fetchAppointments();
            
            if (externalAppointments.isEmpty()) {
                logger.warn("No appointments received from external API");
                return;
            }

            int syncedCount = 0;
            int updatedCount = 0;

            for (Appointment externalAppointment : externalAppointments) {
                Optional<Appointment> existingAppointment = appointmentRepository.findById(externalAppointment.getId());
                
                if (existingAppointment.isPresent()) {
                    // Update existing appointment if data has changed
                    Appointment existing = existingAppointment.get();
                    if (!existing.getTime().equals(externalAppointment.getTime()) ||
                        !existing.getClientId().equals(externalAppointment.getClientId())) {
                        
                        existing.setTime(externalAppointment.getTime());
                        existing.setClientId(externalAppointment.getClientId());
                        existing.setUpdatedAt(LocalDateTime.now());
                        appointmentRepository.save(existing);
                        updatedCount++;
                        logger.debug("Updated appointment: {}", existing.getId());
                    }
                } else {
                    // Save new appointment
                    externalAppointment.setCreatedAt(LocalDateTime.now());
                    externalAppointment.setUpdatedAt(LocalDateTime.now());
                    appointmentRepository.save(externalAppointment);
                    syncedCount++;
                    logger.debug("Synced new appointment: {}", externalAppointment.getId());
                }
            }

            logger.info("Sync completed. New appointments: {}, Updated appointments: {}", syncedCount, updatedCount);

        } catch (Exception e) {
            logger.error("Error during appointment sync: {}", e.getMessage(), e);
        }
    }

    // Manual sync method that can be called via API
    public void manualSync() {
        logger.info("Manual sync requested");
        syncAppointments();
    }
}
