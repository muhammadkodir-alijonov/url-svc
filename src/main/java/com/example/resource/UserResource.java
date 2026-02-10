package com.example.resource;

import com.example.domain.User;
import com.example.dto.UserProfileResponse;
import com.example.service.IUserService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;

@Path("/api/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "User Management", description = "User synchronization and profile")
public class UserResource {

    private static final Logger LOG = Logger.getLogger(UserResource.class);

    @Inject
    Instance<JsonWebToken> jwtInstance;

    @Inject
    IUserService userService;

    /**
     * Sync user from Keycloak to local database
     */
    @POST
    @Path("/sync")
    @RolesAllowed("user")
    @SecurityRequirement(name = "bearer-jwt")
    @Operation(
            summary = "Sync user",
            description = "Synchronize user from Keycloak to local database. Called after login to create/update user record."
    )
    @APIResponse(
            responseCode = "200",
            description = "User synchronized successfully",
            content = @Content(schema = @Schema(implementation = User.class))
    )
    @APIResponse(
            responseCode = "201",
            description = "New user created",
            content = @Content(schema = @Schema(implementation = User.class))
    )
    @APIResponse(responseCode = "401", description = "Unauthorized - token missing or invalid")
    public Response syncUser() {
        JsonWebToken jwt = jwtInstance.get();

        // Extract user info from JWT
        String keycloakId = jwt.getSubject();
        String username = jwt.getClaim("preferred_username");
        String email = jwt.getClaim("email");
        String firstName = jwt.getClaim("given_name");
        String lastName = jwt.getClaim("family_name");

        LOG.infof("Syncing user: %s (keycloakId: %s)", username, keycloakId);

        // Call service to sync user
        User user = userService.syncUser(keycloakId, username, email, firstName, lastName);

        // Determine if this was a new user
        boolean isNewUser = user.linksCreated == 0;

        Response.Status status = isNewUser
                ? Response.Status.CREATED
                : Response.Status.OK;

        return Response.status(status).entity(user).build();
    }

    /**
     * Get current user profile
     */
    @GET
    @Path("/me")
    @RolesAllowed("user")
    @SecurityRequirement(name = "bearer-jwt")
    @Operation(
            summary = "Get current user",
            description = "Get current authenticated user profile information"
    )
    @APIResponse(
            responseCode = "200",
            description = "User profile retrieved successfully",
            content = @Content(schema = @Schema(implementation = UserProfileResponse.class))
    )
    @APIResponse(responseCode = "401", description = "Unauthorized - token missing or invalid")
    @APIResponse(responseCode = "404", description = "User not found - please sync first")
    public Response getCurrentUser() {
        JsonWebToken jwt = jwtInstance.get();
        String keycloakId = jwt.getSubject();

        LOG.debugf("Get current user request for keycloakId: %s", keycloakId);

        // Call service layer
        UserProfileResponse profile = userService.getCurrentUserProfile(keycloakId);

        return Response.ok(profile).build();
    }
}