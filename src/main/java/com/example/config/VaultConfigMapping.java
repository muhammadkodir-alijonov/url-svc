package com.example.config;

/**
 * NOT NEEDED - Quarkus Vault Config Source automatically maps secrets to properties
 *
 * The Vault Config Source reads from the paths specified in application-dev.properties:
 * - quarkus.vault.secret-config-kv-path.database=secret/url-shortener/dev/database/postgres
 * - quarkus.vault.secret-config-kv-path.keycloak=secret/url-shortener/dev/keycloak/config
 * - etc.
 *
 * Any key in Vault that matches a Quarkus configuration property name will be automatically loaded.
 * For example, if Vault has a key "quarkus.datasource.password" at the configured path,
 * it will override the property from application.properties
 */
public class VaultConfigMapping {
    // This class is intentionally empty - not needed with current Vault integration
}

