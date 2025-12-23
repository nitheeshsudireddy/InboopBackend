package com.inboop.backend.plan.exception;

import com.inboop.backend.plan.enums.Feature;
import com.inboop.backend.plan.enums.Plan;
import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a plan limit is reached.
 * Returns structured error response with upgrade suggestion.
 */
public class PlanLimitException extends RuntimeException {

    private final String code;
    private final HttpStatus status;
    private final boolean upgradeSuggested;
    private final Plan currentPlan;
    private final Plan requiredPlan;
    private final Feature feature;

    public PlanLimitException(String code, String message, Plan currentPlan, Plan requiredPlan, Feature feature) {
        super(message);
        this.code = code;
        this.status = HttpStatus.PAYMENT_REQUIRED; // 402
        this.upgradeSuggested = true;
        this.currentPlan = currentPlan;
        this.requiredPlan = requiredPlan;
        this.feature = feature;
    }

    public PlanLimitException(String code, String message, Plan currentPlan) {
        super(message);
        this.code = code;
        this.status = HttpStatus.PAYMENT_REQUIRED; // 402
        this.upgradeSuggested = true;
        this.currentPlan = currentPlan;
        this.requiredPlan = null;
        this.feature = null;
    }

    public String getCode() {
        return code;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public boolean isUpgradeSuggested() {
        return upgradeSuggested;
    }

    public Plan getCurrentPlan() {
        return currentPlan;
    }

    public Plan getRequiredPlan() {
        return requiredPlan;
    }

    public Feature getFeature() {
        return feature;
    }

    // Factory methods for common scenarios

    /**
     * User limit reached for the current plan.
     */
    public static PlanLimitException userLimitReached(Plan currentPlan, int maxUsers) {
        String message = String.format(
            "%s plan supports up to %d users. Upgrade to add more team members.",
            currentPlan.name(), maxUsers
        );
        return new PlanLimitException("PLAN_LIMIT_REACHED", message, currentPlan);
    }

    /**
     * Feature not available on current plan.
     */
    public static PlanLimitException featureNotAvailable(Feature feature, Plan currentPlan, Plan requiredPlan) {
        String message = String.format(
            "%s is not available on your %s plan. Upgrade to %s to unlock this feature.",
            formatFeatureName(feature), currentPlan.name(), requiredPlan.name()
        );
        return new PlanLimitException("PLAN_LIMIT_REACHED", message, currentPlan, requiredPlan, feature);
    }

    /**
     * Plan is expired.
     */
    public static PlanLimitException planExpired(Plan currentPlan) {
        String message = "Your plan has expired. Please renew to continue using premium features.";
        return new PlanLimitException("PLAN_EXPIRED", message, currentPlan);
    }

    /**
     * Plan is suspended.
     */
    public static PlanLimitException planSuspended(Plan currentPlan) {
        String message = "Your plan is suspended. Please contact support.";
        return new PlanLimitException("PLAN_SUSPENDED", message, currentPlan);
    }

    private static String formatFeatureName(Feature feature) {
        return switch (feature) {
            case INVITE_USERS -> "Inviting users";
            case ANALYTICS_DASHBOARD -> "Analytics dashboard";
            case ANALYTICS_EXPORT -> "Analytics export";
            case API_ACCESS -> "API access";
            case WEBHOOK_ACCESS -> "Webhook access";
            case CUSTOM_LABELS -> "Custom labels";
            case BULK_OPERATIONS -> "Bulk operations";
            case PRIORITY_SUPPORT -> "Priority support";
        };
    }
}
