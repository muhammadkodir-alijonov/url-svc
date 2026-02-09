package com.example.resource;

import com.example.dto.AuthRequest;
import com.example.dto.AuthResponse;
import com.example.dto.LoginRequest;
import com.example.service.IKeycloakService;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;


@Path("/api/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Authentication", description = "User registration and authentication")
public class AuthResource {

    private static final Logger LOG = Logger.getLogger(AuthResource.class);

    @Inject
    IKeycloakService keycloakService;

    @POST
    @Path("/register")
    @PermitAll
    public Response register(AuthRequest request) {
        LOG.infof("Registering user: %s", request.username);

        AuthResponse authResponse = keycloakService.registerUser(request);

        return Response.status(Response.Status.CREATED)
                .entity(authResponse)
                .build();
    }

    @POST
    @Path("/login")
    @PermitAll
    public Response login(LoginRequest request) {
        LOG.infof("Login attempt for user: %s", request.username);

        AuthResponse authResponse = keycloakService.loginUser(request.username, request.password);

        return Response.ok(authResponse).build();
    }


    @POST
    @Path("/refresh")
    @PermitAll
    public Response refresh(RefreshTokenRequest request) {
        LOG.info("Token refresh attempt");

        AuthResponse authResponse = keycloakService.refreshToken(request.refreshToken);

        return Response.ok(authResponse).build();
    }

    @POST
    @Path("/logout")
    @RolesAllowed("user")
    public Response logout() {
        LOG.info("User logout");
        return Response.noContent().build();
    }

    public static class RefreshTokenRequest {
        @JsonProperty("refresh_token")
        public String refreshToken;
    }
}
