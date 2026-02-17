package com.example.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponse {

    public String accessToken;

    public String refreshToken;

    public String tokenType = "Bearer";

    public Integer expiresIn;

    // Nested user info (optional - only in login/register responses)
    public UserInfo user;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserInfo {
        public String id;
        public String username;
        // Email is in JWT token, no need to expose here
    }
}
