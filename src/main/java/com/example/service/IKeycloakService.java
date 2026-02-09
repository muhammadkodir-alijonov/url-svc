package com.example.service;

import com.example.dto.AuthRequest;
import com.example.dto.AuthResponse;

/**
 * Interface for Keycloak authentication operations
 */
public interface IKeycloakService {

    AuthResponse registerUser(AuthRequest request);

    AuthResponse loginUser(String username, String password);

    AuthResponse refreshToken(String refreshToken);

    String getAdminToken() throws Exception;

    void syncUserToDatabase(String keycloakId, String username, String email);
}
