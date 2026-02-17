package com.example.controller;

import com.example.dto.*;
import com.example.service.IQRCodeService;
import com.example.service.IUrlService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;

@Path("/api/urls")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "URL Management", description = "Endpoints for creating and managing short URLs")
public class UrlController {

    private static final Logger LOG = Logger.getLogger(UrlController.class);

    @Inject
    IUrlService urlService;

    @Inject
    IQRCodeService qrCodeService;

    @POST
    @Path("/shorten")
    @RolesAllowed("user")
    @SecurityRequirement(name = "bearer-jwt")
    public Response shorten(@Valid ShortenRequest request) {
        LOG.infof("Shorten request received: %s", request.getOriginalUrl());

        ShortenResponse response = urlService.shorten(request);

        return Response.status(Response.Status.CREATED)
                .entity(response)
                .build();
    }

    @GET
    @RolesAllowed("user")
    @SecurityRequirement(name = "bearer-jwt")
    public Response listUrls(
            @QueryParam("page") @DefaultValue("1") int page,
            @QueryParam("size") @DefaultValue("20") int size,
            @QueryParam("sortBy") @DefaultValue("createdAt") String sortBy,
            @QueryParam("order") @DefaultValue("desc") String order) {

        LOG.debugf("List URLs: page=%d, size=%d, sortBy=%s, order=%s",
                page, size, sortBy, order);

        UrlListResponse response = urlService.listUrls(page, size, sortBy);

        return Response.ok(response).build();
    }

    @GET
    @Path("/{shortCode}")
    @RolesAllowed("user")
    @SecurityRequirement(name = "bearer-jwt")
    public Response getUrl(
            @PathParam("shortCode")
            @Parameter(description = "Short code", example = "aB3xK")
            String shortCode) {

        LOG.debugf("Get URL details: %s", shortCode);

        UrlResponse response = urlService.getUrl(shortCode);

        return Response.ok(response).build();
    }

    @PUT
    @Path("/{shortCode}")
    @RolesAllowed("user")
    @SecurityRequirement(name = "bearer-jwt")
    public Response updateUrl(
            @PathParam("shortCode") String shortCode,
            @Valid UpdateUrlRequest request) {

        LOG.infof("Update URL: %s", shortCode);

        UrlResponse response = urlService.updateUrl(shortCode, request);

        return Response.ok(response).build();
    }

    @DELETE
    @Path("/{shortCode}")
    @RolesAllowed("user")
    @SecurityRequirement(name = "bearer-jwt")
    public Response deleteUrl(@PathParam("shortCode") String shortCode) {
        LOG.infof("Delete URL: %s", shortCode);

        urlService.deleteUrl(shortCode);

        return Response.noContent().build();
    }

    @GET
    @Path("/{shortCode}/qr")
    @Produces("image/png")
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