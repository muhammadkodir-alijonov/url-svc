package com.example.service.Impl;

import com.example.service.VaultService;
import io.quarkus.vault.VaultKVSecretEngine;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class VaultServiceImpl implements VaultService {

    private static final Logger LOG = Logger.getLogger(VaultServiceImpl.class);

    @Inject
    VaultKVSecretEngine kvEngine;

    @Override
    public void storeSecret(String path, String key, String value) {
        try {
            Map<String, String> secrets = new HashMap<>();
            secrets.put(key, value);
            kvEngine.writeSecret(path, secrets);
            LOG.infof("Secret stored in Vault: path=%s, key=%s", path, key);
        } catch (Exception e) {
            LOG.warnf("Failed to store secret in Vault: path=%s, key=%s - %s", path, key, e.getMessage());
            LOG.debug("Store secret error details:", e);
            // Don't throw - just log and continue
        }
    }

    @Override
    public void storeSecrets(String path, Map<String, String> secrets) {
        try {
            kvEngine.writeSecret(path, secrets);
            LOG.infof("Multiple secrets stored in Vault: path=%s, count=%d", path, secrets.size());
        } catch (Exception e) {
            LOG.warnf("Failed to store secrets in Vault: path=%s - %s", path, e.getMessage());
            LOG.debug("Store secrets error details:", e);
            // Don't throw - just log and continue
        }
    }

    @Override
    public String getSecret(String path, String key) {
        try {
            Map<String, String> secrets = kvEngine.readSecret(path);
            if (secrets == null || !secrets.containsKey(key)) {
                LOG.warnf("Secret not found in Vault: path=%s, key=%s", path, key);
                return null;
            }
            LOG.debugf("Secret retrieved from Vault: path=%s, key=%s", path, key);
            return secrets.get(key);
        } catch (Exception e) {
            LOG.errorf(e, "Failed to retrieve secret from Vault: path=%s, key=%s", path, key);
            throw new RuntimeException("Failed to retrieve secret from Vault", e);
        }
    }

    @Override
    public Map<String, String> getSecrets(String path) {
        try {
            Map<String, String> secrets = kvEngine.readSecret(path);
            LOG.debugf("Secrets retrieved from Vault: path=%s, count=%d",
                path, secrets != null ? secrets.size() : 0);
            return secrets != null ? secrets : new HashMap<>();
        } catch (Exception e) {
            LOG.errorf(e, "Failed to retrieve secrets from Vault: path=%s", path);
            throw new RuntimeException("Failed to retrieve secrets from Vault", e);
        }
    }

    @Override
    public void deleteSecret(String path) {
        try {
            kvEngine.deleteSecret(path);
            LOG.infof("Secret deleted from Vault: path=%s", path);
        } catch (Exception e) {
            LOG.errorf(e, "Failed to delete secret from Vault: path=%s", path);
            throw new RuntimeException("Failed to delete secret from Vault", e);
        }
    }

    @Override
    public boolean secretExists(String path, String key) {
        try {
            Map<String, String> secrets = kvEngine.readSecret(path);
            boolean exists = secrets != null && secrets.containsKey(key);
            LOG.debugf("Secret existence check: path=%s, key=%s, exists=%s", path, key, exists);
            return exists;
        } catch (Exception e) {
            LOG.debugf("Secret does not exist or error occurred: path=%s, key=%s", path, key);
            return false;
        }
    }
}

