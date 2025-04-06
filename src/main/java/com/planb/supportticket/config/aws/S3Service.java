package com.planb.supportticket.config.aws;

import com.planb.supportticket.exception.AWSServiceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.core.async.AsyncResponseTransformer;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Service for interacting with AWS S3.
 * Provides methods for file operations with both synchronous and asynchronous support.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class S3Service {

    private final S3Client s3Client;
    private final S3AsyncClient s3AsyncClient;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${aws.s3.presigned-url-expiration:3600}")
    private long presignedUrlExpirationSeconds;

    /**
     * Uploads a file to S3 synchronously.
     *
     * @param file The file to upload
     * @param folder The folder to upload to (e.g., "tickets", "profiles")
     * @return The S3 key of the uploaded file
     * @throws AWSServiceException if the upload fails
     */
    public String uploadFile(MultipartFile file, String folder) {
        String key = generateKey(file.getOriginalFilename(), folder);

        try {
            PutObjectRequest request = buildPutObjectRequest(key, file.getContentType());

            s3Client.putObject(request, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
            log.info("Successfully uploaded file to S3: {}", key);
            return key;
        } catch (IOException e) {
            log.error("Failed to read file for S3 upload: {}", e.getMessage());
            throw new AWSServiceException("S3", "uploadFile", "Failed to read file for upload", e);
        } catch (S3Exception e) {
            log.error("S3 error during file upload: {}", e.getMessage());
            throw new AWSServiceException("S3", "uploadFile", e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error during S3 file upload: {}", e.getMessage());
            throw new AWSServiceException("S3", "uploadFile", "Unexpected error during upload", e);
        }
    }

    /**
     * Uploads a file to S3 asynchronously.
     *
     * @param file The file to upload
     * @param folder The folder to upload to (e.g., "tickets", "profiles")
     * @return A CompletableFuture that will complete with the S3 key of the uploaded file
     */
    @Async
    public CompletableFuture<String> uploadFileAsync(MultipartFile file, String folder) {
        String key = generateKey(file.getOriginalFilename(), folder);

        try {
            PutObjectRequest request = buildPutObjectRequest(key, file.getContentType());

            return s3AsyncClient.putObject(
                    request,
                    // Modified to use the correct method signature
                    AsyncRequestBody.fromInputStream(file.getInputStream(), file.getSize(), java.util.concurrent.Executors.newSingleThreadExecutor())
                )
                .thenApply(response -> {
                    log.info("Successfully uploaded file to S3 asynchronously: {}", key);
                    return key;
                })
                .exceptionally(e -> {
                    log.error("Async S3 upload failed: {}", e.getMessage());
                    throw new AWSServiceException("S3", "uploadFileAsync", "Async upload failed", e);
                });
        } catch (IOException e) {
            log.error("Failed to read file for async S3 upload: {}", e.getMessage());
            CompletableFuture<String> future = new CompletableFuture<>();
            future.completeExceptionally(
                    new AWSServiceException("S3", "uploadFileAsync", "Failed to read file for upload", e));
            return future;
        }
    }

    /**
     * Downloads a file from S3 synchronously.
     *
     * @param key The S3 key of the file to download
     * @return The file content as a byte array
     * @throws AWSServiceException if the download fails
     */
    public byte[] downloadFile(String key) {
        try {
            GetObjectRequest request = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            ResponseBytes<GetObjectResponse> objectBytes = s3Client.getObjectAsBytes(request);
            log.info("Successfully downloaded file from S3: {}", key);
            return objectBytes.asByteArray();
        } catch (NoSuchKeyException e) {
            log.error("File not found in S3: {}", key);
            throw new AWSServiceException("S3", "downloadFile", "File not found: " + key, e);
        } catch (S3Exception e) {
            log.error("S3 error during file download: {}", e.getMessage());
            throw new AWSServiceException("S3", "downloadFile", e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error during S3 file download: {}", e.getMessage());
            throw new AWSServiceException("S3", "downloadFile", "Unexpected error during download", e);
        }
    }

    /**
     * Downloads a file from S3 asynchronously.
     *
     * @param key The S3 key of the file to download
     * @return A CompletableFuture that will complete with the file content as a byte array
     */
    @Async
    public CompletableFuture<byte[]> downloadFileAsync(String key) {
        try {
            GetObjectRequest request = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            return s3AsyncClient.getObject(request, AsyncResponseTransformer.toBytes())
                    .thenApply(responseBytes -> {
                        log.info("Successfully downloaded file from S3 asynchronously: {}", key);
                        return responseBytes.asByteArray();
                    })
                    .exceptionally(e -> {
                        log.error("Async S3 download failed: {}", e.getMessage());
                        throw new AWSServiceException("S3", "downloadFileAsync", "Async download failed", e);
                    });
        } catch (Exception e) {
            log.error("Error initiating async S3 download: {}", e.getMessage());
            CompletableFuture<byte[]> future = new CompletableFuture<>();
            future.completeExceptionally(
                    new AWSServiceException("S3", "downloadFileAsync", "Failed to initiate download", e));
            return future;
        }
    }

    /**
     * Deletes a file from S3 synchronously.
     *
     * @param key The S3 key of the file to delete
     * @throws AWSServiceException if the deletion fails
     */
    public void deleteFile(String key) {
        try {
            DeleteObjectRequest request = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.deleteObject(request);
            log.info("Successfully deleted file from S3: {}", key);
        } catch (S3Exception e) {
            log.error("S3 error during file deletion: {}", e.getMessage());
            throw new AWSServiceException("S3", "deleteFile", e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error during S3 file deletion: {}", e.getMessage());
            throw new AWSServiceException("S3", "deleteFile", "Unexpected error during deletion", e);
        }
    }

    /**
     * Deletes a file from S3 asynchronously.
     *
     * @param key The S3 key of the file to delete
     * @return A CompletableFuture<Object> that will complete when the deletion is done
     */
    @Async
    public CompletableFuture<Object> deleteFileAsync(String key) {
        try {
            DeleteObjectRequest request = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            return s3AsyncClient.deleteObject(request)
                    .thenApply(response -> {
                        log.info("Successfully deleted file from S3 asynchronously: {}", key);
                        return null;
                    })
                    .exceptionally(e -> {
                        log.error("Async S3 deletion failed: {}", e.getMessage());
                        throw new AWSServiceException("S3", "deleteFileAsync", "Async deletion failed", e);
                    });
                    // Cast to CompletableFuture<Void> to fix type compatibility
        } catch (Exception e) {
            log.error("Error initiating async S3 deletion: {}", e.getMessage());
            CompletableFuture<Object> future = new CompletableFuture<>();
            future.completeExceptionally(
                    new AWSServiceException("S3", "deleteFileAsync", "Failed to initiate deletion", e));
            return future;
        }
    }

    /**
     * Deletes multiple files from S3 in a single batch operation.
     *
     * @param keys List of S3 keys to delete
     * @throws AWSServiceException if the batch deletion fails
     */
    public void deleteFiles(List<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return;
        }

        try {
            List<ObjectIdentifier> objectIds = keys.stream()
                    .map(key -> ObjectIdentifier.builder().key(key).build())
                    .collect(Collectors.toList());

            DeleteObjectsRequest request = DeleteObjectsRequest.builder()
                    .bucket(bucketName)
                    .delete(Delete.builder().objects(objectIds).build())
                    .build();

            DeleteObjectsResponse response = s3Client.deleteObjects(request);

            if (response.hasErrors()) {
                List<String> failedKeys = response.errors().stream()
                        .map(error -> error.key() + " (" + error.code() + ": " + error.message() + ")")
                        .collect(Collectors.toList());

                log.error("Failed to delete some files from S3: {}", String.join(", ", failedKeys));
                throw new AWSServiceException("S3", "deleteFiles",
                        "Failed to delete some files: " + String.join(", ", failedKeys));
            }

            log.info("Successfully deleted {} files from S3", keys.size());
        } catch (S3Exception e) {
            log.error("S3 error during batch file deletion: {}", e.getMessage());
            throw new AWSServiceException("S3", "deleteFiles", e.getMessage(), e);
        } catch (AWSServiceException e) {
            throw e; // Re-throw our custom exception
        } catch (Exception e) {
            log.error("Unexpected error during S3 batch file deletion: {}", e.getMessage());
            throw new AWSServiceException("S3", "deleteFiles", "Unexpected error during batch deletion", e);
        }
    }

    /**
     * Generates a pre-signed URL for downloading a file from S3.
     *
     * @param key The S3 key of the file
     * @return The pre-signed URL as a string
     * @throws AWSServiceException if generating the URL fails
     */
    public String generatePresignedUrl(String key) {
        return generatePresignedUrl(key, Duration.ofSeconds(presignedUrlExpirationSeconds));
    }

    /**
     * Generates a pre-signed URL for downloading a file from S3 with a custom expiration.
     *
     * @param key The S3 key of the file
     * @param expiration The expiration duration for the URL
     * @return The pre-signed URL as a string
     * @throws AWSServiceException if generating the URL fails
     */
    public String generatePresignedUrl(String key, Duration expiration) {
        try (S3Presigner presigner = S3Presigner.builder()
                .region(s3Client.serviceClientConfiguration().region())
                .credentialsProvider(s3Client.serviceClientConfiguration().credentialsProvider())
                .build()) {

            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(expiration)
                    .getObjectRequest(getObjectRequest)
                    .build();

            PresignedGetObjectRequest presignedRequest = presigner.presignGetObject(presignRequest);
            URL url = presignedRequest.url();

            log.info("Generated pre-signed URL for S3 object: {} (expires in {} seconds)",
                    key, expiration.getSeconds());

            return url.toString();
        } catch (S3Exception e) {
            log.error("S3 error generating pre-signed URL: {}", e.getMessage());
            throw new AWSServiceException("S3", "generatePresignedUrl", e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error generating S3 pre-signed URL: {}", e.getMessage());
            throw new AWSServiceException("S3", "generatePresignedUrl", "Unexpected error generating URL", e);
        }
    }

    /**
     * Checks if a file exists in S3.
     *
     * @param key The S3 key of the file
     * @return true if the file exists, false otherwise
     */
    public boolean fileExists(String key) {
        try {
            HeadObjectRequest request = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.headObject(request);
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        } catch (S3Exception e) {
            log.error("S3 error checking if file exists: {}", e.getMessage());
            throw new AWSServiceException("S3", "fileExists", e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error checking if S3 file exists: {}", e.getMessage());
            throw new AWSServiceException("S3", "fileExists", "Unexpected error checking file existence", e);
        }
    }

    /**
     * Lists all files in a folder.
     *
     * @param folder The folder to list files from
     * @return A list of S3 object summaries
     * @throws AWSServiceException if listing the files fails
     */
    public List<S3Object> listFiles(String folder) {
        try {
            String prefix = folder;
            if (!prefix.endsWith("/")) {
                prefix += "/";
            }

            ListObjectsV2Request request = ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .prefix(prefix)
                    .build();

            ListObjectsV2Response response = s3Client.listObjectsV2(request);
            log.info("Listed {} files from S3 folder: {}", response.contents().size(), folder);

            return response.contents();
        } catch (S3Exception e) {
            log.error("S3 error listing files: {}", e.getMessage());
            throw new AWSServiceException("S3", "listFiles", e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error listing S3 files: {}", e.getMessage());
            throw new AWSServiceException("S3", "listFiles", "Unexpected error listing files", e);
        }
    }

    /**
     * Uploads an InputStream to S3.
     *
     * @param inputStream The input stream to upload
     * @param contentLength The length of the content in bytes
     * @param contentType The content type (MIME type) of the file
     * @param folder The folder to upload to
     * @param filename The name of the file
     * @return The S3 key of the uploaded file
     * @throws AWSServiceException if the upload fails
     */
    public String uploadStream(InputStream inputStream, long contentLength, String contentType,
                              String folder, String filename) {
        String key = generateKey(filename, folder);

        try {
            PutObjectRequest request = buildPutObjectRequest(key, contentType);

            s3Client.putObject(request, RequestBody.fromInputStream(inputStream, contentLength));
            log.info("Successfully uploaded stream to S3: {}", key);
            return key;
        } catch (S3Exception e) {
            log.error("S3 error during stream upload: {}", e.getMessage());
            throw new AWSServiceException("S3", "uploadStream", e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error during S3 stream upload: {}", e.getMessage());
            throw new AWSServiceException("S3", "uploadStream", "Unexpected error during upload", e);
        }
    }

    /**
     * Copies a file within S3.
     *
     * @param sourceKey The source S3 key
     * @param destinationKey The destination S3 key
     * @throws AWSServiceException if the copy operation fails
     */
    public void copyFile(String sourceKey, String destinationKey) {
        try {
            CopyObjectRequest request = CopyObjectRequest.builder()
                    .sourceBucket(bucketName)
                    .sourceKey(sourceKey)
                    .destinationBucket(bucketName)
                    .destinationKey(destinationKey)
                    .build();

            s3Client.copyObject(request);
            log.info("Successfully copied S3 file from {} to {}", sourceKey, destinationKey);
        } catch (S3Exception e) {
            log.error("S3 error during file copy: {}", e.getMessage());
            throw new AWSServiceException("S3", "copyFile", e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error during S3 file copy: {}", e.getMessage());
            throw new AWSServiceException("S3", "copyFile", "Unexpected error during copy", e);
        }
    }

    /**
     * Gets metadata for a file in S3.
     *
     * @param key The S3 key of the file
     * @return A map of metadata attributes
     * @throws AWSServiceException if getting the metadata fails
     */
    public Map<String, String> getFileMetadata(String key) {
        try {
            HeadObjectRequest request = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            HeadObjectResponse response = s3Client.headObject(request);
            log.info("Successfully retrieved metadata for S3 file: {}", key);

            return response.metadata();
        } catch (NoSuchKeyException e) {
            log.error("File not found in S3 when getting metadata: {}", key);
            throw new AWSServiceException("S3", "getFileMetadata", "File not found: " + key, e);
        } catch (S3Exception e) {
            log.error("S3 error getting file metadata: {}", e.getMessage());
            throw new AWSServiceException("S3", "getFileMetadata", e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error getting S3 file metadata: {}", e.getMessage());
            throw new AWSServiceException("S3", "getFileMetadata", "Unexpected error getting metadata", e);
        }
    }

    /**
     * Generates a unique key for a file in S3.
     *
     * @param originalFilename The original filename
     * @param folder The folder to store the file in
     * @return A unique S3 key
     */
    private String generateKey(String originalFilename, String folder) {
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String key = folder;
        if (!key.endsWith("/")) {
            key += "/";
        }

        return key + UUID.randomUUID() + extension;
    }

    /**
     * Builds a PutObjectRequest for S3.
     *
     * @param key The S3 key
     * @param contentType The content type
     * @return A configured PutObjectRequest
     */
    private PutObjectRequest buildPutObjectRequest(String key, String contentType) {
        return PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(contentType)
                .build();
    }
}
