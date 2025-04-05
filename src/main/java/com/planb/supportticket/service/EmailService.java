package com.planb.supportticket.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.Body;
import software.amazon.awssdk.services.ses.model.Content;
import software.amazon.awssdk.services.ses.model.Destination;
import software.amazon.awssdk.services.ses.model.Message;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final SesClient sesClient;
    
    @Value("${aws.ses.from-email}")
    private String fromEmail;
    
    /**
     * Sends an email using AWS SES
     */
    public void sendEmail(String to, String subject, String htmlBody) {
        sendEmail(List.of(to), null, null, subject, htmlBody);
    }
    
    /**
     * Sends an email with CC and BCC recipients
     */
    public void sendEmail(List<String> toAddresses, List<String> ccAddresses, 
                          List<String> bccAddresses, String subject, String htmlBody) {
        try {
            Destination destination = Destination.builder()
                    .toAddresses(toAddresses)
                    .build();
            
            if (ccAddresses != null && !ccAddresses.isEmpty()) {
                destination = destination.toBuilder()
                        .ccAddresses(ccAddresses)
                        .build();
            }
            
            if (bccAddresses != null && !bccAddresses.isEmpty()) {
                destination = destination.toBuilder()
                        .bccAddresses(bccAddresses)
                        .build();
            }
            
            Content subjectContent = Content.builder()
                    .data(subject)
                    .build();
            
            Content htmlContent = Content.builder()
                    .data(htmlBody)
                    .build();
            
            Body body = Body.builder()
                    .html(htmlContent)
                    .build();
            
            Message message = Message.builder()
                    .subject(subjectContent)
                    .body(body)
                    .build();
            
            SendEmailRequest emailRequest = SendEmailRequest.builder()
                    .destination(destination)
                    .message(message)
                    .source(fromEmail)
                    .build();
            
            sesClient.sendEmail(emailRequest);
            log.info("Email sent successfully to {}", toAddresses);
        } catch (Exception e) {
            log.error("Failed to send email", e);
            throw new RuntimeException("Failed to send email", e);
        }
    }
}
