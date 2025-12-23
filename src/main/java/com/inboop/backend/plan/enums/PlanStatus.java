package com.inboop.backend.plan.enums;

/**
 * Status of a workspace's plan subscription.
 */
public enum PlanStatus {
    ACTIVE,      // Plan is active and features are available
    EXPIRED,     // Plan has expired, may have reduced features
    SUSPENDED    // Plan is suspended (e.g., payment issues)
}
