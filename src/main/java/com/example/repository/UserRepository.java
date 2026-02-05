package com.example.repository;

import com.example.domain.User;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class UserRepository implements PanacheRepository<User> {

    /**
     * Find user by Keycloak ID
     */
    public Optional<User> findByKeycloakId(String keycloakId) {
        return find("keycloakId", keycloakId).firstResultOptional();
    }
    /**
     * Find user by UUID
     */
    public Optional<User> findById(UUID userId) {
        return find("id", userId).firstResultOptional();
    }

    /**
     * Find user by username
     */
    public Optional<User> findByUsername(String username) {
        return find("username", username).firstResultOptional();
    }

    /**
     * Find user by email
     */
    public Optional<User> findByEmail(String email) {
        return find("email", email).firstResultOptional();
    }

    /**
     * Check if username exists
     */
    public boolean existsByUsername(String username) {
        return count("username", username) > 0;
    }

    /**
     * Check if email exists
     */
    public boolean existsByEmail(String email) {
        return count("email", email) > 0;
    }

    /**
     * Increment links created counter
     */
    public void incrementLinksCreated(UUID userId) {
        update("linksCreated = linksCreated + 1 where id = ?1", userId);
    }

    /**
     * Decrement links created counter
     */
    public void decrementLinksCreated(UUID userId) {
        update("linksCreated = linksCreated - 1 where id = ?1 and linksCreated > 0", userId);
    }

    /**
     * Update user plan
     */
    public void updatePlan(UUID userId, String plan) {
        update("plan = ?1 where id = ?2", plan, userId);
    }
}