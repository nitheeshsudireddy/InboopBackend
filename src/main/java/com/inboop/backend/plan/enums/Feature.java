package com.inboop.backend.plan.enums;

/**
 * Feature keys for plan-based feature gating.
 */
public enum Feature {
    // User management
    INVITE_USERS,

    // Analytics
    ANALYTICS_DASHBOARD,
    ANALYTICS_EXPORT,

    // Integrations
    API_ACCESS,
    WEBHOOK_ACCESS,

    // Advanced features
    CUSTOM_LABELS,
    BULK_OPERATIONS,
    PRIORITY_SUPPORT;

    /**
     * Get the minimum plan required for this feature.
     */
    public Plan getMinimumPlan() {
        return switch (this) {
            case INVITE_USERS -> Plan.FREE;  // All plans can invite (up to their limit)
            case ANALYTICS_DASHBOARD, CUSTOM_LABELS -> Plan.PRO;
            case ANALYTICS_EXPORT, API_ACCESS, WEBHOOK_ACCESS, BULK_OPERATIONS -> Plan.PRO;
            case PRIORITY_SUPPORT -> Plan.ENTERPRISE;
        };
    }
}
