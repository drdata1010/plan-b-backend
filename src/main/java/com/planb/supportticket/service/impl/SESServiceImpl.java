package com.planb.supportticket.service.impl;

import com.planb.supportticket.service.SESService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.*;

/**
 * Implementation of the SESService interface.
 * Handles email sending via Amazon Simple Email Service.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SESServiceImpl implements SESService {

    private final SesClient sesClient;
    
    @Value("${aws.ses.from-email}")
    private String fromEmail;

    @Override
    public void sendEmail(String to, String subject, String body) {
        try {
            Destination destination = Destination.builder()
                    .toAddresses(to)
                    .build();
            
            Content subjectContent = Content.builder()
                    .data(subject)
                    .build();
            
            Content bodyContent = Content.builder()
                    .data(body)
                    .build();
            
            Body messageBody = Body.builder()
                    .text(bodyContent)
                    .build();
            
            Message message = Message.builder()
                    .subject(subjectContent)
                    .body(messageBody)
                    .build();
            
            SendEmailRequest emailRequest = SendEmailRequest.builder()
                    .destination(destination)
                    .message(message)
                    .source(fromEmail)
                    .build();
            
            sesClient.sendEmail(emailRequest);
            log.info("Email sent to {}: {}", to, subject);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
            throw new RuntimeException("Failed to send email", e);
        }
    }

    @Override
    public void sendHtmlEmail(String to, String subject, String htmlBody) {
        try {
            Destination destination = Destination.builder()
                    .toAddresses(to)
                    .build();
            
            Content subjectContent = Content.builder()
                    .data(subject)
                    .build();
            
            Content htmlContent = Content.builder()
                    .data(htmlBody)
                    .build();
            
            Body messageBody = Body.builder()
                    .html(htmlContent)
                    .build();
            
            Message message = Message.builder()
                    .subject(subjectContent)
                    .body(messageBody)
                    .build();
            
            SendEmailRequest emailRequest = SendEmailRequest.builder()
                    .destination(destination)
                    .message(message)
                    .source(fromEmail)
                    .build();
            
            sesClient.sendEmail(emailRequest);
            log.info("HTML email sent to {}: {}", to, subject);
        } catch (Exception e) {
            log.error("Failed to send HTML email to {}: {}", to, e.getMessage());
            throw new RuntimeException("Failed to send HTML email", e);
        }
    }

    @Override
    public void sendEmailWithAttachment(String to, String subject, String body, String attachmentPath, String attachmentName) {
        // For simplicity, we're not implementing attachment handling in this version
        // In a real implementation, you would use the AWS SDK to send raw emails with attachments
        log.warn("Sending email with attachment is not implemented yet");
        sendEmail(to, subject, body + "\n\nNote: Attachment functionality is not implemented yet.");
    }
}
