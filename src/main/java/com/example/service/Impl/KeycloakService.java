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
            // Step 1: Create user in Keycloak Admin API
            String adminToken = getAdminToken();
            String userId = createUserInKeycloak(adminToken, request);

            // Step 2: Set user password
            setUserPassword(adminToken, userId, request.password);

            // Step 3: Login to get tokens
            return loginUser(request.username, request.password);

        } catch (Exception e) {
            LOG.errorf(e, "Failed to register user: %s", request.username);
            throw new WebApplicationException(
                    "Failed to register user: " + e.getMessage(),
                    Response.Status.INTERNAL_SERVER_ERROR
            );
        }
    }

    public AuthResponse loginUser(String username, String password) {
        try {
            String tokenUrl = keycloakUrl + "/protocol/openid-connect/token";

            Map<String, String> formData = new HashMap<>();
            formData.put("grant_type", "password");
            formData.put("client_id", clientId);
            formData.put("username", username);
            formData.put("password", password);

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

            if (response.statusCode() != 200) {
                LOG.errorf("Login failed: %d - %s", response.statusCode(), response.body());
                throw new WebApplicationException("Invalid credentials", Response.Status.UNAUTHORIZED);
            }

            return parseTokenResponse(response.body());

        } catch (WebApplicationException e) {
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

            if (response.statusCode() != 200) {
                throw new WebApplicationException("Invalid refresh token", Response.Status.UNAUTHORIZED);
            }

            return parseTokenResponse(response.body());

        } catch (WebApplicationException e) {
            throw e;
        } catch (Exception e) {
            LOG.errorf(e, "Failed to refresh token");
            throw new WebApplicationException(
                    "Token refresh failed",
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

        String userJson = String.format("""
                {
                    "username": "%s",
                    "email": "%s",
                    "firstName": "%s",
                    "lastName": "%s",
                    "enabled": true,
                    "emailVerified": true
                }
                """,
                request.username,
                request.email,
                request.firstName != null ? request.firstName : "",
                request.lastName != null ? request.lastName : ""
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
