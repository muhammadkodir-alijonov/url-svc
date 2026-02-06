package com.example.service;

import io.smallrye.mutiny.Uni;

/**
 * Interface for caching operations
 */
public interface ICacheService {

    /**
     * Get cached original URL by short code
     *
     * @param shortCode the short code
     * @return Uni containing the original URL or null if not found
     */
    Uni<String> getOriginalUrl(String shortCode);

    /**
     * Cache original URL with short code
     *
     * @param shortCode the short code
     * @param originalUrl the original URL to cache
     * @param ttlSeconds time to live in seconds
     * @return Uni<Void> completion signal
     */
    Uni<Void> cacheOriginalUrl(String shortCode, String originalUrl, long ttlSeconds);

    /**
     * Invalidate cache for a short code
     *
     * @param shortCode the short code to invalidate
     * @return Uni<Void> completion signal
     */
    Uni<Void> invalidateCache(String shortCode);

    /**
     * Check if rate limit is exceeded
     *
     * @param key the rate limit key (e.g., userId or IP)
     * @param limit the maximum number of requests allowed
     * @param windowSeconds the time window in seconds
     * @return Uni<Boolean> true if rate limit is exceeded, false otherwise
     */
    Uni<Boolean> isRateLimitExceeded(String key, int limit, long windowSeconds);
}
