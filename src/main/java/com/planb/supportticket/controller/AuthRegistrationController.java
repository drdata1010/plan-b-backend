package com.planb.supportticket.controller;

import com.planb.supportticket.dto.ExpertRegistrationDTO;
import com.planb.supportticket.dto.RegistrationResponse;
import com.planb.supportticket.dto.UserRegistrationDTO;
import com.planb.supportticket.entity.Expert;
import com.planb.supportticket.entity.UserProfile;
import com.planb.supportticket.entity.enums.UserRole;
import com.planb.supportticket.service.EmailService;
import com.planb.supportticket.service.ExpertService;
import com.planb.supportticket.service.TwilioSMSService;
import com.planb.supportticket.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashSet;
import java.util.UUID;

/**
 * Controller for user and expert registration.
 */
@RestController
@RequestMapping("/api/register")
@RequiredArgsConstructor
@Slf4j
public class AuthRegistrationController {

    private final UserService userService;
    private final ExpertService expertService;
    private final EmailService emailService;
    private final TwilioSMSService twilioSMSService;

    @Value("${twilio.enabled:false}")
    private boolean twilioEnabled;

    /**
     * Registers a new user (individual or client).
     *
     * @param registrationDTO the user registration data
     * @return the registration response
     */
    @PostMapping("/user")
    public ResponseEntity<RegistrationResponse> registerUser(@Valid @RequestBody UserRegistrationDTO registrationDTO) {
        log.info("Registering new user with email: {}", registrationDTO.getEmail());

        // Validate passwords match
        if (!registrationDTO.getPassword().equals(registrationDTO.getConfirmPassword())) {
            return ResponseEntity.badRequest().body(
                    RegistrationResponse.builder()
                            .success(false)
                            .message("Passwords do not match")
                            .build()
            );
        }

        // Validate terms acceptance
        if (!registrationDTO.isAcceptTerms()) {
            return ResponseEntity.badRequest().body(
                    RegistrationResponse.builder()
                            .success(false)
                            .message("You must accept the terms and conditions")
                            .build()
            );
        }

        // Validate company name for Client type
        if ("Client".equals(registrationDTO.getCustomerType()) &&
            (registrationDTO.getCompanyName() == null || registrationDTO.getCompanyName().trim().isEmpty())) {
            return ResponseEntity.badRequest().body(
                    RegistrationResponse.builder()
                            .success(false)
                            .message("Company name is required for Client registration")
                            .build()
            );
        }

        try {
            // Create user profile
            UserProfile userProfile = createUserProfileFromDTO(registrationDTO);

            // Send welcome email
            sendWelcomeEmail(userProfile);

            // Send SMS confirmation if enabled
            if (twilioEnabled) {
                sendSMSConfirmation(userProfile);
            }

            // Create response
            RegistrationResponse response = RegistrationResponse.builder()
                    .success(true)
                    .message("Registration successful")
                    .userId(userProfile.getId())
                    .email(userProfile.getEmail())
                    .customerType(userProfile.getCustomerType())
                    .build();

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Error during user registration", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    RegistrationResponse.builder()
                            .success(false)
                            .message("Registration failed: " + e.getMessage())
                            .build()
            );
        }
    }

    /**
     * Registers a new expert.
     *
     * @param registrationDTO the expert registration data
     * @return the registration response
     */
    @PostMapping("/expert")
    public ResponseEntity<RegistrationResponse> registerExpert(@Valid @RequestBody ExpertRegistrationDTO registrationDTO) {
        log.info("Registering new expert with email: {}", registrationDTO.getEmail());

        // Validate passwords match
        if (!registrationDTO.getPassword().equals(registrationDTO.getConfirmPassword())) {
            return ResponseEntity.badRequest().body(
                    RegistrationResponse.builder()
                            .success(false)
                            .message("Passwords do not match")
                            .build()
            );
        }

        // Validate terms acceptance
        if (!registrationDTO.isAcceptTerms()) {
            return ResponseEntity.badRequest().body(
                    RegistrationResponse.builder()
                            .success(false)
                            .message("You must accept the terms and conditions")
                            .build()
            );
        }

        try {
            // Create user profile for the expert
            UserProfile userProfile = createUserProfileFromExpertDTO(registrationDTO);

            // Add expert role
            userService.addRoleToUser(userProfile.getId(), UserRole.EXPERT);

            // Create expert profile
            Expert expert = createExpertProfileFromDTO(registrationDTO, userProfile.getId());

            // Send welcome email
            sendExpertWelcomeEmail(userProfile);

            // Send SMS confirmation if enabled
            if (twilioEnabled) {
                sendExpertSMSConfirmation(userProfile);
            }

            // Create response
            RegistrationResponse response = RegistrationResponse.builder()
                    .success(true)
                    .message("Expert registration successful")
                    .userId(userProfile.getId())
                    .expertId(expert.getId())
                    .email(userProfile.getEmail())
                    .customerType("Expert")
                    .build();

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Error during expert registration", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    RegistrationResponse.builder()
                            .success(false)
                            .message("Registration failed: " + e.getMessage())
                            .build()
            );
        }
    }

    /**
     * Creates a UserProfile entity from UserRegistrationDTO.
     *
     * @param dto the user registration DTO
     * @return the created user profile
     */
    private UserProfile createUserProfileFromDTO(UserRegistrationDTO dto) {
        // In a real implementation, you would create a Firebase user first
        // and then use the Firebase UID to create the user profile
        String mockFirebaseUid = "mock-" + UUID.randomUUID().toString();

        UserProfile userProfile = new UserProfile();
        userProfile.setFirebaseUid(mockFirebaseUid);
        userProfile.setEmail(dto.getEmail());
        userProfile.setFirstName(dto.getFirstName());
        userProfile.setLastName(dto.getLastName());
        userProfile.setDisplayName(dto.getFirstName() + " " + dto.getLastName());
        userProfile.setMobileNumber(dto.getMobileNumber());
        userProfile.setCountry(dto.getCountry());
        userProfile.setCustomerType(dto.getCustomerType());
        userProfile.setCompanyName(dto.getCompanyName());
        userProfile.setPreferredTechnologies(dto.getPreferredTechnologies() != null ?
                dto.getPreferredTechnologies() : new HashSet<>());
        userProfile.setPreferredModules(dto.getPreferredModules() != null ?
                dto.getPreferredModules() : new HashSet<>());
        userProfile.setOtherPreferences(dto.getOtherPreferences());
        userProfile.setEmailVerified(false);
        userProfile.setAccountDisabled(false);

        // Add appropriate role based on customer type
        if ("Client".equals(dto.getCustomerType())) {
            userProfile.addRole(UserRole.USER); // Using USER role for clients too
        } else {
            userProfile.addRole(UserRole.USER);
        }

        // Save the user profile
        return userService.createUserProfile(
                userProfile.getFirebaseUid(),
                userProfile.getEmail(),
                userProfile.getDisplayName()
        );
    }

    /**
     * Creates a UserProfile entity from ExpertRegistrationDTO.
     *
     * @param dto the expert registration DTO
     * @return the created user profile
     */
    private UserProfile createUserProfileFromExpertDTO(ExpertRegistrationDTO dto) {
        // In a real implementation, you would create a Firebase user first
        // and then use the Firebase UID to create the user profile
        String mockFirebaseUid = "mock-" + UUID.randomUUID().toString();

        UserProfile userProfile = new UserProfile();
        userProfile.setFirebaseUid(mockFirebaseUid);
        userProfile.setEmail(dto.getEmail());
        userProfile.setFirstName(dto.getFirstName());
        userProfile.setLastName(dto.getLastName());
        userProfile.setDisplayName(dto.getFirstName() + " " + dto.getLastName());
        userProfile.setMobileNumber(dto.getMobileNumber());
        userProfile.setCountry(dto.getCountry());
        userProfile.setCustomerType("Expert");
        userProfile.setEmailVerified(false);
        userProfile.setAccountDisabled(false);

        // Save the user profile
        return userService.createUserProfile(
                userProfile.getFirebaseUid(),
                userProfile.getEmail(),
                userProfile.getDisplayName()
        );
    }

    /**
     * Creates an Expert entity from ExpertRegistrationDTO.
     *
     * @param dto the expert registration DTO
     * @param userId the user ID
     * @return the created expert profile
     */
    private Expert createExpertProfileFromDTO(ExpertRegistrationDTO dto, UUID userId) {
        // Create expert DTO
        com.planb.supportticket.dto.ExpertDTO expertDTO = new com.planb.supportticket.dto.ExpertDTO();
        expertDTO.setYearsOfExperience(dto.getYearsOfExperience());
        expertDTO.setJobRole(dto.getJobRole());
        expertDTO.setResponseTime(dto.getResponseTime());
        expertDTO.setLinkedinProfile(dto.getLinkedinProfile());
        expertDTO.setTechnologies(dto.getTechnologies());
        expertDTO.setModules(dto.getModules());
        expertDTO.setOtherExpertise(dto.getOtherExpertise());
        expertDTO.setAvailable(true);

        // Create expert profile
        return expertService.createExpertProfile(expertDTO, userId);
    }

    /**
     * Sends a welcome email to a new user.
     *
     * @param userProfile the user profile
     */
    private void sendWelcomeEmail(UserProfile userProfile) {
        String subject = "Welcome to PlanBnext!";
        String body = String.format(
                "Dear %s,\n\n" +
                "Welcome to PlanBnext! Your account has been created successfully.\n\n" +
                "Your Customer ID is: %s\n\n" +
                "Thank you for joining us!\n\n" +
                "Best regards,\n" +
                "The PlanBnext Team",
                userProfile.getDisplayName(),
                userProfile.getId()
        );

        emailService.sendEmail(userProfile.getEmail(), subject, body);
    }

    /**
     * Sends a welcome email to a new expert.
     *
     * @param userProfile the user profile
     */
    private void sendExpertWelcomeEmail(UserProfile userProfile) {
        String subject = "Welcome to PlanBnext as an Expert!";
        String body = String.format(
                "Dear %s,\n\n" +
                "Welcome to PlanBnext! Your expert account has been created successfully.\n\n" +
                "Your Expert ID is: %s\n\n" +
                "Thank you for joining our expert community!\n\n" +
                "Best regards,\n" +
                "The PlanBnext Team",
                userProfile.getDisplayName(),
                userProfile.getId()
        );

        emailService.sendEmail(userProfile.getEmail(), subject, body);
    }

    /**
     * Sends an SMS confirmation to a new user.
     *
     * @param userProfile the user profile
     */
    private void sendSMSConfirmation(UserProfile userProfile) {
        String message = String.format(
                "Welcome to PlanBnext! Your account has been created successfully. Your Customer ID is: %s",
                userProfile.getId()
        );

        twilioSMSService.sendSMS(userProfile.getMobileNumber(), message);
    }

    /**
     * Sends an SMS confirmation to a new expert.
     *
     * @param userProfile the user profile
     */
    private void sendExpertSMSConfirmation(UserProfile userProfile) {
        String message = String.format(
                "Welcome to PlanBnext as an Expert! Your account has been created successfully. Your Expert ID is: %s",
                userProfile.getId()
        );

        twilioSMSService.sendSMS(userProfile.getMobileNumber(), message);
    }
}
