package com.planb.supportticket.entity.enums;

/**
 * Enum representing user roles in the system.
 * These roles determine access permissions throughout the application.
 */
public enum UserRole {
    /**
     * Regular user/client role. Can access Dashboard, Multi AI Tool, 
     * Ticket Management, and Connect to Expert.
     */
    USER,
    
    /**
     * Expert role. Can access Dashboard, Pick New Tickets, and My Open Tickets.
     */
    EXPERT,
    
    /**
     * Administrator role. Has full access to all system features.
     */
    ADMIN,
    
    /**
     * Support role. Has access to support-specific features.
     */
    SUPPORT
}
