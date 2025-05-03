package com.planb.supportticket.exception;

/**
 * Exception thrown when a GCP service operation fails.
 */
public class GCPServiceException extends RuntimeException {

    private final String service;
    private final String operation;

    /**
     * Creates a new GCP service exception.
     *
     * @param service The GCP service that failed (e.g., "GCS", "Secret Manager")
     * @param operation The operation that failed (e.g., "uploadFile", "getSecret")
     * @param message The error message
     */
    public GCPServiceException(String service, String operation, String message) {
        super(String.format("GCP %s service error during %s operation: %s", service, operation, message));
        this.service = service;
        this.operation = operation;
    }

    /**
     * Creates a new GCP service exception with a cause.
     *
     * @param service The GCP service that failed (e.g., "GCS", "Secret Manager")
     * @param operation The operation that failed (e.g., "uploadFile", "getSecret")
     * @param message The error message
     * @param cause The cause of the exception
     */
    public GCPServiceException(String service, String operation, String message, Throwable cause) {
        super(String.format("GCP %s service error during %s operation: %s", service, operation, message), cause);
        this.service = service;
        this.operation = operation;
    }

    /**
     * Gets the GCP service that failed.
     *
     * @return The GCP service name
     */
    public String getService() {
        return service;
    }

    /**
     * Gets the operation that failed.
     *
     * @return The operation name
     */
    public String getOperation() {
        return operation;
    }
}
