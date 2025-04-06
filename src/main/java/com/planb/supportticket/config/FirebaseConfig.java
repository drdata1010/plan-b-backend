package com.planb.supportticket.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;

/**
 * Configuration for Firebase integration.
 * Initializes Firebase SDK and provides Firebase beans.
 */
@Configuration
public class FirebaseConfig {

    private static final Logger logger = LoggerFactory.getLogger(FirebaseConfig.class);

    @Value("${firebase.credentials.path:firebase-service-account.json}")
    private String firebaseCredentialsPath;

    @Value("${firebase.database.url:}")
    private String firebaseDatabaseUrl;

    @Value("${firebase.enabled:false}")
    private boolean firebaseEnabled;

    /**
     * Initializes Firebase SDK and creates a FirebaseApp bean.
     *
     * @return the FirebaseApp instance
     */
    @Bean
    public FirebaseApp firebaseApp() {
        if (!firebaseEnabled) {
            logger.warn("Firebase integration is disabled");
            return null;
        }

        try {
            Resource resource = new ClassPathResource(firebaseCredentialsPath);

            // Check if the resource exists
            if (!resource.exists()) {
                logger.warn("Firebase credentials file not found: {}", firebaseCredentialsPath);
                return null;
            }

            InputStream serviceAccount = resource.getInputStream();

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setDatabaseUrl(firebaseDatabaseUrl)
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp app = FirebaseApp.initializeApp(options);
                logger.info("Firebase application has been initialized");
                return app;
            } else {
                logger.info("Firebase application already initialized");
                return FirebaseApp.getInstance();
            }
        } catch (IOException e) {
            logger.warn("Error initializing Firebase application: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Creates a FirebaseAuth bean.
     * Only created when firebase.enabled=true.
     *
     * @return the FirebaseAuth instance or null if Firebase is disabled
     */
    @Bean
    @ConditionalOnProperty(name = "firebase.enabled", havingValue = "true")
    public FirebaseAuth firebaseAuth() {
        if (!firebaseEnabled) {
            logger.warn("Firebase Auth is disabled");
            return null;
        }

        try {
            return FirebaseAuth.getInstance();
        } catch (Exception e) {
            logger.warn("Error getting FirebaseAuth instance: {}", e.getMessage());
            return null;
        }
    }
}
