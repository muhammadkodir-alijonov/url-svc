package com.example.service;

import com.example.domain.User;
import com.example.dto.UserProfileResponse;

/**
 * Interface for user management operations
 */
public interface IUserService {

    /**
     * Sync user from Keycloak to local database
     *
     * @param keycloakId Keycloak user ID (from JWT subject)
     * @param username username from JWT
     * @param email email from JWT
     * @return User entity (created or updated)
     */
    User syncUser(String keycloakId, String username, String email);

    /**
     * Get user by Keycloak ID
     *
     * @param keycloakId Keycloak user ID
     * @return User entity
     */
    User getUserByKeycloakId(String keycloakId);

    /**
     * Get current user profile
     *
     * @param keycloakId Keycloak user ID from JWT
     * @return User profile response
     */
    UserProfileResponse getCurrentUserProfile(String keycloakId);
}
