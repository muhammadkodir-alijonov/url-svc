package com.example.resource;

import com.example.domain.User;
import com.example.repository.UserRepository;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;

import java.util.UUID;

@Path("/api/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "User Management", description = "User synchronization and profile")
public class UserResource {

    private static final Logger LOG = Logger.getLogger(UserResource.class);

    @Inject
    UserRepository userRepository;

    @Inject
    JsonWebToken jwt;

    /**
     * Sync user from Keycloak to local database
     *
     * Called by frontend after successful Keycloak login
     *
     * POST /api/users/sync
     */
    @POST
    @Path("/sync")
    @RolesAllowed("user")
    @Transactional
    @Operation(
            summary = "Sync user",
            description = "Synchronize user from Keycloak to local database"
    )
    @SecurityRequirement(name = "bearer-jwt")
    @APIResponse(responseCode = "200", description = "User synced successfully")
    @APIResponse(responseCode = "201", description = "User created")
    public Response syncUser() {
        // Extract user info from JWT
        String keycloakId = jwt.getSubject();
        String username = jwt.getClaim("preferred_username");
        String email = jwt.getClaim("email");

        LOG.infof("Syncing user: %s (keycloakId: %s)", username, keycloakId);

        // Check if user already exists
        User user = userRepository.findByKeycloakId(keycloakId).orElse(null);

        boolean isNewUser = false;

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

            isNewUser = true;
            LOG.infof("Created new user: %s", username);

        } else {
            // Update existing user (in case username/email changed in Keycloak)
            user.username = username;
            user.email = email;
            userRepository.persist(user);

            LOG.infof("Updated existing user: %s", username);
        }

        Response.Status status = isNewUser
                ? Response.Status.CREATED
                : Response.Status.OK;

        return Response.status(status).entity(user).build();
    }

    /**
     * Get current user profile
     *
     * GET /api/users/me
     */
    @GET
    @Path("/me")
    @RolesAllowed("user")
    @Operation(summary = "Get current user", description = "Get current user profile")
    @SecurityRequirement(name = "bearer-jwt")
    @APIResponse(responseCode = "200", description = "User profile")
    @APIResponse(responseCode = "404", description = "User not found")
    public Response getCurrentUser() {
        String keycloakId = jwt.getSubject();

        User user = userRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        return Response.ok(user).build();
    }
}