package com.example.service.Impl;

import com.example.domain.User;
import com.example.dto.UserProfileResponse;
import com.example.exception.UserNotFoundException;
import com.example.repository.UserRepository;
import com.example.service.IUserService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

import java.util.UUID;

/**
 * Implementation of user management operations
 */
@ApplicationScoped
public class UserService implements IUserService {

    private static final Logger LOG = Logger.getLogger(UserService.class);

    @Inject
    UserRepository userRepository;

    @Override
    @Transactional
    public User syncUser(String keycloakId, String username, String email) {
        LOG.infof("Syncing user: %s (keycloakId: %s)", username, keycloakId);

        // Check if user already exists
        User user = userRepository.findByKeycloakId(keycloakId).orElse(null);

        if (user == null) {
            // Create new user
            user = new User();
            user.id = UUID.randomUUID();
            user.keycloakId = keycloakId;
            user.username = username;
            user.email = email;
            user.plan = "FREE";
            user.linksCreated = 0;
            user.linksLimit = 100;

            userRepository.persist(user);

            LOG.infof("Created new user: %s", username);
        } else {
            // Update existing user (in case username/email changed in Keycloak)
            // JPA will auto-update on transaction commit (dirty checking)
            user.username = username;
            user.email = email;

            LOG.infof("Updated existing user: %s", username);
        }

        return user;
    }

    @Override
    public User getUserByKeycloakId(String keycloakId) {
        LOG.debugf("Getting user by keycloakId: %s", keycloakId);

        return userRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    @Override
    public UserProfileResponse getCurrentUserProfile(String keycloakId) {
        LOG.debugf("Getting current user profile for keycloakId: %s", keycloakId);

        User user = getUserByKeycloakId(keycloakId);

        return UserProfileResponse.builder()
                .id(String.valueOf(user.id))
                .username(user.username)
                .email(user.email)
                .plan(user.plan)
                .linksCreated(user.linksCreated)
                .linksLimit(user.linksLimit)
                .createdAt(String.valueOf(user.createdAt))
                .build();
    }
}
