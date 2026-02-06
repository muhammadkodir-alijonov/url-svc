package com.example.service;

import com.example.domain.Url;
import com.example.dto.*;
import io.smallrye.mutiny.Uni;

import java.util.UUID;

/**
 * Interface for URL shortening operations
 */
public interface IUrlService {

    /**
     * Shorten a URL
     *
     * @param request URL shortening request
     * @return ShortenResponse containing the shortened URL
     */
    ShortenResponse shorten(ShortenRequest request);

    /**
     * Get original URL by short code (for redirect)
     *
     * @param shortCode the short code
     * @return Uni containing the original URL
     */
    Uni<String> redirect(String shortCode);

    /**
     * Get URL details by short code
     *
     * @param shortCode the short code
     * @return UrlResponse with URL details
     */
    UrlResponse getUrl(String shortCode);

    /**
     * List URLs for current user
     *
     * @param page page number (0-indexed)
     * @param size page size
     * @param sortBy sort field
     * @return UrlListResponse containing URLs
     */
    UrlListResponse listUrls(int page, int size, String sortBy);

    /**
     * Update URL
     *
     * @param shortCode the short code
     * @param request update request
     * @return updated UrlResponse
     */
    UrlResponse updateUrl(String shortCode, UpdateUrlRequest request);

    /**
     * Delete URL
     *
     * @param shortCode the short code
     */
    void deleteUrl(String shortCode);

    /**
     * Generate QR code for URL
     *
     * @param shortCode the short code
     * @return byte array containing QR code image
     */
    byte[] generateQRCode(String shortCode);

    /**
     * Get URL analytics/statistics
     *
     * @param shortCode the short code
     * @return Url entity with analytics
     */
    Url getUrlAnalytics(String shortCode);
}
