package com.clinic.client.repository;

import com.clinic.client.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, String> {

    // Find client by email
    Optional<Client> findByEmail(String email);

    // Find clients by name containing (case insensitive)
    @Query("SELECT c FROM Client c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Client> findByNameContainingIgnoreCase(@Param("name") String name);

    // Find clients by phone
    Optional<Client> findByPhone(String phone);

    // Check if client exists by email
    boolean existsByEmail(String email);

    // Check if client exists by phone
    boolean existsByPhone(String phone);
}
