package com.planb.supportticket.config.aws;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableAsync;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.client.config.ClientAsyncConfiguration;
import software.amazon.awssdk.core.client.config.SdkAdvancedAsyncClientOption;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.ses.SesAsyncClient;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ssm.SsmClient;

import java.time.Duration;
import java.util.concurrent.Executors;

/**
 * AWS Configuration class that provides beans for AWS services.
 * Configures S3, SES, and SSM clients with appropriate credentials and settings.
 */
@Configuration
@EnableAsync
@Slf4j
public class AWSConfig {

    @Value("${aws.region:us-east-1}")
    private String awsRegion;
    
    @Value("${aws.credentials.use-default:true}")
    private boolean useDefaultCredentials;
    
    @Value("${aws.s3.endpoint:}")
    private String s3Endpoint;
    
    @Value("${aws.ses.endpoint:}")
    private String sesEndpoint;
    
    @Value("${aws.ssm.endpoint:}")
    private String ssmEndpoint;
    
    /**
     * Creates an AWS credentials provider based on configuration.
     * Uses default credentials provider in production and can use static credentials in development.
     */
    @Bean
    @Profile("!dev")
    public AwsCredentialsProvider productionAwsCredentialsProvider() {
        log.info("Configuring AWS credentials provider for production environment");
        return DefaultCredentialsProvider.create();
    }
    
    /**
     * Creates an AWS credentials provider for development environment.
     * Can use static credentials if provided, otherwise falls back to default credentials.
     */
    @Bean
    @Profile("dev")
    public AwsCredentialsProvider developmentAwsCredentialsProvider(
            @Value("${aws.access-key:}") String accessKey,
            @Value("${aws.secret-key:}") String secretKey) {
        
        if (!useDefaultCredentials && !accessKey.isEmpty() && !secretKey.isEmpty()) {
            log.info("Using static AWS credentials for development environment");
            return StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(accessKey, secretKey));
        }
        
        log.info("Using default AWS credentials provider for development environment");
        return DefaultCredentialsProvider.create();
    }
    
    /**
     * Creates an S3 client for synchronous operations.
     */
    @Bean
    public S3Client s3Client(AwsCredentialsProvider credentialsProvider) {
        log.info("Creating S3 client for region: {}", awsRegion);
        
        software.amazon.awssdk.services.s3.S3ClientBuilder builder = S3Client.builder()
                .region(Region.of(awsRegion))
                .credentialsProvider(credentialsProvider)
                .overrideConfiguration(c -> c
                        .apiCallTimeout(Duration.ofSeconds(30))
                        .apiCallAttemptTimeout(Duration.ofSeconds(20)));
        
        // Use custom endpoint if provided (useful for testing with localstack)
        if (!s3Endpoint.isEmpty()) {
            log.info("Using custom S3 endpoint: {}", s3Endpoint);
            builder.endpointOverride(java.net.URI.create(s3Endpoint));
        }
        
        return builder.build();
    }
    
    /**
     * Creates an S3 async client for asynchronous operations.
     */
    @Bean
    public S3AsyncClient s3AsyncClient(AwsCredentialsProvider credentialsProvider) {
        log.info("Creating S3 async client for region: {}", awsRegion);
        
        // Create a thread pool for async operations
        ClientAsyncConfiguration asyncConfig = ClientAsyncConfiguration.builder()
                .advancedOption(SdkAdvancedAsyncClientOption.FUTURE_COMPLETION_EXECUTOR, 
                        Executors.newFixedThreadPool(10))
                .build();
        
        software.amazon.awssdk.services.s3.S3AsyncClientBuilder builder = S3AsyncClient.builder()
                .region(Region.of(awsRegion))
                .credentialsProvider(credentialsProvider)
                .asyncConfiguration(asyncConfig)
                .overrideConfiguration(c -> c
                        .apiCallTimeout(Duration.ofSeconds(30))
                        .apiCallAttemptTimeout(Duration.ofSeconds(20)));
        
        // Use custom endpoint if provided (useful for testing with localstack)
        if (!s3Endpoint.isEmpty()) {
            log.info("Using custom S3 async endpoint: {}", s3Endpoint);
            builder.endpointOverride(java.net.URI.create(s3Endpoint));
        }
        
        return builder.build();
    }
    
    /**
     * Creates an SES client for synchronous operations.
     */
    @Bean
    public SesClient sesClient(AwsCredentialsProvider credentialsProvider) {
        log.info("Creating SES client for region: {}", awsRegion);
        
        software.amazon.awssdk.services.ses.SesClientBuilder builder = SesClient.builder()
                .region(Region.of(awsRegion))
                .credentialsProvider(credentialsProvider)
                .overrideConfiguration(c -> c
                        .apiCallTimeout(Duration.ofSeconds(30))
                        .apiCallAttemptTimeout(Duration.ofSeconds(20)));
        
        // Use custom endpoint if provided (useful for testing with localstack)
        if (!sesEndpoint.isEmpty()) {
            log.info("Using custom SES endpoint: {}", sesEndpoint);
            builder.endpointOverride(java.net.URI.create(sesEndpoint));
        }
        
        return builder.build();
    }
    
    /**
     * Creates an SES async client for asynchronous operations.
     */
    @Bean
    public SesAsyncClient sesAsyncClient(AwsCredentialsProvider credentialsProvider) {
        log.info("Creating SES async client for region: {}", awsRegion);
        
        // Create a thread pool for async operations
        ClientAsyncConfiguration asyncConfig = ClientAsyncConfiguration.builder()
                .advancedOption(SdkAdvancedAsyncClientOption.FUTURE_COMPLETION_EXECUTOR, 
                        Executors.newFixedThreadPool(5))
                .build();
        
        software.amazon.awssdk.services.ses.SesAsyncClientBuilder builder = SesAsyncClient.builder()
                .region(Region.of(awsRegion))
                .credentialsProvider(credentialsProvider)
                .asyncConfiguration(asyncConfig)
                .overrideConfiguration(c -> c
                        .apiCallTimeout(Duration.ofSeconds(30))
                        .apiCallAttemptTimeout(Duration.ofSeconds(20)));
        
        // Use custom endpoint if provided (useful for testing with localstack)
        if (!sesEndpoint.isEmpty()) {
            log.info("Using custom SES async endpoint: {}", sesEndpoint);
            builder.endpointOverride(java.net.URI.create(sesEndpoint));
        }
        
        return builder.build();
    }
    
    /**
     * Creates an SSM client for Parameter Store operations.
     */
    @Bean
    public SsmClient ssmClient(AwsCredentialsProvider credentialsProvider) {
        log.info("Creating SSM client for region: {}", awsRegion);
        
        software.amazon.awssdk.services.ssm.SsmClientBuilder builder = SsmClient.builder()
                .region(Region.of(awsRegion))
                .credentialsProvider(credentialsProvider)
                .overrideConfiguration(c -> c
                        .apiCallTimeout(Duration.ofSeconds(30))
                        .apiCallAttemptTimeout(Duration.ofSeconds(20)));
        
        // Use custom endpoint if provided (useful for testing with localstack)
        if (!ssmEndpoint.isEmpty()) {
            log.info("Using custom SSM endpoint: {}", ssmEndpoint);
            builder.endpointOverride(java.net.URI.create(ssmEndpoint));
        }
        
        return builder.build();
    }
}
