package com.example.dto;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UrlResponse {
    private Long id;
    private String shortCode;
    private String shortUrl;
    private String originalUrl;
    private String title;
    private Integer clicks;
    private Boolean hasPassword;
    private Instant expiresAt;
    private Boolean isActive;
    private Boolean isCustom;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant lastAccessedAt;
}
