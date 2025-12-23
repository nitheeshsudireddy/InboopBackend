package com.inboop.backend.plan.enums;

/**
 * Available subscription plans with their feature limits.
 */
public enum Plan {
    FREE(2, false, false),
    PRO(5, true, true),
    ENTERPRISE(100, true, true);

    private final int maxUsers;
    private final boolean analyticsEnabled;
    private final boolean apiAccessEnabled;

    Plan(int maxUsers, boolean analyticsEnabled, boolean apiAccessEnabled) {
        this.maxUsers = maxUsers;
        this.analyticsEnabled = analyticsEnabled;
        this.apiAccessEnabled = apiAccessEnabled;
    }

    public int getMaxUsers() {
        return maxUsers;
    }

    public boolean isAnalyticsEnabled() {
        return analyticsEnabled;
    }

    public boolean isApiAccessEnabled() {
        return apiAccessEnabled;
    }
}
