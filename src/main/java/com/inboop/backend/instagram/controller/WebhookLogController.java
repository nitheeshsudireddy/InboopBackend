package com.inboop.backend.instagram.controller;

import com.inboop.backend.auth.entity.User;
import com.inboop.backend.instagram.entity.WebhookLog;
import com.inboop.backend.instagram.repository.WebhookLogRepository;
import com.inboop.backend.shared.dto.ApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Controller for viewing webhook logs.
 * Only accessible by super admin emails.
 */
@RestController
@RequestMapping("/api/v1/admin/webhook-logs")
public class WebhookLogController {

    private final WebhookLogRepository webhookLogRepository;
    private final Set<String> superAdminEmails;

    public WebhookLogController(
            WebhookLogRepository webhookLogRepository,
            @Value("${app.super-admin-emails:}") String superAdminEmailsConfig) {
        this.webhookLogRepository = webhookLogRepository;
        this.superAdminEmails = Arrays.stream(superAdminEmailsConfig.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
    }

    /**
     * Get recent webhook logs (last 50).
     * Only accessible by super admins.
     */
    @GetMapping
    public ResponseEntity<?> getWebhookLogs(@AuthenticationPrincipal User user) {
        if (!isSuperAdmin(user)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied. Super admin required."));
        }

        List<WebhookLog> logs = webhookLogRepository.findTop50ByOrderByReceivedAtDesc();
        return ResponseEntity.ok(ApiResponse.success(logs));
    }

    /**
     * Get a specific webhook log by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getWebhookLog(@PathVariable Long id, @AuthenticationPrincipal User user) {
        if (!isSuperAdmin(user)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied. Super admin required."));
        }

        return webhookLogRepository.findById(id)
                .map(log -> ResponseEntity.ok(ApiResponse.success(log)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Clear all webhook logs (for cleanup).
     */
    @DeleteMapping
    public ResponseEntity<?> clearWebhookLogs(@AuthenticationPrincipal User user) {
        if (!isSuperAdmin(user)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied. Super admin required."));
        }

        long count = webhookLogRepository.count();
        webhookLogRepository.deleteAll();
        return ResponseEntity.ok(ApiResponse.success("Deleted " + count + " webhook logs"));
    }

    private boolean isSuperAdmin(User user) {
        if (user == null || user.getEmail() == null) {
            return false;
        }
        return superAdminEmails.contains(user.getEmail().toLowerCase());
    }
}
