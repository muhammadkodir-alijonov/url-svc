package com.example.config;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

@ConfigMapping(prefix = "app")
public interface AppConfig {
    @WithDefault("dev")
    String environment();

    @WithName("base-url")
    @WithDefault("http://localhost:3000")
    String baseUrl();

    ShortCodeConfig shortCode();

    CacheConfig cache();

    RateLimitConfig rateLimit();

    PulsarConfig pulsar();

    /**
     * Short code settings
     */
    interface ShortCodeConfig {

        @WithDefault("7")
        int length();

        @WithName("max-attempts")
        @WithDefault("10")
        int maxAttempts();
    }

    /**
     * Cache settings
     */
    interface CacheConfig {

        @WithName("url-ttl")
        @WithDefault("3600")
        int urlTtl();
    }

    /**
     * Rate limit settings
     */
    interface RateLimitConfig {

        @WithDefault("100")
        int shorten();

        @WithDefault("1000")
        int redirect();
    }

    /**
     * Pulsar settings
     */
    interface PulsarConfig {

        @WithDefault("url-shorten-clicks")
        String topic();
    }
}