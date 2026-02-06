package com.example.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request DTO for user registration/login
 */
public class AuthRequest {

    @JsonProperty("username")
    public String username;

    @JsonProperty("email")
    public String email;

    @JsonProperty("password")
    public String password;

    @JsonProperty("firstName")
    public String firstName;

    @JsonProperty("lastName")
    public String lastName;
}
