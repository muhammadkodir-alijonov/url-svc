package com.example.config;

/**
 * DISABLED - No longer needed
 *
 * Vault secrets are now initialized via scripts/setup-vault.sh
 * Configuration is automatically loaded from Vault using Quarkus Vault Config Source
 *
 * Path structure in Vault:
 * - secret/url-shortener/dev/database/postgres
 * - secret/url-shortener/dev/keycloak/config
 * - secret/url-shortener/dev/redis/config
 * - secret/url-shortener/dev/pulsar/config
 * - secret/url-shortener/dev/application/config
 */
/*
import com.example.service.VaultService;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class VaultInitializer {

    private static final Logger LOG = Logger.getLogger(VaultInitializer.class);

    @Inject
    VaultService vaultService;

    @ConfigProperty(name = "app.environment", defaultValue = "dev")
    String environment;

    @ConfigProperty(name = "quarkus.datasource.username")
    String dbUsername;

    @ConfigProperty(name = "quarkus.datasource.password")
    String dbPassword;

    @ConfigProperty(name = "mp.jwt.verify.publickey.location")
    String keycloakCertsUrl;

    void onStart(@Observes StartupEvent ev) {
        LOG.infof("Initializing Vault with application secrets for environment: %s", environment);

        try {
            // Check if Vault service is available
            if (vaultService == null) {
                LOG.warn("VaultService is not available - skipping Vault initialization");
                return;
            }

            // Store database credentials
            storeDatabaseSecrets();

            // Store Keycloak configuration
            storeKeycloakSecrets();

            // Store application secrets
            storeApplicationSecrets();

            LOG.infof("Vault initialization completed successfully for environment: %s", environment);
        } catch (Exception e) {
            LOG.warnf("Failed to initialize Vault: %s - Application will continue without Vault integration", e.getMessage());
            LOG.debug("Vault initialization error details:", e);
            // Don't fail startup, just log the error
        }
    }

    private void storeDatabaseSecrets() {
        Map<String, String> dbSecrets = new HashMap<>();
        dbSecrets.put("username", dbUsername);
        dbSecrets.put("password", dbPassword);
        dbSecrets.put("host", "localhost");
        dbSecrets.put("port", "30432");
        dbSecrets.put("database", "url_shortener");

        vaultService.storeSecrets("database/postgres", dbSecrets);
        LOG.info("Database secrets stored in Vault");
    }

    private void storeKeycloakSecrets() {
        Map<String, String> keycloakSecrets = new HashMap<>();
        keycloakSecrets.put("server-url", "http://localhost:30180");
        keycloakSecrets.put("realm", "url-shortener");
        keycloakSecrets.put("client-id", "url-shortener-client");
        keycloakSecrets.put("admin-username", "admin");
        keycloakSecrets.put("admin-password", "admin");
        keycloakSecrets.put("certs-url", keycloakCertsUrl);

        vaultService.storeSecrets("keycloak/config", keycloakSecrets);
        LOG.info("Keycloak secrets stored in Vault");
    }

    private void storeApplicationSecrets() {
        Map<String, String> appSecrets = new HashMap<>();

        // Redis
        appSecrets.put("redis-host", "localhost");
        appSecrets.put("redis-port", "30379");

        // Pulsar
        appSecrets.put("pulsar-url", "pulsar://localhost:30650");

        // APISIX
        appSecrets.put("apisix-gateway-url", "http://localhost:30900");
        appSecrets.put("apisix-admin-url", "http://localhost:30901");
        appSecrets.put("apisix-admin-key", "admin-api-key");

        // Vault itself
        appSecrets.put("vault-url", "http://localhost:30200");
        appSecrets.put("vault-token", "dev-root-token");

        vaultService.storeSecrets("application/services", appSecrets);
        LOG.info("Application secrets stored in Vault");
    }
}
*/
