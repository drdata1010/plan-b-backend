package com.planb.supportticket.config.gcp;

import com.google.cloud.storage.*;
import com.google.api.gax.paging.Page;
import com.planb.supportticket.exception.GCPServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Service for interacting with Google Cloud Storage.
 * Provides methods for file operations with both synchronous and asynchronous support.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GCSService {

    private final Storage storage;

    @Value("${gcp.storage.bucket-name}")
    private String bucketName;

    @Value("${gcp.storage.signed-url-expiration:3600}")
    private long signedUrlExpirationSeconds;

    /**
     * Uploads a file to GCS synchronously.
     *
     * @param file The file to upload
     * @param folder The folder to upload to (e.g., "tickets", "profiles")
     * @return The GCS object name of the uploaded file
     * @throws GCPServiceException if the upload fails
     */
    public String uploadFile(MultipartFile file, String folder) {
        String objectName = generateObjectName(file.getOriginalFilename(), folder);

        try {
            BlobId blobId = BlobId.of(bucketName, objectName);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                    .setContentType(file.getContentType())
                    .build();

            storage.create(blobInfo, file.getBytes());
            log.info("Successfully uploaded file to GCS: {}", objectName);
            return objectName;
        } catch (IOException e) {
            log.error("Failed to read file for GCS upload: {}", e.getMessage());
            throw new GCPServiceException("GCS", "uploadFile", "Failed to read file for upload", e);
        } catch (StorageException e) {
            log.error("GCS error during file upload: {}", e.getMessage());
            throw new GCPServiceException("GCS", "uploadFile", e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error during GCS file upload: {}", e.getMessage());
            throw new GCPServiceException("GCS", "uploadFile", "Unexpected error during upload", e);
        }
    }

    /**
     * Uploads a file to GCS asynchronously.
     *
     * @param file The file to upload
     * @param folder The folder to upload to (e.g., "tickets", "profiles")
     * @return A CompletableFuture that will complete with the GCS object name of the uploaded file
     */
    @Async
    public CompletableFuture<String> uploadFileAsync(MultipartFile file, String folder) {
        return CompletableFuture.supplyAsync(() -> uploadFile(file, folder));
    }

    /**
     * Downloads a file from GCS.
     *
     * @param objectName The GCS object name of the file
     * @return The file content as a byte array
     * @throws GCPServiceException if downloading the file fails
     */
    public byte[] downloadFile(String objectName) {
        try {
            Blob blob = storage.get(BlobId.of(bucketName, objectName));
            if (blob == null) {
                throw new GCPServiceException("GCS", "downloadFile", "File not found: " + objectName);
            }

            byte[] content = blob.getContent();
            log.info("Successfully downloaded file from GCS: {}", objectName);
            return content;
        } catch (StorageException e) {
            log.error("GCS error downloading file: {}", e.getMessage());
            throw new GCPServiceException("GCS", "downloadFile", e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error downloading GCS file: {}", e.getMessage());
            throw new GCPServiceException("GCS", "downloadFile", "Unexpected error downloading file", e);
        }
    }

    /**
     * Downloads a file from GCS asynchronously.
     *
     * @param objectName The GCS object name of the file
     * @return A CompletableFuture that will complete with the file content as a byte array
     */
    @Async
    public CompletableFuture<byte[]> downloadFileAsync(String objectName) {
        return CompletableFuture.supplyAsync(() -> downloadFile(objectName));
    }

    /**
     * Deletes a file from GCS.
     *
     * @param objectName The GCS object name of the file
     * @throws GCPServiceException if deleting the file fails
     */
    public void deleteFile(String objectName) {
        try {
            BlobId blobId = BlobId.of(bucketName, objectName);
            boolean deleted = storage.delete(blobId);

            if (deleted) {
                log.info("Successfully deleted file from GCS: {}", objectName);
            } else {
                log.warn("File not found or could not be deleted from GCS: {}", objectName);
            }
        } catch (StorageException e) {
            log.error("GCS error deleting file: {}", e.getMessage());
            throw new GCPServiceException("GCS", "deleteFile", e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error deleting GCS file: {}", e.getMessage());
            throw new GCPServiceException("GCS", "deleteFile", "Unexpected error deleting file", e);
        }
    }

    /**
     * Deletes a file from GCS asynchronously.
     *
     * @param objectName The GCS object name of the file
     * @return A CompletableFuture that will complete when the file is deleted
     */
    @Async
    public CompletableFuture<Void> deleteFileAsync(String objectName) {
        return CompletableFuture.runAsync(() -> deleteFile(objectName));
    }

    /**
     * Generates a signed URL for downloading a file from GCS.
     *
     * @param objectName The GCS object name of the file
     * @return The signed URL as a string
     * @throws GCPServiceException if generating the URL fails
     */
    public String generateSignedUrl(String objectName) {
        return generateSignedUrl(objectName, signedUrlExpirationSeconds, TimeUnit.SECONDS);
    }

    /**
     * Generates a signed URL for downloading a file from GCS with a custom expiration.
     *
     * @param objectName The GCS object name of the file
     * @param expiration The expiration time
     * @param timeUnit The time unit for the expiration
     * @return The signed URL as a string
     * @throws GCPServiceException if generating the URL fails
     */
    public String generateSignedUrl(String objectName, long expiration, TimeUnit timeUnit) {
        try {
            BlobInfo blobInfo = BlobInfo.newBuilder(BlobId.of(bucketName, objectName)).build();

            URL url = storage.signUrl(blobInfo, expiration, timeUnit, Storage.SignUrlOption.withV4Signature());
            log.info("Generated signed URL for GCS object: {}", objectName);
            return url.toString();
        } catch (StorageException e) {
            log.error("GCS error generating signed URL: {}", e.getMessage());
            throw new GCPServiceException("GCS", "generateSignedUrl", e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error generating signed URL: {}", e.getMessage());
            throw new GCPServiceException("GCS", "generateSignedUrl", "Unexpected error generating signed URL", e);
        }
    }

    /**
     * Checks if a file exists in GCS.
     *
     * @param objectName The GCS object name of the file
     * @return true if the file exists, false otherwise
     */
    public boolean fileExists(String objectName) {
        try {
            Blob blob = storage.get(BlobId.of(bucketName, objectName));
            return blob != null && blob.exists();
        } catch (StorageException e) {
            log.error("GCS error checking if file exists: {}", e.getMessage());
            throw new GCPServiceException("GCS", "fileExists", e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error checking if GCS file exists: {}", e.getMessage());
            throw new GCPServiceException("GCS", "fileExists", "Unexpected error checking file existence", e);
        }
    }

    /**
     * Lists all files in a folder.
     *
     * @param folder The folder to list files from
     * @return A list of blob objects
     * @throws GCPServiceException if listing the files fails
     */
    public List<Blob> listFiles(String folder) {
        try {
            String prefix = folder;
            if (!prefix.endsWith("/")) {
                prefix += "/";
            }

            Page<Blob> blobs = storage.list(
                    bucketName,
                    Storage.BlobListOption.prefix(prefix)
            );

            List<Blob> blobList = new ArrayList<>();
            for (Blob blob : blobs.iterateAll()) {
                blobList.add(blob);
            }

            log.info("Listed {} files from GCS folder: {}", blobList.size(), folder);
            return blobList;
        } catch (StorageException e) {
            log.error("GCS error listing files: {}", e.getMessage());
            throw new GCPServiceException("GCS", "listFiles", e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error listing GCS files: {}", e.getMessage());
            throw new GCPServiceException("GCS", "listFiles", "Unexpected error listing files", e);
        }
    }

    /**
     * Generates a unique object name for a file.
     *
     * @param originalFilename The original filename
     * @param folder The folder to upload to
     * @return A unique object name
     */
    private String generateObjectName(String originalFilename, String folder) {
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String objectName = folder;
        if (!objectName.endsWith("/")) {
            objectName += "/";
        }

        return objectName + UUID.randomUUID() + extension;
    }
}
