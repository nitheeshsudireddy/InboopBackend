package com.inboop.backend.plan.dto;

import com.inboop.backend.plan.enums.Feature;
import com.inboop.backend.plan.enums.Plan;

/**
 * Structured error response for plan limit errors.
 */
public class PlanLimitErrorResponse {

    private String code;
    private String message;
    private boolean upgradeSuggested;
    private String currentPlan;
    private String requiredPlan;
    private String feature;

    public PlanLimitErrorResponse() {}

    public PlanLimitErrorResponse(String code, String message, boolean upgradeSuggested,
                                   Plan currentPlan, Plan requiredPlan, Feature feature) {
        this.code = code;
        this.message = message;
        this.upgradeSuggested = upgradeSuggested;
        this.currentPlan = currentPlan != null ? currentPlan.name() : null;
        this.requiredPlan = requiredPlan != null ? requiredPlan.name() : null;
        this.feature = feature != null ? feature.name() : null;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isUpgradeSuggested() {
        return upgradeSuggested;
    }

    public void setUpgradeSuggested(boolean upgradeSuggested) {
        this.upgradeSuggested = upgradeSuggested;
    }

    public String getCurrentPlan() {
        return currentPlan;
    }

    public void setCurrentPlan(String currentPlan) {
        this.currentPlan = currentPlan;
    }

    public String getRequiredPlan() {
        return requiredPlan;
    }

    public void setRequiredPlan(String requiredPlan) {
        this.requiredPlan = requiredPlan;
    }

    public String getFeature() {
        return feature;
    }

    public void setFeature(String feature) {
        this.feature = feature;
    }
}
