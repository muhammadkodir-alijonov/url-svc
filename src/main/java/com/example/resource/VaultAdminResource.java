package com.example.resource;

import com.example.service.VaultService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;

import java.util.Map;

@Path("/api/admin/vault")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Vault Admin", description = "Vault secret management endpoints")
public class VaultAdminResource {

    private static final Logger LOG = Logger.getLogger(VaultAdminResource.class);

    @Inject
    VaultService vaultService;

    @GET
    @Path("/{path}/{key}")
    @RolesAllowed("admin")
    @Operation(summary = "Get a secret from Vault", description = "Retrieve a specific secret by path and key")
    public Response getSecret(@PathParam("path") String path, @PathParam("key") String key) {
        LOG.infof("Admin retrieving secret: path=%s, key=%s", path, key);

        String value = vaultService.getSecret(path, key);
        if (value == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "Secret not found"))
                    .build();
        }

        return Response.ok(Map.of(
                "path", path,
                "key", key,
                "value", value
        )).build();
    }

    @GET
    @Path("/{path}")
    @RolesAllowed("admin")
    @Operation(summary = "Get all secrets from a path", description = "Retrieve all secrets from a specific path")
    public Response getSecrets(@PathParam("path") String path) {
        LOG.infof("Admin retrieving all secrets: path=%s", path);

        Map<String, String> secrets = vaultService.getSecrets(path);

        return Response.ok(Map.of(
                "path", path,
                "secrets", secrets,
                "count", secrets.size()
        )).build();
    }

    @POST
    @Path("/{path}")
    @RolesAllowed("admin")
    @Operation(summary = "Store secrets in Vault", description = "Store one or more secrets at a specific path")
    public Response storeSecrets(@PathParam("path") String path, Map<String, String> secrets) {
        LOG.infof("Admin storing secrets: path=%s, count=%d", path, secrets.size());

        vaultService.storeSecrets(path, secrets);

        return Response.ok(Map.of(
                "message", "Secrets stored successfully",
                "path", path,
                "count", secrets.size()
        )).build();
    }

    @DELETE
    @Path("/{path}")
    @RolesAllowed("admin")
    @Operation(summary = "Delete secrets from Vault", description = "Delete all secrets at a specific path")
    public Response deleteSecret(@PathParam("path") String path) {
        LOG.infof("Admin deleting secret: path=%s", path);

        vaultService.deleteSecret(path);

        return Response.ok(Map.of(
                "message", "Secret deleted successfully",
                "path", path
        )).build();
    }

    @GET
    @Path("/health")
    @Operation(summary = "Check Vault connection", description = "Verify Vault is accessible")
    public Response healthCheck() {
        try {
            // Try to read a test secret to verify connection
            vaultService.secretExists("health", "check");
            return Response.ok(Map.of(
                    "status", "healthy",
                    "vault", "connected"
            )).build();
        } catch (Exception e) {
            LOG.error("Vault health check failed", e);
            return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                    .entity(Map.of(
                            "status", "unhealthy",
                            "vault", "disconnected",
                            "error", e.getMessage()
                    ))
                    .build();
        }
    }
}

