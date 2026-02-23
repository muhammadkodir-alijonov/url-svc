package com.example.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "urls", indexes = {
        @Index(name = "idx_urls_short_code", columnList = "short_code", unique = true),
        @Index(name = "idx_urls_user_id", columnList = "user_id"),
        @Index(name = "idx_urls_created_at", columnList = "created_at"),
        @Index(name = "idx_urls_active", columnList = "is_active")
})
public class Url extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "short_code", nullable = false, unique = true, length = 10)
    public String shortCode;

    @Column(name = "original_url", nullable = false, columnDefinition = "TEXT")
    public String originalUrl;

    @Column(name = "user_id", nullable = false, columnDefinition = "UUID")
    public UUID userId;

    // Security
    @Column(name = "password_hash")
    public String passwordHash;

    // Metadata
    @Column(length = 255)
    public String title;

    // Analytics
    @Column(nullable = false)
    public Integer clicks = 0;

    // Expiration
    @Column(name = "expires_at")
    public Instant expiresAt;

    // Status
    @Column(name = "is_active", nullable = false)
    public Boolean isActive = true;

    @Column(name = "is_custom", nullable = false)
    public Boolean isCustom = false;

    // Timestamps
    @Column(name = "created_at", nullable = false, updatable = false)
    public Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    public Instant updatedAt;

    @Column(name = "last_accessed_at")
    public Instant lastAccessedAt;

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
    public boolean isExpired() {
        return expiresAt != null && Instant.now().isAfter(expiresAt);
    }

    public boolean hasPassword() {
        return passwordHash != null && !passwordHash.isEmpty();
    }

    public void incrementClicks() {
        this.clicks++;
    }

    public void updateLastAccessed() {
        this.lastAccessedAt = Instant.now();
    }

    // toString for debugging
    @Override
    public String toString() {
        return "Url{" +
                "id=" + id +
                ", shortCode='" + shortCode + '\'' +
                ", originalUrl='" + originalUrl + '\'' +
                ", userId=" + userId +
                ", clicks=" + clicks +
                ", isActive=" + isActive +
                ", isExpired=" + isExpired() +
                ", hasPassword=" + hasPassword() +
                ", createdAt=" + createdAt +
                '}';
    }
}