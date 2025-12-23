package com.inboop.backend.rbac;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for RBAC-related exceptions.
 */
@RestControllerAdvice
public class RbacExceptionHandler {

    @ExceptionHandler(RbacException.class)
    public ResponseEntity<Map<String, Object>> handleRbacException(RbacException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", ex.getCode());
        response.put("message", ex.getMessage());

        if (ex.getRequiredPermission() != null) {
            response.put("requiredPermission", ex.getRequiredPermission().name());
        }

        return ResponseEntity.status(ex.getStatus()).body(response);
    }
}
