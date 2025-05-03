package com.planb.supportticket.service;

import java.util.List;

/**
 * Service interface for email operations.
 */
public interface EmailService {

    /**
     * Sends an email.
     *
     * @param to the recipient email address
     * @param subject the email subject
     * @param body the email body
     */
    void sendEmail(String to, String subject, String body);

    /**
     * Sends an HTML email.
     *
     * @param to the recipient email address
     * @param subject the email subject
     * @param htmlBody the HTML email body
     */
    void sendHtmlEmail(String to, String subject, String htmlBody);

    /**
     * Sends an email with CC and BCC recipients.
     *
     * @param toAddresses the list of recipient email addresses
     * @param ccAddresses the list of CC email addresses
     * @param bccAddresses the list of BCC email addresses
     * @param subject the email subject
     * @param htmlBody the HTML email body
     */
    void sendEmail(List<String> toAddresses, List<String> ccAddresses,
                   List<String> bccAddresses, String subject, String htmlBody);

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

    /**
     * Sends an email to multiple recipients.
     *
     * @param to the list of recipient email addresses
     * @param subject the email subject
     * @param body the email body
     */
    void sendEmailToMultipleRecipients(List<String> to, String subject, String body);
}
