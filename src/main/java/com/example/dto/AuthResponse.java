package com.example.dto;

import lombok.*;

/**
 * Response DTO for authentication
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    public String accessToken;

    public String refreshToken;

    public String tokenType = "Bearer";

    public Integer expiresIn;

    public String userId;

    public String username;

    public String email;
}
