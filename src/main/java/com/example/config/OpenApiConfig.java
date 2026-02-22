package com.example.config;

import jakarta.ws.rs.core.Application;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.info.License;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;
import org.eclipse.microprofile.openapi.annotations.servers.Server;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@OpenAPIDefinition(
        info = @Info(
                title = "URL Shortener API",
                version = "1.0.0",
                description = "REST API for URL shortening service with analytics and QR code generation",
                license = @License(
                        name = "MIT",
                        url = "https://opensource.org/licenses/MIT"
                )
        ),
        servers = {
                @Server(
                        url = "http://localhost:8080",
                        description = "Local development server"
                )
        },
        tags = {
                @Tag(name = "Authentication", description = "User registration, login, and token management"),
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
        bearerFormat = "JWT"
)
public class OpenApiConfig extends Application {
    // This class is just for OpenAPI annotations
    // No additional code needed
}
