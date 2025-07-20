package com.clinic.appointment.controller;

import com.clinic.appointment.model.Appointment;
import com.clinic.appointment.service.AppointmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/appointments")
@CrossOrigin(origins = "*") // Allow CORS for frontend
public class AppointmentController {

    private static final Logger logger = LoggerFactory.getLogger(AppointmentController.class);

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @GetMapping
    public ResponseEntity<List<Appointment>> getAllAppointments() {
        logger.info("GET /appointments - Fetching all appointments");
        try {
            List<Appointment> appointments = appointmentService.getAllAppointments();
            logger.info("Successfully retrieved {} appointments", appointments.size());
            return ResponseEntity.ok(appointments);
        } catch (Exception e) {
            logger.error("Error fetching appointments: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/upcoming")
    public ResponseEntity<List<Appointment>> getUpcomingAppointments() {
        logger.info("GET /appointments/upcoming - Fetching upcoming appointments");
        try {
            List<Appointment> appointments = appointmentService.getUpcomingAppointments();
            logger.info("Successfully retrieved {} upcoming appointments", appointments.size());
            return ResponseEntity.ok(appointments);
        } catch (Exception e) {
            logger.error("Error fetching upcoming appointments: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/today")
    public ResponseEntity<List<Appointment>> getTodaysAppointments() {
        logger.info("GET /appointments/today - Fetching today's appointments");
        try {
            List<Appointment> appointments = appointmentService.getTodaysAppointments();
            logger.info("Successfully retrieved {} appointments for today", appointments.size());
            return ResponseEntity.ok(appointments);
        } catch (Exception e) {
            logger.error("Error fetching today's appointments: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Appointment> getAppointmentById(@PathVariable String id) {
        logger.info("GET /appointments/{} - Fetching appointment by id", id);
        try {
            Optional<Appointment> appointment = appointmentService.getAppointmentById(id);
            if (appointment.isPresent()) {
                logger.info("Successfully retrieved appointment: {}", appointment.get().getId());
                return ResponseEntity.ok(appointment.get());
            } else {
                logger.warn("Appointment not found with id: {}", id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Error fetching appointment by id {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<Appointment>> getAppointmentsByClientId(@PathVariable String clientId) {
        logger.info("GET /appointments/client/{} - Fetching appointments for client", clientId);
        try {
            List<Appointment> appointments = appointmentService.getAppointmentsByClientId(clientId);
            logger.info("Successfully retrieved {} appointments for client: {}", appointments.size(), clientId);
            return ResponseEntity.ok(appointments);
        } catch (Exception e) {
            logger.error("Error fetching appointments for client {}: {}", clientId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Appointment>> getAppointmentsByStatus(@PathVariable String status) {
        logger.info("GET /appointments/status/{} - Fetching appointments by status", status);
        try {
            Appointment.AppointmentStatus appointmentStatus = Appointment.AppointmentStatus.valueOf(status.toUpperCase());
            List<Appointment> appointments = appointmentService.getAppointmentsByStatus(appointmentStatus);
            logger.info("Successfully retrieved {} appointments with status: {}", appointments.size(), status);
            return ResponseEntity.ok(appointments);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid appointment status: {}", status);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error fetching appointments by status {}: {}", status, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/range")
    public ResponseEntity<List<Appointment>> getAppointmentsBetween(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        logger.info("GET /appointments/range - Fetching appointments between {} and {}", startTime, endTime);
        try {
            List<Appointment> appointments = appointmentService.getAppointmentsBetween(startTime, endTime);
            logger.info("Successfully retrieved {} appointments in date range", appointments.size());
            return ResponseEntity.ok(appointments);
        } catch (Exception e) {
            logger.error("Error fetching appointments in date range: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    public ResponseEntity<Appointment> createAppointment(@Valid @RequestBody Appointment appointment) {
        logger.info("POST /appointments - Creating new appointment for client: {}", appointment.getClientId());
        try {
            Appointment createdAppointment = appointmentService.createAppointment(appointment);
            logger.info("Successfully created appointment: {}", createdAppointment.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(createdAppointment);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid appointment data: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error creating appointment: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Appointment> updateAppointment(@PathVariable String id, @Valid @RequestBody Appointment appointment) {
        logger.info("PUT /appointments/{} - Updating appointment", id);
        try {
            Appointment updatedAppointment = appointmentService.updateAppointment(id, appointment);
            logger.info("Successfully updated appointment: {}", updatedAppointment.getId());
            return ResponseEntity.ok(updatedAppointment);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid appointment update: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error updating appointment {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<String> cancelAppointment(@PathVariable String id) {
        logger.info("PATCH /appointments/{}/cancel - Cancelling appointment", id);
        try {
            appointmentService.cancelAppointment(id);
            logger.info("Successfully cancelled appointment: {}", id);
            return ResponseEntity.ok("Appointment cancelled successfully");
        } catch (IllegalArgumentException e) {
            logger.warn("Cannot cancel appointment: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error cancelling appointment {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error cancelling appointment: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAppointment(@PathVariable String id) {
        logger.info("DELETE /appointments/{} - Deleting appointment", id);
        try {
            appointmentService.deleteAppointment(id);
            logger.info("Successfully deleted appointment: {}", id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            logger.warn("Cannot delete appointment: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error deleting appointment {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/sync")
    public ResponseEntity<String> manualSync() {
        logger.info("POST /appointments/sync - Manual sync requested");
        try {
            appointmentService.manualSync();
            return ResponseEntity.ok("Sync completed successfully");
        } catch (Exception e) {
            logger.error("Error during manual sync: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Sync failed: " + e.getMessage());
        }
    }

    @GetMapping("/stats/count/{status}")
    public ResponseEntity<Long> getAppointmentCountByStatus(@PathVariable String status) {
        logger.info("GET /appointments/stats/count/{} - Getting appointment count by status", status);
        try {
            Appointment.AppointmentStatus appointmentStatus = Appointment.AppointmentStatus.valueOf(status.toUpperCase());
            long count = appointmentService.countAppointmentsByStatus(appointmentStatus);
            logger.info("Successfully retrieved count for status {}: {}", status, count);
            return ResponseEntity.ok(count);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid appointment status: {}", status);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error getting appointment count by status {}: {}", status, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
