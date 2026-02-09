package com.example.dto;

import lombok.*;

/**
 * Request DTO for user login
 * Only username and password required
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequest {

    public String username;

    public String password;
}
