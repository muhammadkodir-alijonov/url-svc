package com.example.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateUrlRequest {
    @Pattern(regexp = "^https?://.*", message = "URL must start with http:// or https://")
    private String originalUrl;

    @Size(max = 255, message = "Title cannot exceed 255 characters")
    private String title;

    private Instant expiresAt;

    private Boolean isActive;
}
