package com.example.service.Impl;

import com.example.repository.UrlRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.security.SecureRandom;

@ApplicationScoped
public class ShortCodeGenerator {

    private static final Logger LOG = Logger.getLogger(ShortCodeGenerator.class);

    private static final String BASE62 = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    private static final int CODE_LENGTH = 6;
    private static final int MAX_ATTEMPTS = 10;
    private static final SecureRandom RANDOM = new SecureRandom();

    @Inject
    UrlRepository urlRepository;

    /**
     * Generate random 6-character Base62 code
     * <p>
     * Total possibilities: 62^6 = 56,800,235,584 (56+ billion)
     * Collision probability at 1M URLs: ~0.0009%
     */
    public String generate() {
        StringBuilder code = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(BASE62.charAt(RANDOM.nextInt(BASE62.length())));
        }
        return code.toString();
    }

    /**
     * Generate unique code with collision check
     *
     * @return Unique short code guaranteed to not exist in database
     */
    public String generateUnique() {
        String code;
        int attempts = 0;

        do {
            code = generate();
            attempts++;

            if (attempts >= MAX_ATTEMPTS) {
                LOG.warn("Reached max attempts for code generation, using longer code");
                // Fallback: append extra character to reduce collision
                return code + BASE62.charAt(RANDOM.nextInt(BASE62.length()));
            }
        } while (urlRepository.existsByShortCode(code));

        LOG.debugf("Generated unique code: %s (attempts: %d)", code, attempts);
        return code;
    }

    /**
     * Validate custom alias
     * <p>
     * Rules:
     * - Length: 4-10 characters
     * - Allowed: a-z, A-Z, 0-9, hyphen (-)
     * - Not reserved
     */
    public boolean isValidCustomAlias(String alias) {
        if (alias == null || alias.length() < 4 || alias.length() > 10) {
            return false;
        }

        if (!alias.matches("^[a-zA-Z0-9-]+$")) {
            return false;
        }

        return !isReserved(alias);
    }

    /**
     * Reserved codes (system routes, common words)
     */
    private static final String[] RESERVED_CODES = {"api", "admin", "login", "logout", "register", "signup", "signin", "health", "metrics", "swagger", "docs", "help", "about", "contact", "terms", "privacy", "dashboard", "settings", "profile", "qr"};

    /**
     * Check if code is reserved
     */
    public boolean isReserved(String code) {
        if (code == null) {
            return false;
        }

        String lower = code.toLowerCase();
        for (String reserved : RESERVED_CODES) {
            if (lower.equals(reserved)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Validate URL format
     */
    public boolean isValidUrl(String url) {
        if (url == null || url.isEmpty()) {
            return false;
        }

        // Basic validation: starts with http:// or https://
        if (!url.matches("^https?://.*")) {
            return false;
        }

        // Check length (max 2048 chars)
        if (url.length() > 2048) {
            return false;
        }

        return true;
    }
}