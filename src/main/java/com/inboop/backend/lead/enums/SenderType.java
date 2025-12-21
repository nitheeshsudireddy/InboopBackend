package com.inboop.backend.lead.enums;

/**
 * Sender type enum for messages.
 * Per inboop-domain-model-v1.md
 */
public enum SenderType {
    CUSTOMER,  // Message sent by customer
    BUSINESS,  // Message sent by business user
    SYSTEM     // System-generated message (e.g., auto-replies)
}
