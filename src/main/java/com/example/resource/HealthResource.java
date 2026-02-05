package com.example.resource;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Path("/health")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Health", description = "Health check endpoints")
public class HealthResource {

    @GET
    @Operation(summary = "Health check", description = "Check if service is running")
    public Response health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "url-service");
        response.put("timestamp", Instant.now());

        return Response.ok(response).build();
    }
}