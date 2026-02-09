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
 */
@ApplicationScoped
public class RedisConfig {

    @Inject
    RedisDataSource redisDataSource;

    @Inject
    ReactiveRedisDataSource reactiveRedisDataSource;

    @Produces
    @ApplicationScoped
    @Named("reactive")
    public ReactiveRedisDataSource reactiveDataSource() {
        return reactiveRedisDataSource;
    }

    @Produces
    @ApplicationScoped
    @Named("stringCommands")
    public ValueCommands<String, String> stringCommands() {
        return redisDataSource.value(String.class, String.class);
    }

    @Produces
    @ApplicationScoped
    @Named("longCommands")
    public ValueCommands<String, Long> longCommands() {
        return redisDataSource.value(String.class, Long.class);
    }

    @Produces
    @ApplicationScoped
    public KeyCommands<String> keyCommands() {
        return redisDataSource.key(String.class);
    }
}