package com.example.service.Impl;

import com.example.service.VaultService;
import io.quarkus.vault.VaultKVSecretEngine;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class VaultServiceImpl implements VaultService {

    private static final Logger LOG = Logger.getLogger(VaultServiceImpl.class);
    private static final String BASE_PATH = "url-shortener";

    @Inject
    VaultKVSecretEngine kvEngine;

    @ConfigProperty(name = "app.environment", defaultValue = "dev")
    String environment;

    /**
     * Build full path with environment prefix
     */
    private String buildPath(String path) {
        return String.format("%s/%s/%s", BASE_PATH, environment, path);
    }

    @Override
    public void storeSecret(String path, String key, String value) {
        try {
            String fullPath = buildPath(path);
            Map<String, String> secrets = new HashMap<>();
            secrets.put(key, value);
            kvEngine.writeSecret(fullPath, secrets);
            LOG.infof("Secret stored in Vault: env=%s, path=%s, key=%s", environment, fullPath, key);
        } catch (Exception e) {
            LOG.warnf("Failed to store secret in Vault: path=%s, key=%s - %s", path, key, e.getMessage());
            LOG.debug("Store secret error details:", e);
            // Don't throw - just log and continue
        }
    }

    @Override
    public void storeSecrets(String path, Map<String, String> secrets) {
        try {
            String fullPath = buildPath(path);
            kvEngine.writeSecret(fullPath, secrets);
            LOG.infof("Multiple secrets stored in Vault: env=%s, path=%s, count=%d", environment, fullPath, secrets.size());
        } catch (Exception e) {
            LOG.warnf("Failed to store secrets in Vault: path=%s - %s", path, e.getMessage());
            LOG.debug("Store secrets error details:", e);
            // Don't throw - just log and continue
        }
    }

    @Override
    public String getSecret(String path, String key) {
        try {
            String fullPath = buildPath(path);
            Map<String, String> secrets = kvEngine.readSecret(fullPath);
            if (secrets == null || !secrets.containsKey(key)) {
                LOG.warnf("Secret not found in Vault: env=%s, path=%s, key=%s", environment, fullPath, key);
                return null;
            }
            LOG.debugf("Secret retrieved from Vault: env=%s, path=%s, key=%s", environment, fullPath, key);
            return secrets.get(key);
        } catch (Exception e) {
            LOG.errorf(e, "Failed to retrieve secret from Vault: env=%s, path=%s, key=%s", environment, path, key);
            throw new RuntimeException("Failed to retrieve secret from Vault", e);
        }
    }

    @Override
    public Map<String, String> getSecrets(String path) {
        try {
            String fullPath = buildPath(path);
            Map<String, String> secrets = kvEngine.readSecret(fullPath);
            LOG.debugf("Secrets retrieved from Vault: env=%s, path=%s, count=%d",
                environment, fullPath, secrets != null ? secrets.size() : 0);
            return secrets != null ? secrets : new HashMap<>();
        } catch (Exception e) {
            LOG.errorf(e, "Failed to retrieve secrets from Vault: env=%s, path=%s", environment, path);
            throw new RuntimeException("Failed to retrieve secrets from Vault", e);
        }
    }

    @Override
    public void deleteSecret(String path) {
        try {
            String fullPath = buildPath(path);
            kvEngine.deleteSecret(fullPath);
            LOG.infof("Secret deleted from Vault: env=%s, path=%s", environment, fullPath);
        } catch (Exception e) {
            LOG.errorf(e, "Failed to delete secret from Vault: env=%s, path=%s", environment, path);
            throw new RuntimeException("Failed to delete secret from Vault", e);
        }
    }

    @Override
    public boolean secretExists(String path, String key) {
        try {
            String fullPath = buildPath(path);
            Map<String, String> secrets = kvEngine.readSecret(fullPath);
            boolean exists = secrets != null && secrets.containsKey(key);
            LOG.debugf("Secret existence check: env=%s, path=%s, key=%s, exists=%s", environment, fullPath, key, exists);
            return exists;
        } catch (Exception e) {
            LOG.debugf("Secret does not exist or error occurred: env=%s, path=%s, key=%s", environment, path, key);
            return false;
        }
    }
}

