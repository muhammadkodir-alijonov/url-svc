package com.example.service;

import com.example.domain.User;
import com.example.dto.UserProfileResponse;

/**
 * Interface for user management operations
 */
public interface IUserService {

    User syncUser(String keycloakId, String username, String email, String firstName, String lastName);

    User getUserByKeycloakId(String keycloakId);

    UserProfileResponse getCurrentUserProfile(String keycloakId);
}
