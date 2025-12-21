package com.inboop.backend.lead.enums;

/**
 * Lead status enum.
 * Per inboop-lead-lifecycle-v1.md:
 * - NEW is the only non-terminal state
 * - CONVERTED, CLOSED, LOST are terminal states
 *
 * Note: Legacy values (CONTACTED, QUALIFIED, NEGOTIATING, SPAM) are preserved
 * for backward compatibility with existing data. New code should only use
 * NEW, CONVERTED, CLOSED, LOST.
 */
public enum LeadStatus {
    NEW,              // Just received - only non-terminal state
    CONTACTED,        // @deprecated - Legacy: Initial response sent
    QUALIFIED,        // @deprecated - Legacy: Verified as potential customer
    NEGOTIATING,      // @deprecated - Legacy: In discussion
    CONVERTED,        // Terminal: Became a customer/order
    CLOSED,           // Terminal: Closed without conversion (neutral outcome)
    LOST,             // Terminal: Did not convert (negative outcome)
    SPAM              // @deprecated - Legacy: Use conversation intent instead
}
