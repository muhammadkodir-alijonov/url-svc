package com.example.config;

import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.keys.KeyCommands;
import io.quarkus.redis.datasource.value.ValueCommands;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;

/**
 * Redis/Valkey configuration
 *
 * Pre-configured command objects for easier use
 */
@ApplicationScoped
public class RedisConfig {

    @Inject
    RedisDataSource redisDataSource;

    /**
     * String value commands
     */
    @Produces
    @ApplicationScoped
    public ValueCommands<String, String> stringCommands() {
        return redisDataSource.value(String.class);
    }

    /**
     * Long value commands (for counters)
     */
    @Produces
    @ApplicationScoped
    public ValueCommands<String, Long> longCommands() {
        return redisDataSource.value(Long.class);
    }

    /**
     * Key commands (for operations like DEL, EXISTS, etc.)
     */
    @Produces
    @ApplicationScoped
    public KeyCommands<String> keyCommands() {
        return redisDataSource.key(String.class);
    }
}