package com.example.resource;

import com.example.domain.User;
import com.example.dto.AuthRequest;
import com.example.dto.AuthResponse;
import com.example.dto.UserProfileResponse;
import com.example.repository.UserRepository;
import com.example.service.KeycloakService;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.enterprise.inject.Instance;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;


@Path("/api/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Authentication", description = "User registration and authentication")
public class AuthResource {

    private static final Logger LOG = Logger.getLogger(AuthResource.class);

    @Inject
    KeycloakService keycloakService;

    @Inject
    UserRepository userRepository;

    @Inject
    Instance<JsonWebToken> jwtInstance;

    private JsonWebToken getJwt() {
        return jwtInstance.get();
    }

    /**
     * Register a new user
     * POST /api/auth/register
     */
    @POST
    @Path("/register")
    @PermitAll
    @Transactional
    @Operation(
            summary = "Register user",
            description = "Register a new user and return authentication tokens"
    )
    @APIResponse(responseCode = "201", description = "User registered successfully")
    @APIResponse(responseCode = "400", description = "Invalid request")
    @APIResponse(responseCode = "409", description = "User already exists")
    public Response register(AuthRequest request) {
        LOG.infof("Registering user: %s", request.username);

        // Validate request
        if (request.username == null || request.username.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Username is required"))
                    .build();
        }

        if (request.email == null || request.email.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Email is required"))
                    .build();
        }

        if (request.password == null || request.password.length() < 6) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Password must be at least 6 characters"))
                    .build();
        }

        try {
            // Register in Keycloak and get tokens
            AuthResponse authResponse = keycloakService.registerUser(request);

            LOG.infof("User registered successfully: %s", request.username);

            return Response.status(Response.Status.CREATED)
                    .entity(authResponse)
                    .build();

        } catch (WebApplicationException e) {
            throw e;
        } catch (Exception e) {
            LOG.errorf(e, "Registration failed for user: %s", request.username);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("Registration failed: " + e.getMessage()))
                    .build();
        }
    }

    /**
     * Login user
     * POST /api/auth/login
     */
    @POST
    @Path("/login")
    @PermitAll
    @Operation(
            summary = "Login",
            description = "Login with username and password, returns JWT tokens"
    )
    @APIResponse(responseCode = "200", description = "Login successful")
    @APIResponse(responseCode = "401", description = "Invalid credentials")
    public Response login(AuthRequest request) {
        LOG.infof("Login attempt for user: %s", request.username);

        if (request.username == null || request.password == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Username and password are required"))
                    .build();
        }

        try {
            AuthResponse authResponse = keycloakService.loginUser(request.username, request.password);

            LOG.infof("User logged in successfully: %s", request.username);

            return Response.ok(authResponse).build();

        } catch (WebApplicationException e) {
            throw e;
        } catch (Exception e) {
            LOG.errorf(e, "Login failed for user: %s", request.username);
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new ErrorResponse("Invalid credentials"))
                    .build();
        }
    }

    /**
     * Refresh access token
     * POST /api/auth/refresh
     */
    @POST
    @Path("/refresh")
    @PermitAll
    @Operation(
            summary = "Refresh token",
            description = "Refresh access token using refresh token"
    )
    @APIResponse(responseCode = "200", description = "Token refreshed")
    @APIResponse(responseCode = "401", description = "Invalid refresh token")
    public Response refresh(RefreshTokenRequest request) {
        LOG.info("Token refresh attempt");

        if (request.refreshToken == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Refresh token is required"))
                    .build();
        }

        try {
            AuthResponse authResponse = keycloakService.refreshToken(request.refreshToken);

            return Response.ok(authResponse).build();

        } catch (WebApplicationException e) {
            throw e;
        } catch (Exception e) {
            LOG.errorf(e, "Token refresh failed");
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new ErrorResponse("Invalid refresh token"))
                    .build();
        }
    }

    /**
     * Get current user profile
     * GET /api/auth/profile
     */
    @GET
    @Path("/profile")
    @RolesAllowed("user")
    @Operation(
            summary = "Get profile",
            description = "Get current authenticated user profile"
    )
    @SecurityRequirement(name = "bearer-jwt")
    @APIResponse(responseCode = "200", description = "User profile")
    @APIResponse(responseCode = "404", description = "User not found")
    public Response getProfile() {
        String keycloakId = getJwt().getSubject();

        User user = userRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new NotFoundException("User not found. Please sync your account first."));

        UserProfileResponse profile = new UserProfileResponse();
        profile.id = user.id.toString();
        profile.username = user.username;
        profile.email = user.email;
        profile.plan = user.plan;
        profile.linksCreated = user.linksCreated;
        profile.linksLimit = user.linksLimit;
        profile.createdAt = user.createdAt != null ? user.createdAt.toString() : null;

        return Response.ok(profile).build();
    }

    /**
     * Logout (client-side token invalidation)
     * POST /api/auth/logout
     */
    @POST
    @Path("/logout")
    @RolesAllowed("user")
    @Operation(
            summary = "Logout",
            description = "Logout user (client should delete tokens)"
    )
    @SecurityRequirement(name = "bearer-jwt")
    @APIResponse(responseCode = "204", description = "Logout successful")
    public Response logout() {
        LOG.info("User logout");
        return Response.noContent().build();
    }

    // Inner DTO classes

    public static class RefreshTokenRequest {
        @com.fasterxml.jackson.annotation.JsonProperty("refresh_token")
        public String refreshToken;
    }

    public static class ErrorResponse {
        public String error;

        public ErrorResponse(String error) {
            this.error = error;
        }
    }
}
