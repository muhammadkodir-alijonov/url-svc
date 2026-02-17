package com.example.repository;

import com.example.domain.User;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class UserRepository implements PanacheRepository<User> {

    public Optional<User> findByKeycloakId(String keycloakId) {
        return find("keycloakId", keycloakId).firstResultOptional();
    }

    public Optional<User> findById(UUID userId) {
        return find("id", userId).firstResultOptional();
    }

    public Optional<User> findByUsername(String username) {
        return find("username", username).firstResultOptional();
    }

    public Optional<User> findByEmail(String email) {
        return find("email", email).firstResultOptional();
    }

    public boolean existsByUsername(String username) {
        return count("username", username) > 0;
    }

    public boolean existsByEmail(String email) {
        return count("email", email) > 0;
    }

    public void incrementLinksCreated(UUID userId) {
        update("linksCreated = linksCreated + 1 where id = ?1", userId);
    }

    public void decrementLinksCreated(UUID userId) {
        update("linksCreated = linksCreated - 1 where id = ?1 and linksCreated > 0", userId);
    }

    public void updatePlan(UUID userId, String plan) {
        update("plan = ?1 where id = ?2", plan, userId);
    }
}