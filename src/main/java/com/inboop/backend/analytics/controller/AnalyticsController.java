package com.inboop.backend.analytics.controller;

import com.inboop.backend.analytics.dto.AnalyticsOverviewResponse;
import com.inboop.backend.analytics.service.AnalyticsService;
import com.inboop.backend.auth.entity.User;
import com.inboop.backend.auth.repository.UserRepository;
import com.inboop.backend.lead.enums.ChannelType;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * REST controller for analytics endpoints.
 */
@RestController
@RequestMapping("/api/v1/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final UserRepository userRepository;

    public AnalyticsController(AnalyticsService analyticsService, UserRepository userRepository) {
        this.analyticsService = analyticsService;
        this.userRepository = userRepository;
    }

    /**
     * Get analytics overview.
     *
     * GET /api/v1/analytics/overview
     *
     * Query params:
     * - from: Start date (ISO format, defaults to 30 days ago)
     * - to: End date (ISO format, defaults to today)
     * - channel: Optional channel filter (INSTAGRAM, WHATSAPP, MESSENGER)
     * - currency: Optional currency (informational, defaults to INR)
     *
     * @return AnalyticsOverviewResponse with all metrics
     */
    @GetMapping("/overview")
    public ResponseEntity<AnalyticsOverviewResponse> getOverview(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) ChannelType channel,
            @RequestParam(required = false) String currency
    ) {
        // Get current user for business context
        User currentUser = getCurrentUser();
        // TODO: Get businessId from user's active business context
        Long businessId = null; // For now, return all data (demo mode)

        // Default date range: last 30 days
        LocalDate endDate = to != null ? to : LocalDate.now();
        LocalDate startDate = from != null ? from : endDate.minusDays(30);

        // Convert to LocalDateTime (start of day for from, end of day for to)
        LocalDateTime periodStart = startDate.atStartOfDay();
        LocalDateTime periodEnd = endDate.atTime(LocalTime.MAX);

        AnalyticsOverviewResponse response = analyticsService.getOverview(
                businessId,
                channel,
                periodStart,
                periodEnd,
                currency
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Get the current authenticated user.
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        String email = authentication.getName();
        return userRepository.findByEmail(email).orElse(null);
    }
}
