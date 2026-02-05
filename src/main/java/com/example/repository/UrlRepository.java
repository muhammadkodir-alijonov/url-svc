package com.example.repository;

import com.example.domain.Url;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class UrlRepository implements PanacheRepository<Url> {

    /**
     * Find URL by short code
     */
    public Optional<Url> findByShortCode(String shortCode) {
        return find("shortCode", shortCode).firstResultOptional();
    }

    /**
     * Check if short code exists
     */
    public boolean existsByShortCode(String shortCode) {
        return count("shortCode", shortCode) > 0;
    }

    /**
     * Find all URLs by user ID
     */
    public List<Url> findByUserId(UUID userId) {
        return find("userId", userId).list();
    }

    /**
     * Find URLs by user ID with pagination and sorting
     */
    public List<Url> findByUserId(UUID userId, int page, int size, String sortBy, String order) {
        Sort.Direction direction = "asc".equalsIgnoreCase(order)
                ? Sort.Direction.Ascending
                : Sort.Direction.Descending;

        return find("userId", Sort.by(sortBy).direction(direction), userId)
                .page(Page.of(page - 1, size))
                .list();
    }

    /**
     * Count URLs by user ID
     */
    public long countByUserId(UUID userId) {
        return count("userId", userId);
    }

    /**
     * Find active URLs by user ID
     */
    public List<Url> findActiveByUserId(UUID userId) {
        return find("userId = ?1 and isActive = true", userId).list();
    }

    /**
     * Find expired URLs
     */
    public List<Url> findExpiredUrls() {
        return find("expiresAt < ?1 and isActive = true", Instant.now()).list();
    }

    /**
     * Update last accessed timestamp
     */
    public void updateLastAccessed(Long urlId, Instant timestamp) {
        update("lastAccessedAt = ?1 where id = ?2", timestamp, urlId);
    }

    /**
     * Increment clicks counter
     */
    public void incrementClicks(Long urlId) {
        update("clicks = clicks + 1 where id = ?1", urlId);
    }

    /**
     * Soft delete (set isActive = false)
     */
    public void softDelete(Long urlId) {
        update("isActive = false where id = ?1", urlId);
    }

    /**
     * Find URLs created between dates
     */
    public List<Url> findByDateRange(UUID userId, Instant startDate, Instant endDate) {
        return find("userId = ?1 and createdAt between ?2 and ?3", userId, startDate, endDate).list();
    }

    /**
     * Find top URLs by clicks
     */
    public List<Url> findTopByClicks(int limit) {
        return find("ORDER BY clicks DESC")
                .page(Page.ofSize(limit))
                .list();
    }

    /**
     * Find URLs by user ID and search term (title or short code)
     */
    public List<Url> searchByUserIdAndTerm(UUID userId, String searchTerm, int page, int size) {
        String query = "userId = ?1 and (lower(title) like ?2 or lower(shortCode) like ?2)";
        String term = "%" + searchTerm.toLowerCase() + "%";

        return find(query, userId, term)
                .page(Page.of(page - 1, size))
                .list();
    }
}