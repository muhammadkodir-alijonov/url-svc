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

    public Optional<Url> findByShortCode(String shortCode) {
        return find("shortCode", shortCode).firstResultOptional();
    }

    public boolean existsByShortCode(String shortCode) {
        return count("shortCode", shortCode) > 0;
    }

    public List<Url> findByUserId(UUID userId) {
        return find("userId", userId).list();
    }

    public List<Url> findByUserId(UUID userId, int page, int size, String sortBy, String order) {
        Sort.Direction direction = "asc".equalsIgnoreCase(order)
                ? Sort.Direction.Ascending
                : Sort.Direction.Descending;

        return find("userId", Sort.by(sortBy).direction(direction), userId)
                .page(Page.of(page - 1, size))
                .list();
    }

    public long countByUserId(UUID userId) {
        return count("userId", userId);
    }

    public List<Url> findActiveByUserId(UUID userId) {
        return find("userId = ?1 and isActive = true", userId).list();
    }

    public List<Url> findExpiredUrls() {
        return find("expiresAt < ?1 and isActive = true", Instant.now()).list();
    }

    public void updateLastAccessed(Long urlId, Instant timestamp) {
        update("lastAccessedAt = ?1 where id = ?2", timestamp, urlId);
    }

    public void incrementClicks(Long urlId) {
        update("clicks = clicks + 1 where id = ?1", urlId);
    }

    public void softDelete(Long urlId) {
        update("isActive = false where id = ?1", urlId);
    }

    public List<Url> findByDateRange(UUID userId, Instant startDate, Instant endDate) {
        return find("userId = ?1 and createdAt between ?2 and ?3", userId, startDate, endDate).list();
    }

    public List<Url> findTopByClicks(int limit) {
        return find("ORDER BY clicks DESC")
                .page(Page.ofSize(limit))
                .list();
    }

    public List<Url> searchByUserIdAndTerm(UUID userId, String searchTerm, int page, int size) {
        String query = "userId = ?1 and (lower(title) like ?2 or lower(shortCode) like ?2)";
        String term = "%" + searchTerm.toLowerCase() + "%";

        return find(query, userId, term)
                .page(Page.of(page - 1, size))
                .list();
    }
}