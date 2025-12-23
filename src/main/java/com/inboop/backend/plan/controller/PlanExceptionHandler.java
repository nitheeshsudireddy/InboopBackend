package com.inboop.backend.plan.controller;

import com.inboop.backend.plan.dto.PlanLimitErrorResponse;
import com.inboop.backend.plan.exception.PlanLimitException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Global exception handler for plan-related exceptions.
 */
@RestControllerAdvice
public class PlanExceptionHandler {

    @ExceptionHandler(PlanLimitException.class)
    public ResponseEntity<PlanLimitErrorResponse> handlePlanLimitException(PlanLimitException ex) {
        PlanLimitErrorResponse response = new PlanLimitErrorResponse(
            ex.getCode(),
            ex.getMessage(),
            ex.isUpgradeSuggested(),
            ex.getCurrentPlan(),
            ex.getRequiredPlan(),
            ex.getFeature()
        );
        return ResponseEntity.status(ex.getStatus()).body(response);
    }
}
