package com.planb.supportticket.exception;

/**
 * Custom exception for AWS service errors.
 * Provides additional context about the AWS service and operation that failed.
 */
public class AWSServiceException extends RuntimeException {

    private final String serviceName;
    private final String operationName;
    
    /**
     * Creates a new AWS service exception.
     *
     * @param serviceName The name of the AWS service (e.g., "S3", "SES")
     * @param operationName The name of the operation that failed (e.g., "uploadFile", "sendEmail")
     * @param message The error message
     */
    public AWSServiceException(String serviceName, String operationName, String message) {
        super(String.format("AWS %s service error during %s operation: %s", serviceName, operationName, message));
        this.serviceName = serviceName;
        this.operationName = operationName;
    }
    
    /**
     * Creates a new AWS service exception with a cause.
     *
     * @param serviceName The name of the AWS service (e.g., "S3", "SES")
     * @param operationName The name of the operation that failed (e.g., "uploadFile", "sendEmail")
     * @param message The error message
     * @param cause The cause of the error
     */
    public AWSServiceException(String serviceName, String operationName, String message, Throwable cause) {
        super(String.format("AWS %s service error during %s operation: %s", serviceName, operationName, message), cause);
        this.serviceName = serviceName;
        this.operationName = operationName;
    }
    
    /**
     * Gets the name of the AWS service that encountered an error.
     *
     * @return The service name
     */
    public String getServiceName() {
        return serviceName;
    }
    
    /**
     * Gets the name of the operation that failed.
     *
     * @return The operation name
     */
    public String getOperationName() {
        return operationName;
    }
}
