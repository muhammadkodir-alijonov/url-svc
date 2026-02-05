package com.example.exception;

import com.example.dto.ErrorResponse;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

import java.time.Instant;

/**
 * Global exception handler
 *
 * Catches all exceptions and returns consistent JSON error responses
 */
@Provider
public class GlobalExceptionHandler implements ExceptionMapper<Exception> {

    private static final Logger LOG = Logger.getLogger(GlobalExceptionHandler.class);

    @Context
    UriInfo uriInfo;

    @Override
    public Response toResponse(Exception exception) {
        LOG.error("Exception caught: " + exception.getMessage(), exception);

        ErrorResponse errorResponse;
        Response.Status status;

        // Map exceptions to HTTP status codes
        if (exception instanceof UrlNotFoundException) {
            status = Response.Status.NOT_FOUND;
            errorResponse = buildErrorResponse(status, exception);

        } else if (exception instanceof UrlExpiredException) {
            status = Response.Status.GONE; // 410
            errorResponse = buildErrorResponse(status, exception);

        } else if (exception instanceof ShortCodeTakenException) {
            status = Response.Status.CONFLICT; // 409
            errorResponse = buildErrorResponse(status, exception);

        } else if (exception instanceof InvalidAliasException) {
            status = Response.Status.BAD_REQUEST;
            errorResponse = buildErrorResponse(status, exception);

        } else if (exception instanceof InvalidUrlException) {
            status = Response.Status.BAD_REQUEST;
            errorResponse = buildErrorResponse(status, exception);

        } else if (exception instanceof PasswordRequiredException) {
            status = Response.Status.UNAUTHORIZED; // 401
            errorResponse = buildErrorResponse(status, exception);
            // Add WWW-Authenticate header for password prompt
            return Response.status(status)
                    .entity(errorResponse)
                    .header("WWW-Authenticate", "Password required")
                    .build();

        } else if (exception instanceof InvalidPasswordException) {
            status = Response.Status.UNAUTHORIZED;
            errorResponse = buildErrorResponse(status, exception);

        } else if (exception instanceof UserNotFoundException) {
            status = Response.Status.NOT_FOUND;
            errorResponse = buildErrorResponse(status, exception);

        } else if (exception instanceof UnauthorizedAccessException) {
            status = Response.Status.FORBIDDEN; // 403
            errorResponse = buildErrorResponse(status, exception);

        } else if (exception instanceof LimitExceededException) {
            status = Response.Status.FORBIDDEN;
            errorResponse = buildErrorResponse(status, exception);

        } else if (exception instanceof TooManyRequestsException) {
            status = Response.Status.TOO_MANY_REQUESTS; // 429
            errorResponse = buildErrorResponse(status, exception);

        } else if (exception instanceof jakarta.validation.ValidationException) {
            status = Response.Status.BAD_REQUEST;
            errorResponse = buildErrorResponse(status, "Validation failed", exception.getMessage());

        } else {
            // Unknown exception - return 500
            status = Response.Status.INTERNAL_SERVER_ERROR;
            errorResponse = buildErrorResponse(
                    status,
                    "Internal server error",
                    "An unexpected error occurred"
            );
            LOG.error("Unhandled exception: ", exception);
        }

        return Response.status(status).entity(errorResponse).build();
    }

    /**
     * Build error response with exception message
     */
    private ErrorResponse buildErrorResponse(Response.Status status, Exception exception) {
        return buildErrorResponse(status, status.getReasonPhrase(), exception.getMessage());
    }

    /**
     * Build error response with custom error and message
     */
    private ErrorResponse buildErrorResponse(Response.Status status, String error, String message) {
        return ErrorResponse.builder()
                .status(status.getStatusCode())
                .error(error)
                .message(message)
                .path(uriInfo != null ? uriInfo.getPath() : "unknown")
                .timestamp(Instant.now())
                .build();
    }
}
