package com.planb.supportticket.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ssm.SsmClient;

@Configuration
public class AwsConfig {

    @Value("${aws.region:us-east-1}")
    private String awsRegion;
    
    @Value("${aws.s3.bucket-name:}")
    private String s3BucketName;
    
    @Bean
    @Profile("!dev")
    public AwsCredentialsProvider awsCredentialsProvider() {
        // In production, use the default credentials provider chain
        // This will look for credentials in environment variables, system properties,
        // credential profiles, and EC2 instance profiles
        return DefaultCredentialsProvider.create();
    }
    
    @Bean
    @Profile("dev")
    public AwsCredentialsProvider devAwsCredentialsProvider(
            @Value("${aws.access-key:}") String accessKey,
            @Value("${aws.secret-key:}") String secretKey) {
        // For development, you can use static credentials if needed
        if (!accessKey.isEmpty() && !secretKey.isEmpty()) {
            return StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(accessKey, secretKey));
        }
        // Fall back to default credentials provider
        return DefaultCredentialsProvider.create();
    }
    
    @Bean
    public S3Client s3Client(AwsCredentialsProvider credentialsProvider) {
        return S3Client.builder()
                .region(Region.of(awsRegion))
                .credentialsProvider(credentialsProvider)
                .build();
    }
    
    @Bean
    public SesClient sesClient(AwsCredentialsProvider credentialsProvider) {
        return SesClient.builder()
                .region(Region.of(awsRegion))
                .credentialsProvider(credentialsProvider)
                .build();
    }
    
    @Bean
    public SsmClient ssmClient(AwsCredentialsProvider credentialsProvider) {
        return SsmClient.builder()
                .region(Region.of(awsRegion))
                .credentialsProvider(credentialsProvider)
                .build();
    }
}
