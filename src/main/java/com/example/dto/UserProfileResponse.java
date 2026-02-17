package com.example.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class UserProfileResponse {

    public String id;

    public String username;

    public String email;

    public String firstName;

    public String lastName;

    public String plan;

    public Integer linksCreated;

    public Integer linksLimit;

    public String createdAt;
}
