package com.example.exception;

import com.example.dto.ErrorResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.time.Instant;
import java.util.stream.Collectors;

@Provider
public class ValidationExceptionHandler implements ExceptionMapper<ConstraintViolationException> {

    @Context
    UriInfo uriInfo;

    @Override
    public Response toResponse(ConstraintViolationException exception) {
        // Collect all validation error messages
        String message = exception.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));

        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(Response.Status.BAD_REQUEST.getStatusCode())
                .error("Validation Error")
                .message(message)
                .path(uriInfo != null ? uriInfo.getPath() : "unknown")
                .timestamp(Instant.now())
                .build();

        return Response.status(Response.Status.BAD_REQUEST).entity(errorResponse).build();
    }
}