# AWS Configuration Guide

This document explains how to configure and use the AWS services in the Support Ticket System.

## Overview

The application integrates with the following AWS services:

- **S3** - For file storage (attachments, profile pictures, etc.)
- **SES** - For sending emails (notifications, alerts, etc.)
- **Parameter Store** - For secure configuration and secrets management

## Configuration

AWS services are configured in `application.yml`:

```yaml
# AWS Configuration
aws:
  region: ${AWS_REGION:us-east-1}
  access-key: ${AWS_ACCESS_KEY:}
  secret-key: ${AWS_SECRET_KEY:}
  credentials:
    use-default: ${AWS_USE_DEFAULT_CREDENTIALS:true}
  s3:
    bucket-name: ${AWS_S3_BUCKET_NAME:support-ticket-system-files}
    endpoint: ${AWS_S3_ENDPOINT:}
    presigned-url-expiration: ${AWS_S3_PRESIGNED_URL_EXPIRATION:3600}
  ses:
    from-email: ${AWS_SES_FROM_EMAIL:no-reply@example.com}
    reply-to-email: ${AWS_SES_REPLY_TO_EMAIL:}
    endpoint: ${AWS_SES_ENDPOINT:}
  ssm:
    parameter-path: ${AWS_SSM_PARAMETER_PATH:/support-ticket-system/}
    endpoint: ${AWS_SSM_ENDPOINT:}
    cache-ttl-seconds: ${AWS_SSM_CACHE_TTL_SECONDS:300}
    preload-parameters: ${AWS_SSM_PRELOAD_PARAMETERS:false}
```

## Environment Variables

The following environment variables can be set to configure AWS services:

### General AWS Configuration
- `AWS_REGION` - AWS region (default: us-east-1)
- `AWS_ACCESS_KEY` - AWS access key (optional, uses default credentials provider if not set)
- `AWS_SECRET_KEY` - AWS secret key (optional, uses default credentials provider if not set)
- `AWS_USE_DEFAULT_CREDENTIALS` - Whether to use the default credentials provider (default: true)

### S3 Configuration
- `AWS_S3_BUCKET_NAME` - S3 bucket name (default: support-ticket-system-files)
- `AWS_S3_ENDPOINT` - Custom S3 endpoint URL (optional, for testing with localstack)
- `AWS_S3_PRESIGNED_URL_EXPIRATION` - Expiration time in seconds for presigned URLs (default: 3600)

### SES Configuration
- `AWS_SES_FROM_EMAIL` - Email address to send emails from (default: no-reply@example.com)
- `AWS_SES_REPLY_TO_EMAIL` - Reply-to email address (optional)
- `AWS_SES_ENDPOINT` - Custom SES endpoint URL (optional, for testing with localstack)

### Parameter Store Configuration
- `AWS_SSM_PARAMETER_PATH` - Base path for parameters in Parameter Store (default: /support-ticket-system/)
- `AWS_SSM_ENDPOINT` - Custom SSM endpoint URL (optional, for testing with localstack)
- `AWS_SSM_CACHE_TTL_SECONDS` - Time-to-live for parameter cache in seconds (default: 300)
- `AWS_SSM_PRELOAD_PARAMETERS` - Whether to preload parameters at startup (default: false)

## AWS Credentials

The application supports two methods for providing AWS credentials:

1. **Environment Variables** - Set `AWS_ACCESS_KEY` and `AWS_SECRET_KEY` environment variables
2. **Default Credentials Provider** - Uses the AWS SDK's default credentials provider chain:
   - Environment variables (`AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`)
   - Java system properties (`aws.accessKeyId`, `aws.secretKey`)
   - Credential profiles file (~/.aws/credentials)
   - EC2 instance profile or ECS task role

For production environments, it's recommended to use the default credentials provider with IAM roles.

## Service Classes

### S3Service

The `S3Service` class provides methods for file operations:

```java
// Synchronous operations
String key = s3Service.uploadFile(multipartFile, "tickets");
byte[] fileContent = s3Service.downloadFile(key);
s3Service.deleteFile(key);
String presignedUrl = s3Service.generatePresignedUrl(key);
boolean exists = s3Service.fileExists(key);
List<S3Object> files = s3Service.listFiles("tickets");

// Asynchronous operations
CompletableFuture<String> keyFuture = s3Service.uploadFileAsync(multipartFile, "tickets");
CompletableFuture<byte[]> contentFuture = s3Service.downloadFileAsync(key);
CompletableFuture<Void> deleteFuture = s3Service.deleteFileAsync(key);
```

### SESService

The `SESService` class provides methods for sending emails:

```java
// Synchronous operations
String messageId = sesService.sendEmail("user@example.com", "Subject", "<p>HTML Body</p>");
String messageId2 = sesService.sendEmail(
    List.of("user@example.com"), 
    List.of("cc@example.com"), 
    List.of("bcc@example.com"), 
    "Subject", 
    "<p>HTML Body</p>", 
    "Plain text body"
);
String messageId3 = sesService.sendTemplatedEmail(
    "user@example.com", 
    "TemplateNameInSES", 
    "{\"name\":\"John\",\"link\":\"https://example.com\"}"
);

// Asynchronous operations
CompletableFuture<String> messageIdFuture = sesService.sendEmailAsync(
    "user@example.com", 
    "Subject", 
    "<p>HTML Body</p>"
);
```

### AWSParameterStoreConfig

The `AWSParameterStoreConfig` class provides methods for retrieving configuration and secrets:

```java
// Get a single parameter
Optional<String> apiKey = parameterStoreConfig.getParameter("api-key");

// Get a secret (same as getParameter but semantically different)
Optional<String> dbPassword = parameterStoreConfig.getSecret("database/password");

// Get all parameters under a path
Map<String, String> emailConfig = parameterStoreConfig.getParametersByPath("email");

// Force refresh from Parameter Store (bypass cache)
Optional<String> freshApiKey = parameterStoreConfig.getParameter("api-key", true);

// Clear the parameter cache
parameterStoreConfig.clearCache();
```

## Error Handling

All AWS service operations throw `AWSServiceException` in case of errors. This exception includes:

- The AWS service name (S3, SES, SSM)
- The operation name (uploadFile, sendEmail, getParameter)
- A detailed error message
- The original exception as the cause

Example error handling:

```java
try {
    String key = s3Service.uploadFile(multipartFile, "tickets");
    // Process the uploaded file
} catch (AWSServiceException e) {
    log.error("AWS service error: {}", e.getMessage());
    // Handle the error appropriately
}
```

## Async Operations

Asynchronous operations return `CompletableFuture` objects that can be composed and chained:

```java
s3Service.uploadFileAsync(multipartFile, "tickets")
    .thenCompose(key -> {
        // File uploaded, now send an email notification
        return sesService.sendEmailAsync(
            "user@example.com",
            "File Uploaded",
            "<p>Your file has been uploaded with key: " + key + "</p>"
        );
    })
    .thenAccept(messageId -> {
        // Email sent successfully
        log.info("File uploaded and notification sent with message ID: {}", messageId);
    })
    .exceptionally(e -> {
        // Handle any errors in the chain
        log.error("Error in async operation chain: {}", e.getMessage());
        return null;
    });
```

## Testing with LocalStack

For local development and testing, you can use LocalStack to emulate AWS services:

1. Start LocalStack with the required services:
   ```
   docker run -d -p 4566:4566 -e SERVICES=s3,ses,ssm localstack/localstack
   ```

2. Configure the application to use LocalStack endpoints:
   ```yaml
   aws:
     region: us-east-1
     s3:
       endpoint: http://localhost:4566
     ses:
       endpoint: http://localhost:4566
     ssm:
       endpoint: http://localhost:4566
   ```

3. Create the S3 bucket in LocalStack:
   ```
   aws --endpoint-url=http://localhost:4566 s3 mb s3://support-ticket-system-files
   ```

4. Add test parameters to Parameter Store:
   ```
   aws --endpoint-url=http://localhost:4566 ssm put-parameter --name "/support-ticket-system/test-param" --value "test-value" --type String
   ```
