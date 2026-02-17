package com.example.service;

import java.util.Map;

public interface VaultService {

    void storeSecret(String path, String key, String value);

    void storeSecrets(String path, Map<String, String> secrets);

    String getSecret(String path, String key);

    Map<String, String> getSecrets(String path);

    void deleteSecret(String path);

    boolean secretExists(String path, String key);
}

