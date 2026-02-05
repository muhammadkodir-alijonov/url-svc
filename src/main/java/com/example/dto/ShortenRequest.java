package com.example.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ShortenRequest {
    @NotBlank(message = "Original URL is required")
    @Pattern(regexp = "^https?://.*", message = "URL must start with http:// or https://")
    private String originalUrl;

    @Size(min = 4, max = 10, message = "Custom alias must be between 4 and 10 characters")
    @Pattern(regexp = "^[a-zA-Z0-9-]*$", message = "Only letters, numbers, and hyphens allowed")
    private String customAlias;

    @Size(max = 255, message = "Title cannot exceed 255 characters")
    private String title;

    private String password;

    private Instant expiresAt;
}