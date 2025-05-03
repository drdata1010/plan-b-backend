package com.planb.supportticket.config.gcp;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableAsync;

import java.io.FileInputStream;
import java.io.IOException;

/**
 * GCP Configuration class that provides beans for Google Cloud Platform services.
 * Configures GCS (Google Cloud Storage), Secret Manager, and other GCP services.
 */
@Configuration
@EnableAsync
@Slf4j
public class GCPConfig {

    @Value("${gcp.project-id}")
    private String projectId;

    @Value("${gcp.credentials.use-default:true}")
    private boolean useDefaultCredentials;

    @Value("${gcp.credentials.path:}")
    private String credentialsPath;

    /**
     * Creates Google credentials based on configuration.
     * Uses default credentials provider in production and can use service account credentials in development.
     */
    @Bean
    @Primary
    public GoogleCredentials googleCredentials() throws IOException {
        if (!useDefaultCredentials && !credentialsPath.isEmpty()) {
            log.info("Using service account credentials from: {}", credentialsPath);
            return GoogleCredentials.fromStream(new FileInputStream(credentialsPath));
        }

        try {
            log.info("Using default Google credentials");
            return GoogleCredentials.getApplicationDefault();
        } catch (IOException e) {
            log.warn("Default Google credentials not found. Using mock credentials for local development");
            // Create a mock GoogleCredentials for local development
            return GoogleCredentials.newBuilder().build();
        }
    }

    /**
     * Creates a Google Cloud Storage client.
     */
    @Bean
    public Storage storageClient(GoogleCredentials credentials) {
        log.info("Creating Google Cloud Storage client for project: {}", projectId);

        return StorageOptions.newBuilder()
                .setProjectId(projectId)
                .setCredentials(credentials)
                .build()
                .getService();
    }

    /**
     * Creates a Secret Manager client for secrets operations.
     */
    @Bean
    public SecretManagerServiceClient secretManagerClient(GoogleCredentials credentials) throws IOException {
        log.info("Creating Secret Manager client for project: {}", projectId);

        try {
            return SecretManagerServiceClient.create();
        } catch (IOException e) {
            log.warn("Failed to create Secret Manager client: {}", e.getMessage());
            log.warn("Using mock Secret Manager client for local development");
            return new MockSecretManagerServiceClient(); // Return a mock implementation for local development
        }
    }

    /**
     * Mock implementation of SecretManagerServiceClient for local development.
     */
    private static class MockSecretManagerServiceClient extends SecretManagerServiceClient {
        @Override
        public AccessSecretVersionResponse accessSecretVersion(SecretVersionName name) {
            // Return a mock response
            return AccessSecretVersionResponse.newBuilder()
                    .setPayload(SecretPayload.newBuilder()
                            .setData(com.google.protobuf.ByteString.copyFromUtf8("mock-" + name.getSecret() + "-value"))
                            .build())
                    .build();
        }

        @Override
        public void close() {
            // Do nothing
        }
    }

    /**
     * Development-specific configuration.
     */
    @Bean
    @Profile("dev")
    public String devEnvironmentSetup() {
        log.info("GCP configuration initialized for DEVELOPMENT environment");
        log.info("Project ID: {}", projectId);
        return "gcpDevEnvironmentSetup";
    }

    /**
     * Production-specific configuration.
     */
    @Bean
    @Profile("prod")
    public String prodEnvironmentSetup() {
        log.info("GCP configuration initialized for PRODUCTION environment");
        log.info("Project ID: {}", projectId);
        return "gcpProdEnvironmentSetup";
    }
}
