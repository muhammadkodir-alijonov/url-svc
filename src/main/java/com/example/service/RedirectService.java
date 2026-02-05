package com.example.service;

import com.example.domain.Url;
import com.example.dto.ClickEvent;
import com.example.exception.InvalidPasswordException;
import com.example.exception.PasswordRequiredException;
import com.example.exception.UrlExpiredException;
import com.example.exception.UrlNotFoundException;
import com.example.repository.UrlRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;
import org.mindrot.jbcrypt.BCrypt;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@ApplicationScoped
public class RedirectService {

    private static final Logger LOG = Logger.getLogger(RedirectService.class);
    private static final Duration CACHE_TTL = Duration.ofHours(1);

    @Inject
    UrlRepository urlRepository;

    @Inject
    CacheService cacheService;

    @Inject
    EventPublisher eventPublisher;

    /**
     * Resolve short code to original URL (HOT PATH - PERFORMANCE CRITICAL!)
     * <p>
     * Flow:
     * 1. Check Valkey cache (< 5ms)
     * 2. If miss → query database (10-50ms)
     * 3. Validate (expiration, password, active)
     * 4. Cache result for future requests
     * 5. Increment counter (async)
     * 6. Publish analytics event (async)
     * 7. Return original URL
     */
    public String resolveShortCode(String shortCode, String password,
                                   String ipAddress, String userAgent, String referer) {
        LOG.debugf("Resolving short code: %s", shortCode);

        // STEP 1: Check cache (HOT PATH - most requests end here!)
        Optional<String> cachedUrl = cacheService.get(CacheService.urlCacheKey(shortCode));

        if (cachedUrl.isPresent()) {
            LOG.debugf("Cache HIT for: %s", shortCode);

            // Async operations (don't block redirect!)
            incrementClickCounterAsync(shortCode);
            publishClickEventAsync(shortCode, null, ipAddress, userAgent, referer);

            return cachedUrl.get();
        }

        // STEP 2: Cache miss - query database
        LOG.debugf("Cache MISS for: %s, querying database", shortCode);

        Url url = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new UrlNotFoundException("Short URL not found: " + shortCode));

        if (!url.isActive) {
            throw new UrlNotFoundException("This link has been disabled");
        }

        if (url.isExpired()) {
            throw new UrlExpiredException("This link has expired");
        }

        if (url.hasPassword()) {
            if (password == null || password.isEmpty()) {
                throw new PasswordRequiredException("Password required");
            }

            if (!BCrypt.checkpw(password, url.passwordHash)) {
                throw new InvalidPasswordException("Incorrect password");
            }
        }
        // STEP 3: Validate URL
        validateUrl(url, password);

        // STEP 4: Cache for future requests
        cacheService.set(
                CacheService.urlCacheKey(shortCode),
                url.originalUrl,
                CACHE_TTL
        );

        // STEP 5: Async operations
        incrementClickCounterAsync(shortCode);
        publishClickEventAsync(shortCode, url.id, ipAddress, userAgent, referer);
        updateLastAccessedAsync(url.id);

        LOG.infof("Resolved: %s -> %s", shortCode, url.originalUrl);

        return url.originalUrl;
    }

    /**
     * Validate URL (active, not expired, password check)
     */
    private void validateUrl(Url url, String password) {
        // Check if active
        if (!url.isActive) {
            LOG.warnf("Inactive URL accessed: %s", url.shortCode);
            throw new UrlNotFoundException("This link has been disabled");
        }

        // Check expiration
        if (url.isExpired()) {
            LOG.warnf("Expired URL accessed: %s (expired at: %s)",
                    url.shortCode, url.expiresAt);
            throw new UrlExpiredException("This link has expired");
        }

        // Check password protection
        if (url.hasPassword()) {
            if (password == null || password.isEmpty()) {
                throw new PasswordRequiredException("Password required to access this link");
            }

            if (!BCrypt.checkpw(password, url.passwordHash)) {
                LOG.warnf("Invalid password for: %s", url.shortCode);
                throw new InvalidPasswordException("Incorrect password");
            }
        }
    }

    /**
     * Increment click counter in Valkey (async, non-blocking)
     */
    private void incrementClickCounterAsync(String shortCode) {
        CompletableFuture.runAsync(() -> {
            try {
                cacheService.increment(CacheService.clickCounterKey(shortCode));
            } catch (Exception e) {
                LOG.errorf("Failed to increment counter for %s: %s",
                        shortCode, e.getMessage());
            }
        });
    }

    /**
     * Publish click event to Pulsar (async, fire-and-forget)
     */
    private void publishClickEventAsync(String shortCode, Long urlId,
                                        String ip, String userAgent, String referer) {
        CompletableFuture.runAsync(() -> {
            try {
                ClickEvent event = ClickEvent.builder()
                        .shortCode(shortCode)
                        .urlId(urlId)
                        .ipAddress(ip)
                        .userAgent(userAgent)
                        .referer(referer)
                        .timestamp(Instant.now())
                        .build();

                eventPublisher.publishClickEvent(event);
            } catch (Exception e) {
                LOG.errorf("Failed to publish click event for %s: %s",
                        shortCode, e.getMessage());
            }
        });
    }

    /**
     * Update last_accessed_at timestamp (async)
     */
    private void updateLastAccessedAsync(Long urlId) {
        CompletableFuture.runAsync(() -> {
            try {
                urlRepository.updateLastAccessed(urlId, Instant.now());
            } catch (Exception e) {
                LOG.errorf("Failed to update last accessed for %d: %s",
                        urlId, e.getMessage());
            }
        });
    }

    /**
     * Background job: Sync click counters from Valkey to PostgreSQL
     * <p>
     * Runs every 1 minute
     */
    // TODO: Add @Scheduled annotation in next step
    @Transactional
    public void syncClickCounters() {
        LOG.info("Syncing click counters from Valkey to PostgreSQL");

        // This will be implemented when we add scheduled jobs
        // For now, counters stay in Valkey
    }
}