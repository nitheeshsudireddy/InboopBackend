package com.inboop.backend.instagram.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller for Facebook/Instagram OAuth2 status checks.
 *
 * NOTE: The actual OAuth flow is handled by Spring Security OAuth2 Client.
 * To initiate login, redirect to: GET /oauth2/authorization/facebook
 *
 * Spring Security automatically:
 * 1. Generates the authorization URL with proper encoding
 * 2. Creates secure 'state' parameter for CSRF protection
 * 3. Handles the callback at /login/oauth2/code/facebook
 * 4. Validates state and exchanges code for token
 * 5. Calls FacebookOAuth2SuccessHandler which redirects to frontend
 */
@RestController
@RequestMapping("/api/v1/instagram/oauth")
public class FacebookOAuthController {

    private static final Logger log = LoggerFactory.getLogger(FacebookOAuthController.class);

    @Value("${spring.security.oauth2.client.registration.facebook.client-id:}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.facebook.client-secret:}")
    private String clientSecret;

    @Value("${spring.security.oauth2.client.registration.facebook.redirect-uri:}")
    private String redirectUri;

    /**
     * Check if Facebook OAuth is configured.
     *
     * GET /api/v1/instagram/oauth/status
     *
     * Returns:
     * - configured: true if client ID and secret are set
     * - redirectUri: the callback URL that must be registered in Meta Developer Console
     * - authorizationUrl: the URL to redirect users to initiate OAuth
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getOAuthStatus() {
        boolean configured = clientId != null && !clientId.isEmpty() && !clientId.equals("placeholder")
                && clientSecret != null && !clientSecret.isEmpty() && !clientSecret.equals("placeholder");

        return ResponseEntity.ok(Map.of(
                "configured", configured,
                "redirectUri", redirectUri != null ? redirectUri : "",
                "authorizationUrl", "/oauth2/authorization/facebook"
        ));
    }
}
