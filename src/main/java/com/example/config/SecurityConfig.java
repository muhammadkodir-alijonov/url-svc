package com.example.config;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

import java.io.IOException;


@ApplicationScoped
public class SecurityConfig {

    private static final Logger LOG = Logger.getLogger(SecurityConfig.class);

  @Provider
    public static class CorsFilter implements ContainerRequestFilter {

        @Override
        public void filter(ContainerRequestContext requestContext) throws IOException {
            // Custom CORS logic if needed
            // Currently handled by quarkus.http.cors config
        }
    }

    @Provider
    public static class CustomAuthFilter implements ContainerRequestFilter {

        private static final Logger LOG = Logger.getLogger(CustomAuthFilter.class);

        @Override
        public void filter(ContainerRequestContext requestContext) throws IOException {
            String path = requestContext.getUriInfo().getPath();

            // Log all requests (for debugging)
            LOG.debugf("Request: %s %s",
                    requestContext.getMethod(),
                    path);

            // Custom auth logic here if needed
            // For now, JWT is handled by @RolesAllowed
        }
    }

    /**
     * Request logging filter
     */
    @Provider
    public static class RequestLoggingFilter implements ContainerRequestFilter {

        private static final Logger LOG = Logger.getLogger(RequestLoggingFilter.class);

        @Override
        public void filter(ContainerRequestContext requestContext) throws IOException {
            // Log incoming requests
            LOG.infof("%s %s from %s",
                    requestContext.getMethod(),
                    requestContext.getUriInfo().getPath(),
                    getClientIp(requestContext));
        }

        private String getClientIp(ContainerRequestContext context) {
            String xForwardedFor = context.getHeaderString("X-Forwarded-For");
            if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                return xForwardedFor.split(",")[0].trim();
            }

            String xRealIp = context.getHeaderString("X-Real-IP");
            if (xRealIp != null && !xRealIp.isEmpty()) {
                return xRealIp;
            }

            return "unknown";
        }
    }

    /**
     * Security helper methods
     */
    public static class SecurityHelper {

        /**
         * Hash password using BCrypt
         */
        public static String hashPassword(String password) {
            return org.mindrot.jbcrypt.BCrypt.hashpw(
                    password,
                    org.mindrot.jbcrypt.BCrypt.gensalt()
            );
        }

        /**
         * Verify password against hash
         */
        public static boolean verifyPassword(String password, String hash) {
            return org.mindrot.jbcrypt.BCrypt.checkpw(password, hash);
        }

        /**
         * Generate API key (for future use)
         */
        public static String generateApiKey() {
            return java.util.UUID.randomUUID().toString().replace("-", "");
        }
    }
}