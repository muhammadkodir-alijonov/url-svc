package com.example.resource;

import com.example.service.Impl.RedirectService;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;

import java.net.URI;

@Path("/")
@Tag(name = "Redirect", description = "Public redirect endpoint")
public class RedirectResource {

    private static final Logger LOG = Logger.getLogger(RedirectResource.class);

    @Inject
    RedirectService redirectService;

    @Context
    HttpHeaders headers;

    @Context
    UriInfo uriInfo;

    @GET
    @Path("/{shortCode}")
    @PermitAll
    public Response redirect(
            @PathParam("shortCode")
            @Parameter(description = "Short code", example = "aB3xK")
            String shortCode,

            @QueryParam("password")
            @Parameter(description = "Password (if URL is password-protected)")
            String password) {

        LOG.infof("Redirect request: %s", shortCode);

        // Extract request metadata
        String ipAddress = getClientIpAddress();
        String userAgent = headers.getHeaderString("User-Agent");
        String referer = headers.getHeaderString("Referer");

        // Resolve short code to original URL
        String originalUrl = redirectService.resolveShortCode(
                shortCode,
                password,
                ipAddress,
                userAgent,
                referer
        );

        LOG.infof("Redirecting %s -> %s", shortCode, originalUrl);

        // Return 302 redirect
        return Response.status(Response.Status.FOUND)
                .location(URI.create(originalUrl))
                .header("Cache-Control", "no-cache, no-store, must-revalidate")
                .build();
    }

    private String getClientIpAddress() {
        // Check X-Forwarded-For header (if behind proxy/load balancer)
        String xForwardedFor = headers.getHeaderString("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // X-Forwarded-For can contain multiple IPs, take the first one
            return xForwardedFor.split(",")[0].trim();
        }

        // Check X-Real-IP header (alternative)
        String xRealIp = headers.getHeaderString("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        // Fallback (not reliable behind proxy)
        return uriInfo.getRequestUri().getHost();
    }
}