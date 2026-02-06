package com.example.service;

import com.example.dto.AuthRequest;
import com.example.dto.AuthResponse;

/**
 * Interface for Keycloak authentication operations
 */
public interface IKeycloakService {

    /**
     * Register a new user in Keycloak
     *
     * @param request registration request containing user details
     * @return authentication response with tokens
     */
    AuthResponse registerUser(AuthRequest request);

    /**
     * Login user and get tokens
     *
     * @param username user's username
     * @param password user's password
     * @return authentication response with tokens
     */
    AuthResponse loginUser(String username, String password);

    /**
     * Refresh access token using refresh token
     *
     * @param refreshToken the refresh token
     * @return new authentication response with refreshed tokens
     */
    AuthResponse refreshToken(String refreshToken);

    /**
     * Get admin token for Keycloak admin operations
     *
     * @return admin access token
     */
    String getAdminToken() throws Exception;

    /**
     * Sync user from Keycloak to local database
     *
     * @param keycloakId Keycloak user ID
     * @param username username
     * @param email user email
     */
    void syncUserToDatabase(String keycloakId, String username, String email);
}
