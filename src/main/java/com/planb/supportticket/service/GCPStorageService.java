package com.planb.supportticket.service;

import com.planb.supportticket.config.gcp.GCSService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Service for handling file storage operations using Google Cloud Storage.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GCPStorageService {

    private final GCSService gcsService;
    
    @Value("${gcp.storage.bucket-name}")
    private String bucketName;
    
    /**
     * Uploads a file to Google Cloud Storage and returns the object name
     */
    public String uploadFile(MultipartFile file, String folder) {
        try {
            return gcsService.uploadFile(file, folder);
        } catch (Exception e) {
            log.error("Error uploading file to GCS", e);
            throw new RuntimeException("Failed to upload file to Google Cloud Storage", e);
        }
    }
    
    /**
     * Generates a signed URL for downloading a file
     */
    public String generateSignedUrl(String objectName, Duration expiration) {
        return gcsService.generateSignedUrl(objectName, expiration.getSeconds(), TimeUnit.SECONDS);
    }
    
    /**
     * Deletes a file from Google Cloud Storage
     */
    public void deleteFile(String objectName) {
        gcsService.deleteFile(objectName);
        log.info("File deleted successfully from GCS: {}", objectName);
    }
    
    /**
     * Extracts file extension from filename
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf(".") == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }
}
