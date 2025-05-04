package com.planb.supportticket.entity.enums;

/**
 * Enum representing permissions in the system.
 * These permissions can be assigned to roles for fine-grained access control.
 */
public enum Permission {
    // Dashboard permissions
    VIEW_DASHBOARD,
    
    // Ticket permissions
    CREATE_TICKET,
    VIEW_TICKET,
    UPDATE_TICKET,
    DELETE_TICKET,
    ASSIGN_TICKET,
    CLOSE_TICKET,
    
    // Expert permissions
    PICK_TICKET,
    MANAGE_AVAILABILITY,
    
    // User management permissions
    VIEW_USERS,
    CREATE_USER,
    UPDATE_USER,
    DELETE_USER,
    
    // Expert management permissions
    VIEW_EXPERTS,
    CREATE_EXPERT,
    UPDATE_EXPERT,
    DELETE_EXPERT,
    
    // AI Tool permissions
    USE_AI_TOOL,
    
    // Admin permissions
    MANAGE_SYSTEM,
    VIEW_REPORTS,
    
    // Support permissions
    SUPPORT_USERS
}
