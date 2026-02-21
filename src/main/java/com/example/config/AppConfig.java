package com.example.config;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

@ConfigMapping(prefix = "app")
public interface AppConfig {
    @WithDefault("dev")
    String environment();

    @WithName("base-url")
    String baseUrl();

    ShortCodeConfig shortCode();

    CacheConfig cache();

    RateLimitConfig rateLimit();

    PulsarConfig pulsar();

    /**
     * Short code settings
     */
    interface ShortCodeConfig {

        int length();

        @WithName("max-attempts")
        int maxAttempts();
    }

    /**
     * Cache settings
     */
    interface CacheConfig {

        @WithName("url-ttl")
        int urlTtl();
    }

    /**
     * Rate limit settings
     */
    interface RateLimitConfig {

        int shorten();

        int redirect();
    }

    /**
     * Pulsar settings
     */
    @ConfigMapping(prefix = "app.pulsar")
    interface PulsarConfig {

        String topic();
    }
}