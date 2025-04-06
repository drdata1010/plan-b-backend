package com.planb.supportticket.config.aws;

import com.planb.supportticket.exception.AWSServiceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.ses.SesAsyncClient;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Service for sending emails using AWS SES.
 * Provides methods for email operations with both synchronous and asynchronous support.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SESService {

    private final SesClient sesClient;
    private final SesAsyncClient sesAsyncClient;
    
    @Value("${aws.ses.from-email}")
    private String fromEmail;
    
    @Value("${aws.ses.reply-to-email:}")
    private String replyToEmail;
    
    /**
     * Sends an email synchronously.
     *
     * @param to The recipient email address
     * @param subject The email subject
     * @param htmlBody The HTML body of the email
     * @return The message ID of the sent email
     * @throws AWSServiceException if sending the email fails
     */
    public String sendEmail(String to, String subject, String htmlBody) {
        return sendEmail(List.of(to), null, null, subject, htmlBody, null);
    }
    
    /**
     * Sends an email synchronously with CC and BCC recipients.
     *
     * @param toAddresses The recipient email addresses
     * @param ccAddresses The CC recipient email addresses (can be null)
     * @param bccAddresses The BCC recipient email addresses (can be null)
     * @param subject The email subject
     * @param htmlBody The HTML body of the email
     * @param textBody The plain text body of the email (can be null)
     * @return The message ID of the sent email
     * @throws AWSServiceException if sending the email fails
     */
    public String sendEmail(List<String> toAddresses, List<String> ccAddresses, List<String> bccAddresses,
                           String subject, String htmlBody, String textBody) {
        try {
            SendEmailRequest.Builder requestBuilder = SendEmailRequest.builder()
                    .destination(buildDestination(toAddresses, ccAddresses, bccAddresses))
                    .message(buildMessage(subject, htmlBody, textBody))
                    .source(fromEmail);
            
            // Add reply-to if configured
            if (replyToEmail != null && !replyToEmail.isEmpty()) {
                requestBuilder.replyToAddresses(replyToEmail);
            }
            
            SendEmailResponse response = sesClient.sendEmail(requestBuilder.build());
            String messageId = response.messageId();
            log.info("Successfully sent email with message ID: {}", messageId);
            return messageId;
        } catch (SesException e) {
            log.error("SES error sending email: {}", e.getMessage());
            throw new AWSServiceException("SES", "sendEmail", e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error sending email: {}", e.getMessage());
            throw new AWSServiceException("SES", "sendEmail", "Unexpected error sending email", e);
        }
    }
    
    /**
     * Sends an email asynchronously.
     *
     * @param to The recipient email address
     * @param subject The email subject
     * @param htmlBody The HTML body of the email
     * @return A CompletableFuture that will complete with the message ID of the sent email
     */
    @Async
    public CompletableFuture<String> sendEmailAsync(String to, String subject, String htmlBody) {
        return sendEmailAsync(List.of(to), null, null, subject, htmlBody, null);
    }
    
    /**
     * Sends an email asynchronously with CC and BCC recipients.
     *
     * @param toAddresses The recipient email addresses
     * @param ccAddresses The CC recipient email addresses (can be null)
     * @param bccAddresses The BCC recipient email addresses (can be null)
     * @param subject The email subject
     * @param htmlBody The HTML body of the email
     * @param textBody The plain text body of the email (can be null)
     * @return A CompletableFuture that will complete with the message ID of the sent email
     */
    @Async
    public CompletableFuture<String> sendEmailAsync(List<String> toAddresses, List<String> ccAddresses, 
                                                  List<String> bccAddresses, String subject, 
                                                  String htmlBody, String textBody) {
        try {
            SendEmailRequest.Builder requestBuilder = SendEmailRequest.builder()
                    .destination(buildDestination(toAddresses, ccAddresses, bccAddresses))
                    .message(buildMessage(subject, htmlBody, textBody))
                    .source(fromEmail);
            
            // Add reply-to if configured
            if (replyToEmail != null && !replyToEmail.isEmpty()) {
                requestBuilder.replyToAddresses(replyToEmail);
            }
            
            return sesAsyncClient.sendEmail(requestBuilder.build())
                    .thenApply(response -> {
                        String messageId = response.messageId();
                        log.info("Successfully sent email asynchronously with message ID: {}", messageId);
                        return messageId;
                    })
                    .exceptionally(e -> {
                        log.error("Async SES email sending failed: {}", e.getMessage());
                        throw new AWSServiceException("SES", "sendEmailAsync", "Async email sending failed", e);
                    });
        } catch (Exception e) {
            log.error("Error initiating async SES email send: {}", e.getMessage());
            CompletableFuture<String> future = new CompletableFuture<>();
            future.completeExceptionally(
                    new AWSServiceException("SES", "sendEmailAsync", "Failed to initiate email sending", e));
            return future;
        }
    }
    
    /**
     * Sends a templated email synchronously.
     *
     * @param to The recipient email address
     * @param templateName The name of the SES template to use
     * @param templateData The template data as a JSON string
     * @return The message ID of the sent email
     * @throws AWSServiceException if sending the email fails
     */
    public String sendTemplatedEmail(String to, String templateName, String templateData) {
        return sendTemplatedEmail(List.of(to), null, null, templateName, templateData);
    }
    
    /**
     * Sends a templated email synchronously with CC and BCC recipients.
     *
     * @param toAddresses The recipient email addresses
     * @param ccAddresses The CC recipient email addresses (can be null)
     * @param bccAddresses The BCC recipient email addresses (can be null)
     * @param templateName The name of the SES template to use
     * @param templateData The template data as a JSON string
     * @return The message ID of the sent email
     * @throws AWSServiceException if sending the email fails
     */
    public String sendTemplatedEmail(List<String> toAddresses, List<String> ccAddresses, 
                                    List<String> bccAddresses, String templateName, String templateData) {
        try {
            SendTemplatedEmailRequest.Builder requestBuilder = SendTemplatedEmailRequest.builder()
                    .destination(buildDestination(toAddresses, ccAddresses, bccAddresses))
                    .template(templateName)
                    .templateData(templateData)
                    .source(fromEmail);
            
            // Add reply-to if configured
            if (replyToEmail != null && !replyToEmail.isEmpty()) {
                requestBuilder.replyToAddresses(replyToEmail);
            }
            
            SendTemplatedEmailResponse response = sesClient.sendTemplatedEmail(requestBuilder.build());
            String messageId = response.messageId();
            log.info("Successfully sent templated email with message ID: {}", messageId);
            return messageId;
        } catch (SesException e) {
            log.error("SES error sending templated email: {}", e.getMessage());
            throw new AWSServiceException("SES", "sendTemplatedEmail", e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error sending templated email: {}", e.getMessage());
            throw new AWSServiceException("SES", "sendTemplatedEmail", "Unexpected error sending templated email", e);
        }
    }
    
    /**
     * Sends a templated email asynchronously.
     *
     * @param to The recipient email address
     * @param templateName The name of the SES template to use
     * @param templateData The template data as a JSON string
     * @return A CompletableFuture that will complete with the message ID of the sent email
     */
    @Async
    public CompletableFuture<String> sendTemplatedEmailAsync(String to, String templateName, String templateData) {
        return sendTemplatedEmailAsync(List.of(to), null, null, templateName, templateData);
    }
    
    /**
     * Sends a templated email asynchronously with CC and BCC recipients.
     *
     * @param toAddresses The recipient email addresses
     * @param ccAddresses The CC recipient email addresses (can be null)
     * @param bccAddresses The BCC recipient email addresses (can be null)
     * @param templateName The name of the SES template to use
     * @param templateData The template data as a JSON string
     * @return A CompletableFuture that will complete with the message ID of the sent email
     */
    @Async
    public CompletableFuture<String> sendTemplatedEmailAsync(List<String> toAddresses, List<String> ccAddresses, 
                                                           List<String> bccAddresses, String templateName, 
                                                           String templateData) {
        try {
            SendTemplatedEmailRequest.Builder requestBuilder = SendTemplatedEmailRequest.builder()
                    .destination(buildDestination(toAddresses, ccAddresses, bccAddresses))
                    .template(templateName)
                    .templateData(templateData)
                    .source(fromEmail);
            
            // Add reply-to if configured
            if (replyToEmail != null && !replyToEmail.isEmpty()) {
                requestBuilder.replyToAddresses(replyToEmail);
            }
            
            return sesAsyncClient.sendTemplatedEmail(requestBuilder.build())
                    .thenApply(response -> {
                        String messageId = response.messageId();
                        log.info("Successfully sent templated email asynchronously with message ID: {}", messageId);
                        return messageId;
                    })
                    .exceptionally(e -> {
                        log.error("Async SES templated email sending failed: {}", e.getMessage());
                        throw new AWSServiceException("SES", "sendTemplatedEmailAsync", 
                                "Async templated email sending failed", e);
                    });
        } catch (Exception e) {
            log.error("Error initiating async SES templated email send: {}", e.getMessage());
            CompletableFuture<String> future = new CompletableFuture<>();
            future.completeExceptionally(
                    new AWSServiceException("SES", "sendTemplatedEmailAsync", 
                            "Failed to initiate templated email sending", e));
            return future;
        }
    }
    
    /**
     * Verifies an email address with SES.
     * SES requires email verification before sending emails from or to an address in sandbox mode.
     *
     * @param emailAddress The email address to verify
     * @throws AWSServiceException if the verification request fails
     */
    public void verifyEmailAddress(String emailAddress) {
        try {
            VerifyEmailAddressRequest request = VerifyEmailAddressRequest.builder()
                    .emailAddress(emailAddress)
                    .build();
            
            sesClient.verifyEmailAddress(request);
            log.info("Verification email sent to: {}", emailAddress);
        } catch (SesException e) {
            log.error("SES error verifying email address: {}", e.getMessage());
            throw new AWSServiceException("SES", "verifyEmailAddress", e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error verifying email address: {}", e.getMessage());
            throw new AWSServiceException("SES", "verifyEmailAddress", "Unexpected error verifying email", e);
        }
    }
    
    /**
     * Lists all verified email addresses.
     *
     * @return A list of verified email addresses
     * @throws AWSServiceException if listing the verified emails fails
     */
    public List<String> listVerifiedEmailAddresses() {
        try {
            ListVerifiedEmailAddressesResponse response = sesClient.listVerifiedEmailAddresses();
            log.info("Retrieved {} verified email addresses", response.verifiedEmailAddresses().size());
            return response.verifiedEmailAddresses();
        } catch (SesException e) {
            log.error("SES error listing verified email addresses: {}", e.getMessage());
            throw new AWSServiceException("SES", "listVerifiedEmailAddresses", e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error listing verified email addresses: {}", e.getMessage());
            throw new AWSServiceException("SES", "listVerifiedEmailAddresses", 
                    "Unexpected error listing verified emails", e);
        }
    }
    
    /**
     * Builds a Destination object for SES.
     *
     * @param toAddresses The recipient email addresses
     * @param ccAddresses The CC recipient email addresses (can be null)
     * @param bccAddresses The BCC recipient email addresses (can be null)
     * @return A configured Destination object
     */
    private Destination buildDestination(List<String> toAddresses, List<String> ccAddresses, List<String> bccAddresses) {
        Destination.Builder destinationBuilder = Destination.builder()
                .toAddresses(toAddresses);
        
        if (ccAddresses != null && !ccAddresses.isEmpty()) {
            destinationBuilder.ccAddresses(ccAddresses);
        }
        
        if (bccAddresses != null && !bccAddresses.isEmpty()) {
            destinationBuilder.bccAddresses(bccAddresses);
        }
        
        return destinationBuilder.build();
    }
    
    /**
     * Builds a Message object for SES.
     *
     * @param subject The email subject
     * @param htmlBody The HTML body of the email
     * @param textBody The plain text body of the email (can be null)
     * @return A configured Message object
     */
    private Message buildMessage(String subject, String htmlBody, String textBody) {
        Content subjectContent = Content.builder()
                .data(subject)
                .build();
        
        Body.Builder bodyBuilder = Body.builder();
        
        // Always add HTML content
        Content htmlContent = Content.builder()
                .data(htmlBody)
                .charset("UTF-8")
                .build();
        bodyBuilder.html(htmlContent);
        
        // Add text content if provided
        if (textBody != null && !textBody.isEmpty()) {
            Content textContent = Content.builder()
                    .data(textBody)
                    .charset("UTF-8")
                    .build();
            bodyBuilder.text(textContent);
        }
        
        return Message.builder()
                .subject(subjectContent)
                .body(bodyBuilder.build())
                .build();
    }
}
