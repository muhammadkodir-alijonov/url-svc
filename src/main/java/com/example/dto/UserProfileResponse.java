package com.example.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * User profile response DTO
 */
public class UserProfileResponse {

    @JsonProperty("id")
    public String id;

    @JsonProperty("username")
    public String username;

    @JsonProperty("email")
    public String email;

    @JsonProperty("first_name")
    public String firstName;

    @JsonProperty("last_name")
    public String lastName;

    @JsonProperty("plan")
    public String plan;

    @JsonProperty("links_created")
    public Integer linksCreated;

    @JsonProperty("links_limit")
    public Integer linksLimit;

    @JsonProperty("created_at")
    public String createdAt;
}
