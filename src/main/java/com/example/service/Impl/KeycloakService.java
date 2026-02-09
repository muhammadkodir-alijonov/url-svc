package com.example.service.Impl;

import com.example.dto.AuthRequest;
import com.example.dto.AuthResponse;
import com.example.service.IKeycloakService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementation of Keycloak authentication operations
 */
@ApplicationScoped
public class KeycloakService implements IKeycloakService {

    private static final Logger LOG = Logger.getLogger(KeycloakService.class);

    @ConfigProperty(name = "quarkus.oidc.auth-server-url")
    String keycloakUrl;

    @ConfigProperty(name = "quarkus.oidc.client-id")
    String clientId;

    @ConfigProperty(name = "quarkus.oidc.credentials.secret", defaultValue = "")
    String clientSecret;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    public AuthResponse registerUser(AuthRequest request) {
        try {
            LOG.infof("Registering user: %s", request.username);

            // Validate request
            validateRegistrationRequest(request);

            // Step 1: Create user in Keycloak Admin API (with password)
            String adminToken = getAdminToken();
            String userId = createUserInKeycloak(adminToken, request);

            LOG.infof("User created in Keycloak with credentials: %s (ID: %s)", request.username, userId);

            // Step 2: Verify user is fully enabled
            verifyUserEnabled(adminToken, userId);

            // Wait for Keycloak to fully process the user (important!)
            LOG.info("Waiting for Keycloak to process user credentials...");
            Thread.sleep(2000);

            // Step 3: Login to get tokens with retry
            return loginUserWithRetry(request.username, request.password, 3);

        } catch (WebApplicationException e) {
            // Re-throw WebApplicationException as-is (already has proper status code and message)
            LOG.errorf("Failed to register user %s: %s", request.username, e.getMessage());
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOG.errorf(e, "Registration interrupted for user: %s", request.username);
            throw new WebApplicationException(
                    "Registration interrupted",
                    Response.Status.INTERNAL_SERVER_ERROR
            );
        } catch (Exception e) {
            LOG.errorf(e, "Failed to register user: %s", request.username);
            throw new WebApplicationException(
                    "Failed to register user: " + e.getMessage(),
                    Response.Status.INTERNAL_SERVER_ERROR
            );
        }
    }

    /**
     * Login with retry mechanism for newly created users
     */
    private AuthResponse loginUserWithRetry(String username, String password, int maxAttempts) {
        WebApplicationException lastException = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                LOG.infof("Login attempt %d/%d for newly registered user: %s", attempt, maxAttempts, username);
                return loginUser(username, password);
            } catch (WebApplicationException e) {
                lastException = e;

                if (e.getResponse().getStatus() == Response.Status.UNAUTHORIZED.getStatusCode()) {
                    if (attempt < maxAttempts) {
                        // Wait a bit before retry
                        try {
                            LOG.infof("Login failed, retrying in 2 seconds... (attempt %d/%d)", attempt, maxAttempts);
                            Thread.sleep(2000);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            throw new WebApplicationException(
                                    "Login retry interrupted",
                                    Response.Status.INTERNAL_SERVER_ERROR
                            );
                        }
                    }
                } else {
                    // Not an auth error, don't retry
                    throw e;
                }
            }
        }

        // All retries failed
        LOG.errorf("All login attempts failed for user: %s", username);
        if (lastException != null) {
            throw lastException;
        } else {
            throw new WebApplicationException(
                    "Login failed after " + maxAttempts + " attempts",
                    Response.Status.INTERNAL_SERVER_ERROR
            );
        }
    }

    /**
     * Validate registration request
     */
    private void validateRegistrationRequest(AuthRequest request) {
        if (request.username == null || request.username.isBlank()) {
            throw new WebApplicationException("Username is required", Response.Status.BAD_REQUEST);
        }
        if (request.email == null || request.email.isBlank()) {
            throw new WebApplicationException("Email is required", Response.Status.BAD_REQUEST);
        }
        if (request.password == null || request.password.length() < 6) {
            throw new WebApplicationException(
                    "Password must be at least 6 characters",
                    Response.Status.BAD_REQUEST
            );
        }
    }

    public AuthResponse loginUser(String username, String password) {
        try {
            LOG.infof("Login attempt for user: %s", username);

            // Validate input
            if (username == null || username.isBlank()) {
                throw new WebApplicationException("Username is required", Response.Status.BAD_REQUEST);
            }
            if (password == null || password.isBlank()) {
                throw new WebApplicationException("Password is required", Response.Status.BAD_REQUEST);
            }

            String tokenUrl = keycloakUrl + "/protocol/openid-connect/token";
            LOG.debugf("Token URL: %s", tokenUrl);
            LOG.debugf("Client ID: %s", clientId);

            Map<String, String> formData = new HashMap<>();
            formData.put("grant_type", "password");
            formData.put("client_id", clientId);
            formData.put("username", username);
            formData.put("password", password);

            if (!clientSecret.isEmpty()) {
                formData.put("client_secret", clientSecret);
                LOG.debug("Using client secret");
            }

            String formBody = formData.entrySet().stream()
                    .map(e -> e.getKey() + "=" + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
                    .collect(Collectors.joining("&"));

            LOG.debugf("Request body (password masked): grant_type=password&client_id=%s&username=%s&password=***", clientId, username);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(tokenUrl))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(formBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            LOG.debugf("Keycloak response status: %d", response.statusCode());

            if (response.statusCode() == 401) {
                LOG.warnf("Invalid credentials for user: %s", username);
                LOG.debugf("Keycloak error response: %s", response.body());
                throw new WebApplicationException("Invalid username or password", Response.Status.UNAUTHORIZED);
            }

            if (response.statusCode() != 200) {
                LOG.errorf("Login failed: %d - %s", response.statusCode(), response.body());
                Response.Status responseStatus = Response.Status.fromStatusCode(response.statusCode());
                throw new WebApplicationException(
                        "Login failed: " + response.body(),
                        responseStatus != null ? responseStatus : Response.Status.INTERNAL_SERVER_ERROR
                );
            }

            LOG.infof("Login successful for user: %s", username);
            return parseTokenResponse(response.body());

        } catch (WebApplicationException e) {
            // Re-throw WebApplicationException as-is
            throw e;
        } catch (Exception e) {
            LOG.errorf(e, "Failed to login user: %s", username);
            throw new WebApplicationException(
                    "Login failed: " + e.getMessage(),
                    Response.Status.INTERNAL_SERVER_ERROR
            );
        }
    }

    public AuthResponse refreshToken(String refreshToken) {
        try {
            LOG.info("Token refresh attempt");

            // Validate input
            if (refreshToken == null || refreshToken.isBlank()) {
                throw new WebApplicationException("Refresh token is required", Response.Status.BAD_REQUEST);
            }

            String tokenUrl = keycloakUrl + "/protocol/openid-connect/token";

            Map<String, String> formData = new HashMap<>();
            formData.put("grant_type", "refresh_token");
            formData.put("client_id", clientId);
            formData.put("refresh_token", refreshToken);

            if (!clientSecret.isEmpty()) {
                formData.put("client_secret", clientSecret);
            }

            String formBody = formData.entrySet().stream()
                    .map(e -> e.getKey() + "=" + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
                    .collect(Collectors.joining("&"));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(tokenUrl))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(formBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 400 || response.statusCode() == 401) {
                LOG.warn("Invalid or expired refresh token");
                throw new WebApplicationException("Invalid or expired refresh token", Response.Status.UNAUTHORIZED);
            }

            if (response.statusCode() != 200) {
                LOG.errorf("Token refresh failed: %d - %s", response.statusCode(), response.body());
                Response.Status responseStatus = Response.Status.fromStatusCode(response.statusCode());
                throw new WebApplicationException(
                        "Token refresh failed",
                        responseStatus != null ? responseStatus : Response.Status.INTERNAL_SERVER_ERROR
                );
            }

            LOG.info("Token refresh successful");
            return parseTokenResponse(response.body());

        } catch (WebApplicationException e) {
            // Re-throw WebApplicationException as-is
            throw e;
        } catch (Exception e) {
            LOG.errorf(e, "Failed to refresh token");
            throw new WebApplicationException(
                    "Token refresh failed: " + e.getMessage(),
                    Response.Status.INTERNAL_SERVER_ERROR
            );
        }
    }

    @Override
    public String getAdminToken() throws Exception {
        String tokenUrl = keycloakUrl.replace("/realms/url-shortener", "/realms/master")
                + "/protocol/openid-connect/token";

        Map<String, String> formData = new HashMap<>();
        formData.put("grant_type", "password");
        formData.put("client_id", "admin-cli");
        formData.put("username", "admin");
        formData.put("password", "admin123"); // TODO: Move to config

        String formBody = formData.entrySet().stream()
                .map(e -> e.getKey() + "=" + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(tokenUrl))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(formBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Failed to get admin token: " + response.body());
        }

        // Parse JSON manually (simple approach)
        String body = response.body();
        String accessToken = body.substring(body.indexOf("\"access_token\":\"") + 16);
        accessToken = accessToken.substring(0, accessToken.indexOf("\""));

        return accessToken;
    }

    private String createUserInKeycloak(String adminToken, AuthRequest request) throws Exception {
        String usersUrl = keycloakUrl.replace("/realms/url-shortener", "")
                + "/admin/realms/url-shortener/users";

        // Create user with credentials included
        String userJson = String.format("""
                {
                    "username": "%s",
                    "email": "%s",
                    "firstName": "%s",
                    "lastName": "%s",
                    "enabled": true,
                    "emailVerified": true,
                    "credentials": [{
                        "type": "password",
                        "value": "%s",
                        "temporary": false
                    }]
                }
                """,
                request.username,
                request.email,
                request.firstName != null ? request.firstName : "",
                request.lastName != null ? request.lastName : "",
                request.password
        );

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(usersUrl))
                .header("Authorization", "Bearer " + adminToken)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(userJson))
                .build();

        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 409) {
            throw new WebApplicationException("User already exists", Response.Status.CONFLICT);
        }

        if (response.statusCode() != 201) {
            throw new RuntimeException("Failed to create user: " + response.body());
        }

        // Get user ID from Location header
        String location = response.headers().firstValue("Location").orElseThrow();
        return location.substring(location.lastIndexOf("/") + 1);
    }

    private void setUserPassword(String adminToken, String userId, String password) throws Exception {
        String passwordUrl = keycloakUrl.replace("/realms/url-shortener", "")
                + "/admin/realms/url-shortener/users/" + userId + "/reset-password";

        String passwordJson = String.format("""
                {
                    "type": "password",
                    "value": "%s",
                    "temporary": false
                }
                """, password);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(passwordUrl))
                .header("Authorization", "Bearer " + adminToken)
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(passwordJson))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 204) {
            throw new RuntimeException("Failed to set password: " + response.body());
        }

        LOG.infof("Password set successfully for user ID: %s", userId);
    }

    /**
     * Verify user is fully created and enabled in Keycloak
     */
    private void verifyUserEnabled(String adminToken, String userId) throws Exception {
        String userUrl = keycloakUrl.replace("/realms/url-shortener", "")
                + "/admin/realms/url-shortener/users/" + userId;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(userUrl))
                .header("Authorization", "Bearer " + adminToken)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Failed to verify user: " + response.body());
        }

        // Check if user is enabled
        if (!response.body().contains("\"enabled\":true")) {
            throw new RuntimeException("User is not enabled in Keycloak");
        }

        LOG.infof("User verified as enabled in Keycloak: %s", userId);
    }

    private AuthResponse parseTokenResponse(String json) {
        // Simple JSON parsing (in production, use Jackson properly)
        AuthResponse authResponse = new AuthResponse();

        String accessToken = extractJsonValue(json, "access_token");
        String refreshToken = extractJsonValue(json, "refresh_token");
        String expiresIn = extractJsonValue(json, "expires_in");

        authResponse.accessToken = accessToken;
        authResponse.refreshToken = refreshToken;
        authResponse.expiresIn = expiresIn != null ? Integer.parseInt(expiresIn) : 300;
        authResponse.tokenType = "Bearer";

        // Extract user info from JWT token
        if (accessToken != null) {
            try {
                String[] parts = accessToken.split("\\.");
                if (parts.length > 1) {
                    // Decode JWT payload (Base64)
                    String payload = new String(
                            java.util.Base64.getUrlDecoder().decode(parts[1]),
                            StandardCharsets.UTF_8
                    );

                    // Extract user information from payload
                    authResponse.userId = extractJsonValue(payload, "sub");
                    authResponse.username = extractJsonValue(payload, "preferred_username");
                    authResponse.email = extractJsonValue(payload, "email");

                    LOG.debugf("Extracted user info: userId=%s, username=%s, email=%s",
                            authResponse.userId, authResponse.username, authResponse.email);
                }
            } catch (Exception e) {
                LOG.warnf("Failed to decode JWT token: %s", e.getMessage());
            }
        }

        return authResponse;
    }

    private String extractJsonValue(String json, String key) {
        String search = "\"" + key + "\":";
        int start = json.indexOf(search);
        if (start == -1) return null;

        start += search.length();
        if (json.charAt(start) == '"') {
            start++;
            int end = json.indexOf("\"", start);
            return json.substring(start, end);
        } else {
            int end = json.indexOf(",", start);
            if (end == -1) end = json.indexOf("}", start);
            return json.substring(start, end).trim();
        }
    }

    @Override
    public void syncUserToDatabase(String keycloakId, String username, String email) {
        // This method can be implemented when UserRepository is available
        LOG.infof("Syncing user to database: keycloakId=%s, username=%s, email=%s", keycloakId, username, email);
        // TODO: Implement database sync logic
    }
}
