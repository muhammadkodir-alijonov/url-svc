package com.example.config;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithName;

/**
 * Application configuration
 *
 * Maps to 'app.*' properties in application.yml
 */
@ConfigMapping(prefix = "app")
public interface AppConfig {

    /**
     * Base URL for short links
     *
     * application.yml:
     *   app.base-url: https://shortener.by
     */
    @WithName("base-url")
    String baseUrl();

    /**
     * Short code configuration
     */
    ShortCodeConfig shortCode();

    /**
     * Cache configuration
     */
    CacheConfig cache();

    /**
     * Rate limit configuration
     */
    RateLimitConfig rateLimit();

    /**
     * Short code settings
     */
    interface ShortCodeConfig {

        /**
         * Length of generated short codes
         * Default: 6
         */
        int length();

        /**
         * Max attempts to generate unique code
         * Default: 10
         */
        @WithName("max-attempts")
        int maxAttempts();
    }

    /**
     * Cache settings
     */
    interface CacheConfig {

        /**
         * URL cache TTL in seconds
         * Default: 3600 (1 hour)
         */
        @WithName("url-ttl")
        int urlTtl();
    }

    /**
     * Rate limit settings
     */
    interface RateLimitConfig {

        /**
         * Max shorten requests per minute per user
         * Default: 10
         */
        int shorten();

        /**
         * Max redirect requests per minute per IP
         * Default: 500
         */
        int redirect();
    }
}