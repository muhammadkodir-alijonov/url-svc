package com.example.service.Impl;

import com.example.service.ICacheService;
import io.quarkus.redis.datasource.ReactiveRedisDataSource;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.value.ReactiveValueCommands;
import io.quarkus.redis.datasource.value.ValueCommands;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.time.Duration;
import java.util.Optional;

/**
 * Implementation of Redis caching operations
 */
@ApplicationScoped
public class CacheService implements ICacheService {

    private static final Logger LOG = Logger.getLogger(CacheService.class);

    @Inject
    RedisDataSource redisDataSource;

    @Inject
    ReactiveRedisDataSource reactiveRedisDataSource;

    private ValueCommands<String, String> stringCommands;
    private ValueCommands<String, Long> longCommands;
    private ReactiveValueCommands<String, String> valueCommands;

    @PostConstruct
    void init() {
        stringCommands = redisDataSource.value(String.class, String.class);
        longCommands = redisDataSource.value(String.class, Long.class);
        valueCommands = reactiveRedisDataSource.value(String.class, String.class);
    }

    @Override
    public Uni<String> getOriginalUrl(String shortCode) {
        String key = urlCacheKey(shortCode);
        return valueCommands.get(key)
                .onItem().ifNotNull().transform(url -> {
                    LOG.debugf("Cache HIT for short code: %s", shortCode);
                    return url;
                })
                .onItem().ifNull().continueWith(() -> {
                    LOG.debugf("Cache MISS for short code: %s", shortCode);
                    return null;
                });
    }

    @Override
    public Uni<Void> cacheOriginalUrl(String shortCode, String originalUrl, long ttlSeconds) {
        String key = urlCacheKey(shortCode);
        return valueCommands.setex(key, ttlSeconds, originalUrl)
                .replaceWithVoid()
                .invoke(() -> LOG.debugf("Cached URL: %s -> %s (TTL: %d)", shortCode, originalUrl, ttlSeconds));
    }

    @Override
    public Uni<Void> invalidateCache(String shortCode) {
        String key = urlCacheKey(shortCode);
        return reactiveRedisDataSource.key().del(key)
                .replaceWithVoid()
                .invoke(() -> LOG.debugf("Invalidated cache for: %s", shortCode));
    }

    @Override
    public Uni<Boolean> isRateLimitExceeded(String key, int limit, long windowSeconds) {
        String rateLimitKey = "ratelimit:" + key;
        return reactiveRedisDataSource.value(String.class, Long.class)
                .get(rateLimitKey)
                .onItem().transformToUni(count -> {
                    if (count == null) {
                        // First request - set counter to 1
                        return reactiveRedisDataSource.value(String.class, Long.class)
                                .setex(rateLimitKey, windowSeconds, 1L)
                                .replaceWith(false);
                    } else if (count >= limit) {
                        // Rate limit exceeded
                        return Uni.createFrom().item(true);
                    } else {
                        // Increment counter
                        return reactiveRedisDataSource.value(String.class, Long.class)
                                .incr(rateLimitKey)
                                .replaceWith(false);
                    }
                });
    }

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

    public void set(String key, String value, Duration ttl) {
        try {
            valueCommands.setex(key, ttl.getSeconds(), value)
                    .await().indefinitely();
            LOG.debugf("Cache SET: %s (TTL: %d seconds)", key, ttl.getSeconds());
        } catch (Exception e) {
            LOG.errorf("Cache SET error for key %s: %s", key, e.getMessage());
        }
    }

    public void set(String key, String value) {
        try {
            valueCommands.set(key, value)
                    .await().indefinitely();
            LOG.debugf("Cache SET (no TTL): %s", key);
        } catch (Exception e) {
            LOG.errorf("Cache SET error for key %s: %s", key, e.getMessage());
        }
    }

    public void delete(String key) {
        try {
            reactiveRedisDataSource.key().del(key)
                    .await().indefinitely();
            LOG.debugf("Cache DELETE: %s", key);
        } catch (Exception e) {
            LOG.errorf("Cache DELETE error for key %s: %s", key, e.getMessage());
        }
    }

    public void increment(String key) {
        try {
            valueCommands.incr(key).await().indefinitely();
        } catch (Exception e) {
            LOG.errorf("Cache INCR error for key %s: %s", key, e.getMessage());
        }
    }

    public boolean exists(String key) {
        try {
            return reactiveRedisDataSource.key().exists(key).await().indefinitely();
        } catch (Exception e) {
            LOG.errorf("Cache EXISTS error for key %s: %s", key, e.getMessage());
            return false;
        }
    }

    public static String urlCacheKey(String shortCode) {
        return "url:" + shortCode;
    }

    public static String clickCounterKey(String shortCode) {
        return "clicks:" + shortCode;
    }

    public static String rateLimitKey(String userId, String action) {
        return "ratelimit:" + userId + ":" + action;
    }


    public static String analyticsCacheKey(String shortCode, String metric) {
        return "analytics:" + shortCode + ":" + metric;
    }

    public void cacheUrl(String key, String url) {
        stringCommands.set(key, url);
    }

    public void incrementCounter(String key) {
        longCommands.incr(key);
    }
}