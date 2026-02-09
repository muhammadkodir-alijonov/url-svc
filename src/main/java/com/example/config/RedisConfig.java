package com.example.config;

import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.keys.KeyCommands;
import io.quarkus.redis.datasource.value.ValueCommands;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Named;

/**
 * Redis/Valkey configuration
 */
@ApplicationScoped
public class RedisConfig {

    @Produces
    @ApplicationScoped
    @Named("stringCommands")
    public ValueCommands<String, String> stringCommands(RedisDataSource redisDataSource) {
        return redisDataSource.value(String.class, String.class);
    }

    @Produces
    @ApplicationScoped
    @Named("longCommands")
    public ValueCommands<String, Long> longCommands(RedisDataSource redisDataSource) {
        return redisDataSource.value(String.class, Long.class);
    }

    @Produces
    @ApplicationScoped
    public KeyCommands<String> keyCommands(RedisDataSource redisDataSource) {
        return redisDataSource.key(String.class);
    }
}