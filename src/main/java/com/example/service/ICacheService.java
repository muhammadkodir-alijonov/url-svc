package com.example.service;

import io.smallrye.mutiny.Uni;

/**
 * Interface for caching operations
 */
public interface ICacheService {

    Uni<String> getOriginalUrl(String shortCode);

    Uni<Void> cacheOriginalUrl(String shortCode, String originalUrl, long ttlSeconds);

    Uni<Void> invalidateCache(String shortCode);

    Uni<Boolean> isRateLimitExceeded(String key, int limit, long windowSeconds);
}
