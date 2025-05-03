package com.planb.supportticket.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.List;

/**
 * Service for sending SMS messages using Twilio.
 */
@Service
@Slf4j
public class TwilioSMSService {

    @Value("${twilio.account-sid}")
    private String accountSid;
    
    @Value("${twilio.auth-token}")
    private String authToken;
    
    @Value("${twilio.phone-number}")
    private String twilioPhoneNumber;
    
    @Value("${twilio.enabled:false}")
    private boolean enabled;
    
    @PostConstruct
    public void init() {
        if (enabled) {
            try {
                Twilio.init(accountSid, authToken);
                log.info("Twilio SMS service initialized successfully");
            } catch (Exception e) {
                log.error("Failed to initialize Twilio SMS service", e);
                // Don't throw exception here to allow application to start even if Twilio is not available
            }
        } else {
            log.info("Twilio SMS service is disabled");
        }
    }
    
    /**
     * Sends an SMS message.
     *
     * @param to The recipient phone number (with country code, e.g., +1234567890)
     * @param body The message body
     * @return The message SID if successful, null otherwise
     */
    public String sendSMS(String to, String body) {
        if (!enabled) {
            log.warn("Twilio SMS service is disabled. Message not sent to: {}", to);
            return null;
        }
        
        try {
            Message message = Message.creator(
                    new PhoneNumber(to),
                    new PhoneNumber(twilioPhoneNumber),
                    body
            ).create();
            
            log.info("SMS sent successfully to {}, SID: {}", to, message.getSid());
            return message.getSid();
        } catch (Exception e) {
            log.error("Failed to send SMS to {}: {}", to, e.getMessage());
            return null;
        }
    }
    
    /**
     * Sends an SMS message to multiple recipients.
     *
     * @param toNumbers The list of recipient phone numbers (with country code)
     * @param body The message body
     * @return The number of successfully sent messages
     */
    public int sendBulkSMS(List<String> toNumbers, String body) {
        if (!enabled) {
            log.warn("Twilio SMS service is disabled. Bulk message not sent to {} recipients", toNumbers.size());
            return 0;
        }
        
        int successCount = 0;
        
        for (String to : toNumbers) {
            try {
                Message message = Message.creator(
                        new PhoneNumber(to),
                        new PhoneNumber(twilioPhoneNumber),
                        body
                ).create();
                
                log.info("SMS sent successfully to {}, SID: {}", to, message.getSid());
                successCount++;
            } catch (Exception e) {
                log.error("Failed to send SMS to {}: {}", to, e.getMessage());
            }
        }
        
        log.info("Bulk SMS: sent {} out of {} messages successfully", successCount, toNumbers.size());
        return successCount;
    }
}
