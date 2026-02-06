package com.example.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response DTO for authentication
 */
public class AuthResponse {

    @JsonProperty("access_token")
    public String accessToken;

    @JsonProperty("refresh_token")
    public String refreshToken;

    @JsonProperty("token_type")
    public String tokenType = "Bearer";

    @JsonProperty("expires_in")
    public Integer expiresIn;

    @JsonProperty("user_id")
    public String userId;

    @JsonProperty("username")
    public String username;

    @JsonProperty("email")
    public String email;

    public AuthResponse() {
    }

    public AuthResponse(String accessToken, String refreshToken, Integer expiresIn) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
    }
}
