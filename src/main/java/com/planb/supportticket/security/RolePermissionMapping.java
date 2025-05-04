package com.planb.supportticket.security;

import com.planb.supportticket.entity.enums.Permission;
import com.planb.supportticket.entity.enums.UserRole;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Maps roles to their corresponding permissions.
 * This class defines which permissions are granted to each role.
 */
public class RolePermissionMapping {

    private static final Map<UserRole, Set<Permission>> ROLE_PERMISSIONS = new HashMap<>();

    static {
        // Initialize USER permissions
        Set<Permission> userPermissions = new HashSet<>();
        userPermissions.add(Permission.VIEW_DASHBOARD);
        userPermissions.add(Permission.CREATE_TICKET);
        userPermissions.add(Permission.VIEW_TICKET);
        userPermissions.add(Permission.UPDATE_TICKET);
        userPermissions.add(Permission.USE_AI_TOOL);
        ROLE_PERMISSIONS.put(UserRole.USER, Collections.unmodifiableSet(userPermissions));

        // Initialize EXPERT permissions
        Set<Permission> expertPermissions = new HashSet<>();
        expertPermissions.add(Permission.VIEW_DASHBOARD);
        expertPermissions.add(Permission.VIEW_TICKET);
        expertPermissions.add(Permission.UPDATE_TICKET);
        expertPermissions.add(Permission.PICK_TICKET);
        expertPermissions.add(Permission.ASSIGN_TICKET);
        expertPermissions.add(Permission.CLOSE_TICKET);
        expertPermissions.add(Permission.MANAGE_AVAILABILITY);
        ROLE_PERMISSIONS.put(UserRole.EXPERT, Collections.unmodifiableSet(expertPermissions));

        // Initialize SUPPORT permissions
        Set<Permission> supportPermissions = new HashSet<>();
        supportPermissions.add(Permission.VIEW_DASHBOARD);
        supportPermissions.add(Permission.VIEW_TICKET);
        supportPermissions.add(Permission.UPDATE_TICKET);
        supportPermissions.add(Permission.ASSIGN_TICKET);
        supportPermissions.add(Permission.CLOSE_TICKET);
        supportPermissions.add(Permission.VIEW_USERS);
        supportPermissions.add(Permission.VIEW_EXPERTS);
        supportPermissions.add(Permission.SUPPORT_USERS);
        ROLE_PERMISSIONS.put(UserRole.SUPPORT, Collections.unmodifiableSet(supportPermissions));

        // Initialize ADMIN permissions (has all permissions)
        Set<Permission> adminPermissions = new HashSet<>();
        for (Permission permission : Permission.values()) {
            adminPermissions.add(permission);
        }
        ROLE_PERMISSIONS.put(UserRole.ADMIN, Collections.unmodifiableSet(adminPermissions));
    }

    /**
     * Gets the permissions for a specific role.
     *
     * @param role the role
     * @return the set of permissions for the role
     */
    public static Set<Permission> getPermissions(UserRole role) {
        return ROLE_PERMISSIONS.getOrDefault(role, Collections.emptySet());
    }

    /**
     * Checks if a role has a specific permission.
     *
     * @param role the role
     * @param permission the permission
     * @return true if the role has the permission, false otherwise
     */
    public static boolean hasPermission(UserRole role, Permission permission) {
        return ROLE_PERMISSIONS.getOrDefault(role, Collections.emptySet()).contains(permission);
    }
}
