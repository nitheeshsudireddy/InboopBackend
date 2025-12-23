package com.inboop.backend.workspace.enums;

import com.inboop.backend.rbac.Permission;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * Roles within a workspace.
 * Each role has a set of permissions that define what actions users with that role can perform.
 */
public enum WorkspaceRole {
    /**
     * Admin: Full access including team management.
     */
    ADMIN,

    /**
     * Editor: Can read and write conversations, leads, orders.
     * Cannot manage team.
     */
    EDITOR,

    /**
     * Viewer: Read-only access to conversations, leads, orders, analytics.
     * Cannot send messages, create/update leads or orders.
     */
    VIEWER,

    /**
     * @deprecated Use EDITOR instead. Kept for backward compatibility during migration.
     */
    @Deprecated
    MEMBER;

    // Permission sets for each role
    private static final Set<Permission> VIEWER_PERMISSIONS = EnumSet.of(
            Permission.CONVERSATION_READ,
            Permission.LEAD_READ,
            Permission.ORDER_READ,
            Permission.ANALYTICS_READ
    );

    private static final Set<Permission> EDITOR_PERMISSIONS = EnumSet.of(
            // Viewer permissions
            Permission.CONVERSATION_READ,
            Permission.LEAD_READ,
            Permission.ORDER_READ,
            Permission.ANALYTICS_READ,
            // Editor-specific permissions
            Permission.CONVERSATION_SEND,
            Permission.CONVERSATION_ASSIGN,
            Permission.LEAD_WRITE,
            Permission.LEAD_ASSIGN,
            Permission.ORDER_WRITE,
            Permission.ORDER_ASSIGN
    );

    private static final Set<Permission> ADMIN_PERMISSIONS = EnumSet.of(
            // All Editor permissions
            Permission.CONVERSATION_READ,
            Permission.LEAD_READ,
            Permission.ORDER_READ,
            Permission.ANALYTICS_READ,
            Permission.CONVERSATION_SEND,
            Permission.CONVERSATION_ASSIGN,
            Permission.LEAD_WRITE,
            Permission.LEAD_ASSIGN,
            Permission.ORDER_WRITE,
            Permission.ORDER_ASSIGN,
            // Admin-specific permissions
            Permission.TEAM_MANAGE
    );

    /**
     * Get the set of permissions for this role.
     */
    public Set<Permission> getPermissions() {
        return switch (this) {
            case ADMIN -> Collections.unmodifiableSet(ADMIN_PERMISSIONS);
            case EDITOR, MEMBER -> Collections.unmodifiableSet(EDITOR_PERMISSIONS);
            case VIEWER -> Collections.unmodifiableSet(VIEWER_PERMISSIONS);
        };
    }

    /**
     * Check if this role has a specific permission.
     */
    public boolean hasPermission(Permission permission) {
        return getPermissions().contains(permission);
    }

    /**
     * Get the display name for this role.
     */
    public String getDisplayName() {
        return switch (this) {
            case ADMIN -> "Admin";
            case EDITOR, MEMBER -> "Editor";
            case VIEWER -> "Viewer";
        };
    }

    /**
     * Get a human-readable description of this role.
     */
    public String getDescription() {
        return switch (this) {
            case ADMIN -> "Full access including team management";
            case EDITOR, MEMBER -> "Can read and write conversations, leads, and orders";
            case VIEWER -> "Read-only access";
        };
    }
}
