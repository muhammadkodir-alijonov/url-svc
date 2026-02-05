package com.example.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_users_keycloak_id", columnList = "keycloak_id"),
        @Index(name = "idx_users_username", columnList = "username")
})
public class User extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "UUID")
    public UUID id;

    @Column(name = "keycloak_id", nullable = false, unique = true, length = 100)
    public String keycloakId;

    @Column(nullable = false, unique = true, length = 50)
    public String username;

    @Column(nullable = false, unique = true)
    public String email;

    @Column(length = 20)
    public String plan = "FREE";

    @Column(name = "links_created")
    public Integer linksCreated = 0;

    @Column(name = "links_limit")
    public Integer linksLimit = 100;

    @Column(name = "created_at", nullable = false, updatable = false)
    public Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    public Instant updatedAt;

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    // Business logic methods
    public boolean canCreateLink() {
        return linksCreated < linksLimit;
    }

    public void incrementLinksCreated() {
        this.linksCreated++;
    }

    public void decrementLinksCreated() {
        if (this.linksCreated > 0) {
            this.linksCreated--;
        }
    }

    // toString for debugging
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", plan='" + plan + '\'' +
                ", linksCreated=" + linksCreated +
                ", linksLimit=" + linksLimit +
                '}';
    }
}