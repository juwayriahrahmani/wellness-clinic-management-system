package com.clinic.appointment.repository;

import com.clinic.appointment.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, String> {

    // Find appointments by client ID
    List<Appointment> findByClientId(String clientId);

    // Find appointments by status
    List<Appointment> findByStatus(Appointment.AppointmentStatus status);

    // Find upcoming appointments (after current time)
    @Query("SELECT a FROM Appointment a WHERE a.time > :currentTime ORDER BY a.time ASC")
    List<Appointment> findUpcomingAppointments(@Param("currentTime") LocalDateTime currentTime);

    // Find appointments for a specific date range
    @Query("SELECT a FROM Appointment a WHERE a.time BETWEEN :startTime AND :endTime ORDER BY a.time ASC")
    List<Appointment> findAppointmentsBetween(@Param("startTime") LocalDateTime startTime, 
                                            @Param("endTime") LocalDateTime endTime);

    // Find appointments by client ID and status
    List<Appointment> findByClientIdAndStatus(String clientId, Appointment.AppointmentStatus status);

    // Find today's appointments
    @Query("SELECT a FROM Appointment a WHERE DATE(a.time) = DATE(:date) ORDER BY a.time ASC")
    List<Appointment> findAppointmentsByDate(@Param("date") LocalDateTime date);

    // Find appointments for a specific client within date range
    @Query("SELECT a FROM Appointment a WHERE a.clientId = :clientId AND a.time BETWEEN :startTime AND :endTime ORDER BY a.time ASC")
    List<Appointment> findByClientIdAndTimeBetween(@Param("clientId") String clientId,
                                                 @Param("startTime") LocalDateTime startTime,
                                                 @Param("endTime") LocalDateTime endTime);

    // Count appointments by status
    long countByStatus(Appointment.AppointmentStatus status);

    // Check if appointment exists at specific time (for conflict checking)
    @Query("SELECT COUNT(a) > 0 FROM Appointment a WHERE a.time = :time AND a.status != 'CANCELLED'")
    boolean existsByTimeAndNotCancelled(@Param("time") LocalDateTime time);
}
