package com.example.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request DTO for user registration/login
 */
public class AuthRequest {

    public String username;

    public String email;

    public String password;

    public String firstName;

    public String lastName;
}
