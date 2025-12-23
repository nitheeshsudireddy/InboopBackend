package com.inboop.backend.rbac;

/**
 * Permission keys for RBAC enforcement.
 * Permissions define what actions a user can perform within a workspace.
 */
public enum Permission {
    // Conversation permissions
    CONVERSATION_READ,    // View conversations and messages
    CONVERSATION_SEND,    // Send messages
    CONVERSATION_ASSIGN,  // Assign conversations to team members

    // Lead permissions
    LEAD_READ,            // View leads
    LEAD_WRITE,           // Create/update leads
    LEAD_ASSIGN,          // Assign leads to team members

    // Order permissions
    ORDER_READ,           // View orders
    ORDER_WRITE,          // Create/update orders, fulfill, refund
    ORDER_ASSIGN,         // Assign orders to team members

    // Analytics permissions
    ANALYTICS_READ,       // View analytics dashboard

    // Team management permissions
    TEAM_MANAGE           // Invite users, change roles, remove users
}
