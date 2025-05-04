package com.planb.supportticket.service;

import com.google.cloud.secretmanager.v1.AccessSecretVersionResponse;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretName;
import com.google.cloud.secretmanager.v1.SecretPayload;
import com.google.cloud.secretmanager.v1.SecretVersionName;
import com.google.protobuf.ByteString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for accessing secrets from Google Secret Manager.
 */
@Service
@Slf4j
public class SecretManagerService {

    @Autowired(required = false)
    private SecretManagerServiceClient secretManagerClient;

    @Value("${gcp.project-id}")
    private String projectId;

    @Value("${gcp.secret-manager.cache-ttl-seconds:300}")
    private long cacheTtlSeconds;

    // Cache for secrets to reduce API calls
    private final Map<String, CachedSecret> secretCache = new ConcurrentHashMap<>();

    /**
     * Gets a secret from Google Secret Manager.
     * Uses cache if available and not expired.
     *
     * @param secretId The ID of the secret
     * @return An Optional containing the secret value, or empty if not found
     */
    public Optional<String> getSecret(String secretId) {
        return getSecret(secretId, false);
    }

    /**
     * Gets a secret from Google Secret Manager.
     * Uses cache if available and not expired.
     *
     * @param secretId The ID of the secret
     * @param forceRefresh Whether to force a refresh from Secret Manager
     * @return An Optional containing the secret value, or empty if not found
     */
    public Optional<String> getSecret(String secretId, boolean forceRefresh) {
        // Check cache first if not forcing refresh
        if (!forceRefresh) {
            CachedSecret cachedSecret = secretCache.get(secretId);
            if (cachedSecret != null && !cachedSecret.isExpired()) {
                log.debug("Using cached secret: {}", secretId);
                return Optional.of(cachedSecret.getValue());
            }
        }

        // Check if Secret Manager client is available
        if (secretManagerClient == null) {
            log.warn("Secret Manager client is not available. Using mock value for secret: {}", secretId);
            // Return a mock value for local development
            String mockValue = "mock-" + secretId + "-value";
            updateCache(secretId, mockValue);
            return Optional.of(mockValue);
        }

        // Retrieve from Secret Manager
        try {
            SecretVersionName secretVersionName = SecretVersionName.of(projectId, secretId, "latest");
            AccessSecretVersionResponse response = secretManagerClient.accessSecretVersion(secretVersionName);
            String secretValue = response.getPayload().getData().toStringUtf8();

            // Update cache
            updateCache(secretId, secretValue);

            log.info("Successfully retrieved secret: {}", secretId);
            return Optional.of(secretValue);
        } catch (Exception e) {
            log.error("Error retrieving secret {}: {}", secretId, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Creates or updates a secret in Google Secret Manager.
     *
     * @param secretId The ID of the secret
     * @param secretValue The value of the secret
     * @return true if successful, false otherwise
     */
    public boolean createOrUpdateSecret(String secretId, String secretValue) {
        // Check if Secret Manager client is available
        if (secretManagerClient == null) {
            log.warn("Secret Manager client is not available. Using mock implementation for secret: {}", secretId);
            // Just update the cache for local development
            updateCache(secretId, secretValue);
            return true;
        }

        try {
            // Check if secret exists
            SecretName secretName = SecretName.of(projectId, secretId);

            try {
                secretManagerClient.getSecret(secretName);
                // Secret exists, add a new version
                SecretPayload payload = SecretPayload.newBuilder()
                        .setData(ByteString.copyFromUtf8(secretValue))
                        .build();

                secretManagerClient.addSecretVersion(secretName, payload);
            } catch (Exception e) {
                // Secret doesn't exist, create it
                com.google.cloud.secretmanager.v1.Secret secret = com.google.cloud.secretmanager.v1.Secret.newBuilder()
                        .setReplication(com.google.cloud.secretmanager.v1.Replication.newBuilder()
                                .setAutomatic(com.google.cloud.secretmanager.v1.Replication.Automatic.getDefaultInstance())
                                .build())
                        .build();

                secretManagerClient.createSecret(
                        com.google.cloud.secretmanager.v1.ProjectName.of(projectId),
                        secretId,
                        secret
                );

                // Add the first version
                SecretPayload payload = SecretPayload.newBuilder()
                        .setData(ByteString.copyFromUtf8(secretValue))
                        .build();

                secretManagerClient.addSecretVersion(secretName, payload);
            }

            // Update cache
            updateCache(secretId, secretValue);

            log.info("Successfully created/updated secret: {}", secretId);
            return true;
        } catch (Exception e) {
            log.error("Error creating/updating secret {}: {}", secretId, e.getMessage());
            return false;
        }
    }

    /**
     * Deletes a secret from Google Secret Manager.
     *
     * @param secretId The ID of the secret
     * @return true if successful, false otherwise
     */
    public boolean deleteSecret(String secretId) {
        // Check if Secret Manager client is available
        if (secretManagerClient == null) {
            log.warn("Secret Manager client is not available. Using mock implementation for deleting secret: {}", secretId);
            // Just remove from the cache for local development
            secretCache.remove(secretId);
            return true;
        }

        try {
            SecretName secretName = SecretName.of(projectId, secretId);
            secretManagerClient.deleteSecret(secretName);

            // Remove from cache
            secretCache.remove(secretId);

            log.info("Successfully deleted secret: {}", secretId);
            return true;
        } catch (Exception e) {
            log.error("Error deleting secret {}: {}", secretId, e.getMessage());
            return false;
        }
    }

    /**
     * Clears the secret cache.
     */
    public void clearCache() {
        log.info("Clearing secret cache");
        secretCache.clear();
    }

    /**
     * Updates the secret cache with a new value.
     *
     * @param secretId The ID of the secret
     * @param value The secret value
     */
    private void updateCache(String secretId, String value) {
        secretCache.put(secretId, new CachedSecret(value, System.currentTimeMillis() + (cacheTtlSeconds * 1000)));
    }

    /**
     * Class to hold a cached secret with expiration.
     */
    private static class CachedSecret {
        private final String value;
        private final long expirationTime;

        public CachedSecret(String value, long expirationTime) {
            this.value = value;
            this.expirationTime = expirationTime;
        }

        public String getValue() {
            return value;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() > expirationTime;
        }
    }
}
