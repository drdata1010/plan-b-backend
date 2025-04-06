package com.planb.supportticket.service;

/**
 * Service interface for AWS SES operations.
 * Handles email sending via Amazon Simple Email Service.
 */
public interface SESService {

    /**
     * Sends an email.
     *
     * @param to the recipient email address
     * @param subject the email subject
     * @param body the email body
     */
    void sendEmail(String to, String subject, String body);
    
    /**
     * Sends an email with HTML content.
     *
     * @param to the recipient email address
     * @param subject the email subject
     * @param htmlBody the HTML email body
     */
    void sendHtmlEmail(String to, String subject, String htmlBody);
    
    /**
     * Sends an email with attachment.
     *
     * @param to the recipient email address
     * @param subject the email subject
     * @param body the email body
     * @param attachmentPath the path to the attachment
     * @param attachmentName the name of the attachment
     */
    void sendEmailWithAttachment(String to, String subject, String body, String attachmentPath, String attachmentName);
}
