package com.example.config;

import jakarta.ws.rs.core.Application;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.info.Contact;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.info.License;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;
import org.eclipse.microprofile.openapi.annotations.servers.Server;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeIn;

/**
 * OpenAPI/Swagger configuration
 *
 * Accessible at: http://localhost:8080/swagger-ui
 */
@OpenAPIDefinition(
        info = @Info(
                title = "URL Shortener API",
                version = "1.0.0",
                description = "REST API for URL shortening service with analytics and QR code generation",
                contact = @Contact(
                        name = "Your Name",
                        email = "your.email@example.com",
                        url = "https://github.com/yourusername/url-service"
                ),
                license = @License(
                        name = "MIT",
                        url = "https://opensource.org/licenses/MIT"
                )
        ),
        servers = {
                @Server(
                        url = "http://localhost:8080",
                        description = "Local development server"
                ),
                @Server(
                        url = "http://localhost:9080",
                        description = "Local APISIX gateway"
                ),
                @Server(
                        url = "https://api.shortener.by",
                        description = "Production server"
                )
        },
        tags = {
                @Tag(name = "URL Management", description = "Create, update, delete short URLs"),
                @Tag(name = "Redirect", description = "Public redirect endpoints"),
                @Tag(name = "User Management", description = "User profile and sync"),
                @Tag(name = "Health", description = "Health check endpoints")
        }
)
@SecurityScheme(
        securitySchemeName = "bearer-jwt",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "JWT token from Keycloak. Get token by logging in through Keycloak."
)
public class OpenApiConfig extends Application {
    // This class is just for OpenAPI annotations
    // No additional code needed
}
