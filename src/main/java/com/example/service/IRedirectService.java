package com.example.service;

/**
 * Interface for redirect and URL resolution operations
 */
public interface IRedirectService {

    /**
     * Resolve short code to original URL and handle redirect logic
     *
     * This is a HOT PATH method - performance critical!
     * - Checks cache first (< 5ms)
     * - Falls back to database if needed (10-50ms)
     * - Validates expiration, password, active status
     * - Increments click counter (async)
     * - Publishes analytics events (async)
     *
     * @param shortCode the short code to resolve
     * @param password optional password for protected URLs
     * @param ipAddress client IP address for analytics
     * @param userAgent client user agent for analytics
     * @param referer referrer URL for analytics
     * @return original URL to redirect to
     * @throws com.example.exception.UrlNotFoundException if short code not found
     * @throws com.example.exception.UrlExpiredException if URL expired
     * @throws com.example.exception.PasswordRequiredException if password required but not provided
     * @throws com.example.exception.InvalidPasswordException if password incorrect
     */
    String resolveShortCode(String shortCode, String password,
                          String ipAddress, String userAgent, String referer);
}
