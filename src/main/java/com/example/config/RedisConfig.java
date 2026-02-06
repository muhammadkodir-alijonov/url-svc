package com.example.config;

import io.quarkus.redis.datasource.ReactiveRedisDataSource;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.keys.KeyCommands;
import io.quarkus.redis.datasource.value.ValueCommands;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 * Redis/Valkey configuration
 *
 * Pre-configured command objects for easier use
 */
@ApplicationScoped
public class RedisConfig {

    @Inject
    RedisDataSource redisDataSource;

    @Inject
    ReactiveRedisDataSource reactiveRedisDataSource;

    /**
     * Reactive Redis DataSource for async operations
     */
    @Produces
    @ApplicationScoped
    @Named("reactive")
    public ReactiveRedisDataSource reactiveDataSource() {
        return reactiveRedisDataSource;
    }

    /**
     * String value commands
     */
    @Produces
    @ApplicationScoped
    @Named("stringCommands")
    public ValueCommands<String, String> stringCommands() {
        return redisDataSource.value(String.class, String.class);
    }

    /**
     * Long value commands (for counters)
     */
    @Produces
    @ApplicationScoped
    @Named("longCommands")
    public ValueCommands<String, Long> longCommands() {
        return redisDataSource.value(String.class, Long.class);
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