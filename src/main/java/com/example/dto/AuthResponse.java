package com.example.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response DTO for authentication
 */
public class AuthResponse {

    public String accessToken;

    public String refreshToken;

    public String tokenType = "Bearer";

    public Integer expiresIn;

    public String userId;

    public String username;

    public String email;

    public AuthResponse() {
    }

    public AuthResponse(String accessToken, String refreshToken, Integer expiresIn) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
    }
}
