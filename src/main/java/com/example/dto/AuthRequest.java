package com.example.dto;

import lombok.*;

/**
 * Request DTO for user registration
 * All fields required for creating a new user
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthRequest {

    public String username;

    public String email;

    public String password;

    public String firstName;

    public String lastName;
}
