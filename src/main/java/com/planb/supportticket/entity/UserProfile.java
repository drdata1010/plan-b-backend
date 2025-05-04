package com.planb.supportticket.entity;

import com.planb.supportticket.entity.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.NaturalId;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "user_profiles",
       indexes = {
           @Index(name = "idx_user_email", columnList = "email"),
           @Index(name = "idx_user_firebase_uid", columnList = "firebase_uid")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfile extends BaseEntity {

    @NaturalId
    @Column(name = "firebase_uid", nullable = false, unique = true)
    private String firebaseUid;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "display_name")
    private String displayName;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "mobile_number")
    private String mobileNumber;

    @Column(name = "country")
    private String country;

    @Column(name = "company_name")
    private String companyName;

    @Column(name = "customer_type")
    private String customerType; // User or Client

    @Column(name = "profile_picture_url")
    private String profilePictureUrl;

    @Column(name = "bio", length = 1000)
    private String bio;

    @ElementCollection
    @CollectionTable(
        name = "user_preferred_technologies",
        joinColumns = @JoinColumn(name = "user_id")
    )
    @Column(name = "technology")
    private Set<String> preferredTechnologies = new HashSet<>();

    @ElementCollection
    @CollectionTable(
        name = "user_preferred_modules",
        joinColumns = @JoinColumn(name = "user_id")
    )
    @Column(name = "module")
    private Set<String> preferredModules = new HashSet<>();

    @Column(name = "other_preferences", length = 500)
    private String otherPreferences;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @Column(name = "is_email_verified")
    private boolean isEmailVerified;

    // For backward compatibility
    public Boolean getEmailVerified() {
        return isEmailVerified;
    }

    public void setEmailVerified(Boolean emailVerified) {
        this.isEmailVerified = emailVerified;
    }

    public boolean isEmailVerified() {
        return isEmailVerified;
    }

    @Column(name = "is_account_disabled")
    private boolean isAccountDisabled;

    // For backward compatibility
    public Boolean getAccountDisabled() {
        return isAccountDisabled;
    }

    public void setAccountDisabled(Boolean accountDisabled) {
        this.isAccountDisabled = accountDisabled;
    }

    public boolean isAccountDisabled() {
        return isAccountDisabled;
    }

    @ElementCollection
    @CollectionTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private Set<UserRole> roles = new HashSet<>();

    @OneToOne(mappedBy = "userProfile", cascade = CascadeType.ALL, orphanRemoval = true)
    private Expert expertProfile;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Ticket> tickets = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TicketComment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ExpertSession> expertSessions = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatSession> chatSessions = new ArrayList<>();

    @OneToMany(mappedBy = "sender", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatMessage> sentMessages = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Attachment> attachments = new ArrayList<>();

    /**
     * Adds a role to the user.
     *
     * @param role the role to add
     */
    public void addRole(UserRole role) {
        roles.add(role);
    }

    /**
     * Removes a role from the user.
     *
     * @param role the role to remove
     */
    public void removeRole(UserRole role) {
        roles.remove(role);
    }

    /**
     * Checks if the user has a specific role.
     *
     * @param role the role to check
     * @return true if the user has the role, false otherwise
     */
    public boolean hasRole(UserRole role) {
        return roles.contains(role);
    }

    /**
     * Gets the Spring Security role name for a given role.
     *
     * @param role the role
     * @return the Spring Security role name
     */
    public static String getSpringSecurityRoleName(UserRole role) {
        return "ROLE_" + role.name();
    }
}
