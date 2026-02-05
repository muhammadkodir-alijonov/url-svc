package com.example.resource;

import com.example.dto.*;
import com.example.service.QRCodeService;
import com.example.service.UrlService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;

@Path("/api/urls")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "URL Management", description = "Endpoints for creating and managing short URLs")
public class UrlResource {

    private static final Logger LOG = Logger.getLogger(UrlResource.class);

    @Inject
    UrlService urlService;

    @Inject
    QRCodeService qrCodeService;

    /**
     * Shorten URL
     *
     * POST /api/urls
     * Body: { "originalUrl": "https://...", "customAlias": "my-link" }
     */
    @POST
    @RolesAllowed("user")
    @Operation(summary = "Shorten URL", description = "Create a new short URL")
    @SecurityRequirement(name = "bearer-jwt")
    @APIResponse(
            responseCode = "201",
            description = "URL shortened successfully",
            content = @Content(schema = @Schema(implementation = ShortenResponse.class))
    )
    @APIResponse(responseCode = "400", description = "Invalid request")
    @APIResponse(responseCode = "401", description = "Unauthorized")
    @APIResponse(responseCode = "409", description = "Alias already taken")
    public Response shorten(@Valid ShortenRequest request) {
        LOG.infof("Shorten request received: %s", request.getOriginalUrl());

        ShortenResponse response = urlService.shorten(request);

        return Response.status(Response.Status.CREATED)
                .entity(response)
                .build();
    }

    /**
     * List my URLs
     *
     * GET /api/urls?page=1&size=20&sortBy=createdAt&order=desc
     */
    @GET
    @RolesAllowed("user")
    @Operation(summary = "List my URLs", description = "Get all URLs created by current user")
    @SecurityRequirement(name = "bearer-jwt")
    @APIResponse(
            responseCode = "200",
            description = "URLs retrieved successfully",
            content = @Content(schema = @Schema(implementation = UrlListResponse.class))
    )
    public Response listUrls(
            @QueryParam("page") @DefaultValue("1") int page,
            @QueryParam("size") @DefaultValue("20") int size,
            @QueryParam("sortBy") @DefaultValue("createdAt") String sortBy,
            @QueryParam("order") @DefaultValue("desc") String order) {

        LOG.debugf("List URLs: page=%d, size=%d, sortBy=%s, order=%s",
                page, size, sortBy, order);

        UrlListResponse response = urlService.listUrls(page, size, sortBy, order);

        return Response.ok(response).build();
    }

    /**
     * Get URL details
     *
     * GET /api/urls/{shortCode}
     */
    @GET
    @Path("/{shortCode}")
    @RolesAllowed("user")
    @Operation(summary = "Get URL details", description = "Get details of a specific short URL")
    @SecurityRequirement(name = "bearer-jwt")
    @APIResponse(
            responseCode = "200",
            description = "URL details retrieved",
            content = @Content(schema = @Schema(implementation = UrlResponse.class))
    )
    @APIResponse(responseCode = "404", description = "URL not found")
    @APIResponse(responseCode = "403", description = "Not your URL")
    public Response getUrl(
            @PathParam("shortCode")
            @Parameter(description = "Short code", example = "aB3xK")
            String shortCode) {

        LOG.debugf("Get URL details: %s", shortCode);

        UrlResponse response = urlService.getUrl(shortCode);

        return Response.ok(response).build();
    }

    /**
     * Update URL
     *
     * PUT /api/urls/{shortCode}
     * Body: { "originalUrl": "https://new-url.com", "title": "New Title" }
     */
    @PUT
    @Path("/{shortCode}")
    @RolesAllowed("user")
    @Operation(summary = "Update URL", description = "Update an existing short URL")
    @SecurityRequirement(name = "bearer-jwt")
    @APIResponse(
            responseCode = "200",
            description = "URL updated successfully",
            content = @Content(schema = @Schema(implementation = UrlResponse.class))
    )
    @APIResponse(responseCode = "404", description = "URL not found")
    @APIResponse(responseCode = "403", description = "Not your URL")
    public Response updateUrl(
            @PathParam("shortCode") String shortCode,
            @Valid UpdateUrlRequest request) {

        LOG.infof("Update URL: %s", shortCode);

        UrlResponse response = urlService.updateUrl(shortCode, request);

        return Response.ok(response).build();
    }

    /**
     * Delete URL
     *
     * DELETE /api/urls/{shortCode}
     */
    @DELETE
    @Path("/{shortCode}")
    @RolesAllowed("user")
    @Operation(summary = "Delete URL", description = "Delete a short URL")
    @SecurityRequirement(name = "bearer-jwt")
    @APIResponse(responseCode = "204", description = "URL deleted successfully")
    @APIResponse(responseCode = "404", description = "URL not found")
    @APIResponse(responseCode = "403", description = "Not your URL")
    public Response deleteUrl(@PathParam("shortCode") String shortCode) {
        LOG.infof("Delete URL: %s", shortCode);

        urlService.deleteUrl(shortCode);

        return Response.noContent().build();
    }

    /**
     * Get QR Code
     *
     * GET /api/urls/{shortCode}/qr?size=256
     */
    @GET
    @Path("/{shortCode}/qr")
    @Produces("image/png")
    @Operation(summary = "Get QR Code", description = "Generate QR code for short URL")
    @APIResponse(
            responseCode = "200",
            description = "QR code generated",
            content = @Content(mediaType = "image/png")
    )
    @APIResponse(responseCode = "404", description = "URL not found")
    public Response getQRCode(
            @PathParam("shortCode") String shortCode,
            @QueryParam("size") @DefaultValue("256") int size) {

        LOG.debugf("Generate QR code: %s (size: %d)", shortCode, size);

        // Verify URL exists (this will throw exception if not)
        UrlResponse url = urlService.getUrl(shortCode);

        // Generate QR code
        byte[] qrCode = qrCodeService.generateQRCode(url.getShortUrl(), size);

        return Response.ok(qrCode)
                .header("Content-Disposition", "inline; filename=\"" + shortCode + ".png\"")
                .build();
    }
}