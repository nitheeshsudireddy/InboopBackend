package com.inboop.backend.instagram.controller;

import com.inboop.backend.auth.entity.User;
import com.inboop.backend.auth.repository.UserRepository;
import com.inboop.backend.business.entity.Business;
import com.inboop.backend.business.repository.BusinessRepository;
import com.inboop.backend.instagram.util.SecureCookieUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Controller for Instagram Business account connection.
 *
 * Flow:
 * 1. GET /api/v1/integrations/instagram/connect - Sets secure cookie, redirects to OAuth
 * 2. User authorizes on Facebook
 * 3. FacebookOAuth2SuccessHandler reads cookie, stores token, redirects to frontend
 * 4. GET /api/v1/integrations/instagram/status - Check connection status
 */
@RestController
@RequestMapping("/api/v1/integrations/instagram")
public class InstagramConnectionController {

    private static final Logger log = LoggerFactory.getLogger(InstagramConnectionController.class);
    public static final String CONNECTION_COOKIE_NAME = "ig_connect";

    private final SecureCookieUtil secureCookieUtil;
    private final UserRepository userRepository;
    private final BusinessRepository businessRepository;

    @Value("${spring.security.oauth2.client.registration.facebook.client-id:}")
    private String clientId;

    @Value("${app.cookie.secure:true}")
    private boolean secureCookie;

    public InstagramConnectionController(SecureCookieUtil secureCookieUtil,
                                         UserRepository userRepository,
                                         BusinessRepository businessRepository) {
        this.secureCookieUtil = secureCookieUtil;
        this.userRepository = userRepository;
        this.businessRepository = businessRepository;
    }

    /**
     * Initiate Instagram connection.
     * Sets a secure cookie with user ID and redirects to OAuth.
     *
     * GET /api/v1/integrations/instagram/connect
     */
    @GetMapping("/connect")
    public void initiateConnection(@AuthenticationPrincipal UserDetails userDetails,
                                   HttpServletResponse response) throws IOException {

        if (!isConfigured()) {
            log.warn("Instagram OAuth not configured");
            response.sendRedirect("/settings?error=oauth_not_configured");
            return;
        }

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElse(null);

        if (user == null) {
            log.error("User not found: {}", userDetails.getUsername());
            response.sendRedirect("/settings?error=user_not_found");
            return;
        }

        // Create signed cookie with user ID and timestamp
        String cookieValue = user.getId() + ":" + System.currentTimeMillis();
        String signedValue = secureCookieUtil.sign(cookieValue);

        Cookie cookie = new Cookie(CONNECTION_COOKIE_NAME, signedValue);
        cookie.setHttpOnly(true);
        cookie.setSecure(secureCookie);
        cookie.setPath("/");
        cookie.setMaxAge(600); // 10 minutes
        cookie.setAttribute("SameSite", "Lax");

        response.addCookie(cookie);

        log.info("Initiating Instagram connection for user ID: {}", user.getId());

        // Redirect to Spring Security's OAuth2 authorization endpoint
        response.sendRedirect("/oauth2/authorization/facebook");
    }

    /**
     * Get Instagram connection status.
     *
     * GET /api/v1/integrations/instagram/status
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getConnectionStatus(
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElse(null);

        if (user == null) {
            return ResponseEntity.ok(Map.of(
                    "connected", false,
                    "configured", isConfigured()
            ));
        }

        // Find user's active businesses with Instagram connected
        List<Business> businesses = businessRepository.findByOwnerId(user.getId());
        Business connectedBusiness = businesses.stream()
                .filter(b -> b.getIsActive() && b.getAccessToken() != null)
                .findFirst()
                .orElse(null);

        if (connectedBusiness != null) {
            return ResponseEntity.ok(Map.of(
                    "connected", true,
                    "configured", isConfigured(),
                    "instagramUsername", connectedBusiness.getInstagramUsername() != null
                            ? connectedBusiness.getInstagramUsername() : "",
                    "businessName", connectedBusiness.getName()
            ));
        }

        return ResponseEntity.ok(Map.of(
                "connected", false,
                "configured", isConfigured()
        ));
    }

    /**
     * Disconnect Instagram account.
     *
     * DELETE /api/v1/integrations/instagram/disconnect
     */
    @DeleteMapping("/disconnect")
    public ResponseEntity<Map<String, Object>> disconnect(
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElse(null);

        if (user == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "User not found"
            ));
        }

        // Find and deactivate user's businesses
        List<Business> businesses = businessRepository.findByOwnerId(user.getId());
        int disconnected = 0;

        for (Business business : businesses) {
            if (business.getAccessToken() != null) {
                business.setAccessToken(null);
                business.setTokenExpiresAt(null);
                business.setIsActive(false);
                businessRepository.save(business);
                disconnected++;
            }
        }

        log.info("Disconnected {} Instagram account(s) for user ID: {}", disconnected, user.getId());

        return ResponseEntity.ok(Map.of(
                "success", true,
                "disconnected", disconnected
        ));
    }

    private boolean isConfigured() {
        return clientId != null && !clientId.isEmpty() && !clientId.equals("placeholder");
    }
}
