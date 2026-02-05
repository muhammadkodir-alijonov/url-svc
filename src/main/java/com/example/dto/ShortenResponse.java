package com.example.dto;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShortenResponse {
    private Long id;
    private String shortCode;
    private String shortUrl;
    private String originalUrl;
    private String qrCodeUrl;
    private String title;
    private Integer clicks;
    private Boolean hasPassword;
    private Instant expiresAt;
    private Instant createdAt;
}
