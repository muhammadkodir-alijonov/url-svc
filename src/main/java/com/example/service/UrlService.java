package com.example.service;

import com.example.config.AppConfig;
import com.example.config.SecurityConfig;
import com.example.domain.Url;
import com.example.domain.User;
import com.example.dto.*;
import com.example.exception.*;
import com.example.repository.UrlRepository;
import com.example.repository.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.logging.Logger;
import org.mindrot.jbcrypt.BCrypt;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class UrlService {

    private static final Logger LOG = Logger.getLogger(UrlService.class);
    private static final String BASE_URL = "https://shortener.by"; // TODO: config

    @Inject
    UrlRepository urlRepository;

    @Inject
    UserRepository userRepository;

    @Inject
    ShortCodeGenerator codeGenerator;

    @Inject
    CacheService cacheService;

    @Inject
    JsonWebToken jwt;

    @Inject
    AppConfig appConfig;

    /**
     * Shorten URL
     */
    @Transactional
    public ShortenResponse shorten(ShortenRequest request) {
        LOG.infof("Shortening URL: %s", request.getOriginalUrl());

        // 1. Get current user
        UUID userId = getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        // 2. Check user limits
        if (!user.canCreateLink()) {
            throw new LimitExceededException(
                    String.format("Link limit reached (%d/%d)",
                            user.linksCreated, user.linksLimit)
            );
        }

        // 3. Validate original URL
        if (!codeGenerator.isValidUrl(request.getOriginalUrl())) {
            throw new InvalidUrlException("Invalid URL format");
        }

        // 4. Generate or validate short code
        String shortCode;
        boolean isCustom = false;

        if (request.getCustomAlias() != null && !request.getCustomAlias().isEmpty()) {
            // Custom alias
            String alias = request.getCustomAlias();

            if (!codeGenerator.isValidCustomAlias(alias)) {
                throw new InvalidAliasException("Invalid custom alias format");
            }

            if (codeGenerator.isReserved(alias)) {
                throw new InvalidAliasException("This alias is reserved");
            }

            if (urlRepository.existsByShortCode(alias)) {
                throw new ShortCodeTakenException("This alias is already taken");
            }

            shortCode = alias;
            isCustom = true;
            LOG.infof("Using custom alias: %s", alias);

        } else {
            // Auto-generate
            shortCode = codeGenerator.generateUnique();
            LOG.infof("Generated short code: %s", shortCode);
        }

        // 5. Hash password if provided
        String passwordHash = null;
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            passwordHash = BCrypt.hashpw(request.getPassword(), BCrypt.gensalt());
            LOG.debug("Password protection enabled");
        }

        // 6. Create URL entity
        Url url = new Url();
        url.shortCode = shortCode;
        url.originalUrl = request.getOriginalUrl();
        url.userId = userId;
        url.passwordHash = passwordHash;
        url.title = request.getTitle();
        url.expiresAt = request.getExpiresAt();
        url.isCustom = isCustom;
        url.isActive = true;
        url.clicks = 0;

        // 7. Persist to database
        urlRepository.persist(url);

        // 8. Update user stats
        user.incrementLinksCreated();
        userRepository.persist(user);

        LOG.infof("URL shortened successfully: %s -> %s", url.originalUrl, url.shortCode);

        // 9. Build and return response
        return ShortenResponse.builder()
                .id(url.id)
                .shortCode(shortCode)
                .shortUrl(buildShortUrl(shortCode))
                .originalUrl(url.originalUrl)
                .qrCodeUrl(buildQrCodeUrl(shortCode))
                .title(url.title)
                .clicks(0)
                .hasPassword(url.hasPassword())
                .expiresAt(url.expiresAt)
                .createdAt(url.createdAt)
                .build();
    }

    /**
     * Get URL details
     */
    public UrlResponse getUrl(String shortCode) {
        LOG.debugf("Getting URL details: %s", shortCode);

        Url url = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new UrlNotFoundException("URL not found: " + shortCode));
        // Check ownership
        UUID currentUserId = getCurrentUserId();
        if (!url.userId.equals(currentUserId)) {
            throw new UnauthorizedAccessException("You don't own this URL");
        }

        return mapToResponse(url);
    }

    /**
     * List user's URLs with pagination
     */
    public UrlListResponse listUrls(int page, int size, String sortBy, String order) {
        UUID userId = getCurrentUserId();
        LOG.debugf("Listing URLs for user: %s (page: %d, size: %d)", userId, page, size);

        // Validate pagination
        if (page < 1) page = 1;
        if (size < 1 || size > 100) size = 20;

        // Get URLs
        List<Url> urls = urlRepository.findByUserId(userId, page, size, sortBy, order);
        long total = urlRepository.countByUserId(userId);

        List<UrlResponse> responses = urls.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return UrlListResponse.builder()
                .data(responses)
                .page(page)
                .size(size)
                .total(total)
                .totalPages((int) Math.ceil((double) total / size))
                .build();
    }

    /**
     * Update URL
     */
    @Transactional
    public UrlResponse updateUrl(String shortCode, UpdateUrlRequest request) {
        LOG.infof("Updating URL: %s", shortCode);

        Url url = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new UrlNotFoundException("URL not found: " + shortCode));

        // Check ownership
        UUID currentUserId = getCurrentUserId();
        if (!url.userId.equals(currentUserId)) {
            throw new UnauthorizedAccessException("You don't own this URL");
        }

        // Update fields
        boolean cacheInvalidated = false;

        if (request.getOriginalUrl() != null) {
            if (!codeGenerator.isValidUrl(request.getOriginalUrl())) {
                throw new InvalidUrlException("Invalid URL format");
            }
            url.originalUrl = request.getOriginalUrl();
            cacheInvalidated = true;
        }

        if (request.getTitle() != null) {
            url.title = request.getTitle();
        }

        if (request.getExpiresAt() != null) {
            url.expiresAt = request.getExpiresAt();
        }

        if (request.getIsActive() != null) {
            url.isActive = request.getIsActive();
            cacheInvalidated = true;
        }

        // Persist changes
        urlRepository.persist(url);

        // Invalidate cache if URL or status changed
        if (cacheInvalidated) {
            cacheService.delete(CacheService.urlCacheKey(shortCode));
            LOG.debug("Cache invalidated for: " + shortCode);
        }

        LOG.infof("URL updated successfully: %s", shortCode);

        return mapToResponse(url);
    }

    /**
     * Delete URL
     */
    @Transactional
    public void deleteUrl(String shortCode) {
        LOG.infof("Deleting URL: %s", shortCode);

        Url url = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new UrlNotFoundException("URL not found: " + shortCode));

        // Check ownership
        UUID currentUserId = getCurrentUserId();
        if (!url.userId.equals(currentUserId)) {
            throw new UnauthorizedAccessException("You don't own this URL");
        }

        // Soft delete
        url.isActive = false;
        urlRepository.persist(url);

        // Invalidate cache
        cacheService.delete(CacheService.urlCacheKey(shortCode));

        // Update user stats
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));;
        if (user != null) {
            user.decrementLinksCreated();
            userRepository.persist(user);
        }

        LOG.infof("URL deleted successfully: %s", shortCode);
    }

    // ============================================
    // HELPER METHODS
    // ============================================

    /**
     * Get current user ID from JWT
     */
    private UUID getCurrentUserId() {
        String keycloakId = jwt.getSubject();
        return userRepository.findByKeycloakId(keycloakId)
                .map(user -> user.id)
                .orElseThrow(() -> new UnauthorizedAccessException("User not found in database"));
    }

    /**
     * Build short URL
     */
    private String buildShortUrl(String shortCode) {
        return appConfig.baseUrl() + "/" + shortCode;
    }

    /**
     * Build QR code URL
     */
    private String buildQrCodeUrl(String shortCode) {
        return BASE_URL + "/api/urls/" + shortCode + "/qr";
    }

    /**
     * Map entity to response DTO
     */
    private UrlResponse mapToResponse(Url url) {
        return UrlResponse.builder()
                .id(url.id)
                .shortCode(url.shortCode)
                .shortUrl(buildShortUrl(url.shortCode))
                .originalUrl(url.originalUrl)
                .title(url.title)
                .clicks(url.clicks)
                .hasPassword(url.hasPassword())
                .expiresAt(url.expiresAt)
                .isActive(url.isActive)
                .isCustom(url.isCustom)
                .createdAt(url.createdAt)
                .updatedAt(url.updatedAt)
                .lastAccessedAt(url.lastAccessedAt)
                .build();
    }
    /**
     * Check password
     */
    public boolean checkPassword(String input, String hash) {
        return SecurityConfig.SecurityHelper.verifyPassword(input, hash);
    }
}