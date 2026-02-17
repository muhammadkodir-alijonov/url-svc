package com.example.dto;

import lombok.*;

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
