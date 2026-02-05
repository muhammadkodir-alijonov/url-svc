package com.example.service;

import io.quarkus.redis.datasource.ReactiveRedisDataSource;
import io.quarkus.redis.datasource.value.ReactiveValueCommands;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.time.Duration;
import java.util.Optional;

@ApplicationScoped
public class CacheService {

    private static final Logger LOG = Logger.getLogger(CacheService.class);

    @Inject
    ReactiveRedisDataSource redisDataSource;

    private ReactiveValueCommands<String, String> valueCommands;

    @PostConstruct
    void init() {
        valueCommands = redisDataSource.value(String.class, String.class);
    }

    /**
     * Get value from cache
     */
    public Optional<String> get(String key) {
        try {
            String value = valueCommands.get(key).await().indefinitely();
            if (value != null) {
                LOG.debugf("Cache HIT: %s", key);
                return Optional.of(value);
            } else {
                LOG.debugf("Cache MISS: %s", key);
                return Optional.empty();
            }
        } catch (Exception e) {
            LOG.errorf("Cache GET error for key %s: %s", key, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Set value in cache with TTL
     */
    public void set(String key, String value, Duration ttl) {
        try {
            valueCommands.setex(key, ttl.getSeconds(), value)
                    .await().indefinitely();
            LOG.debugf("Cache SET: %s (TTL: %d seconds)", key, ttl.getSeconds());
        } catch (Exception e) {
            LOG.errorf("Cache SET error for key %s: %s", key, e.getMessage());
        }
    }

    /**
     * Set value in cache without TTL (permanent)
     */
    public void set(String key, String value) {
        try {
            valueCommands.set(key, value)
                    .await().indefinitely();
            LOG.debugf("Cache SET (no TTL): %s", key);
        } catch (Exception e) {
            LOG.errorf("Cache SET error for key %s: %s", key, e.getMessage());
        }
    }

    /**
     * Delete key from cache
     */
    public void delete(String key) {
        try {
            redisDataSource.key().del(key)
                    .await().indefinitely();
            LOG.debugf("Cache DELETE: %s", key);
        } catch (Exception e) {
            LOG.errorf("Cache DELETE error for key %s: %s", key, e.getMessage());
        }
    }

    /**
     * Increment counter
     */
    public Long increment(String key) {
        try {
            return valueCommands.incr(key).await().indefinitely();
        } catch (Exception e) {
            LOG.errorf("Cache INCR error for key %s: %s", key, e.getMessage());
            return 0L;
        }
    }

    /**
     * Check if key exists
     */
    public boolean exists(String key) {
        try {
            return redisDataSource.key().exists(key).await().indefinitely();
        } catch (Exception e) {
            LOG.errorf("Cache EXISTS error for key %s: %s", key, e.getMessage());
            return false;
        }
    }

    /**
     * Cache keys for URL mapping
     */
    public static String urlCacheKey(String shortCode) {
        return "url:" + shortCode;
    }

    /**
     * Cache keys for click counters
     */
    public static String clickCounterKey(String shortCode) {
        return "clicks:" + shortCode;
    }

    /**
     * Cache keys for rate limiting
     */
    public static String rateLimitKey(String userId, String action) {
        return "ratelimit:" + userId + ":" + action;
    }

    /**
     * Cache keys for analytics
     */
    public static String analyticsCacheKey(String shortCode, String metric) {
        return "analytics:" + shortCode + ":" + metric;
    }
}