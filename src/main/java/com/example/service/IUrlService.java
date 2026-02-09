package com.example.service;

import com.example.domain.Url;
import com.example.dto.*;
import io.smallrye.mutiny.Uni;

import java.util.UUID;

/**
 * Interface for URL shortening operations
 */
public interface IUrlService {

    ShortenResponse shorten(ShortenRequest request);

    Uni<String> redirect(String shortCode);

    UrlResponse getUrl(String shortCode);

    UrlListResponse listUrls(int page, int size, String sortBy);

    UrlResponse updateUrl(String shortCode, UpdateUrlRequest request);

    void deleteUrl(String shortCode);

    byte[] generateQRCode(String shortCode);

    Url getUrlAnalytics(String shortCode);
}
