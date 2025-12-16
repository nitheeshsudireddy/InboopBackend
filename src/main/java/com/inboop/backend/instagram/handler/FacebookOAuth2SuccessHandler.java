package com.inboop.backend.instagram.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

/**
 * Handles successful Facebook/Instagram OAuth2 authentication.
 *
 * Spring Security OAuth2 Client handles:
 * 1. Generating the authorization URL with proper encoding
 * 2. Creating a secure random 'state' parameter for CSRF protection
 * 3. Validating the state on callback
 * 4. Exchanging the authorization code for access token
 *
 * This handler receives the result and redirects to the frontend.
 *
 * WHY SPRING SECURITY OAUTH2 IS SAFER THAN MANUAL URL BUILDING:
 * - Proper URL encoding (avoids injection attacks)
 * - Automatic CSRF protection via state parameter
 * - Secure token storage in OAuth2AuthorizedClientService
 * - Handles edge cases like token refresh
 * - Less code = fewer bugs
 */
@Component
public class FacebookOAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private static final Logger log = LoggerFactory.getLogger(FacebookOAuth2SuccessHandler.class);

    private final OAuth2AuthorizedClientService authorizedClientService;

    @Value("${app.frontend.url:https://app.inboop.com}")
    private String frontendUrl;

    public FacebookOAuth2SuccessHandler(OAuth2AuthorizedClientService authorizedClientService) {
        this.authorizedClientService = authorizedClientService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        if (!(authentication instanceof OAuth2AuthenticationToken oauthToken)) {
            log.error("Unexpected authentication type: {}", authentication.getClass());
            redirectWithError(response, "unexpected_auth_type");
            return;
        }

        String registrationId = oauthToken.getAuthorizedClientRegistrationId();

        // Only handle Facebook OAuth (Instagram uses Facebook OAuth)
        if (!"facebook".equals(registrationId)) {
            log.warn("Unexpected OAuth registration: {}", registrationId);
            redirectWithError(response, "wrong_provider");
            return;
        }

        try {
            // Get the authorized client which contains the access token
            OAuth2AuthorizedClient authorizedClient = authorizedClientService.loadAuthorizedClient(
                    registrationId,
                    oauthToken.getName()
            );

            if (authorizedClient == null || authorizedClient.getAccessToken() == null) {
                log.error("No access token found for Facebook OAuth");
                redirectWithError(response, "no_token");
                return;
            }

            String accessToken = authorizedClient.getAccessToken().getTokenValue();
            Instant expiresAt = authorizedClient.getAccessToken().getExpiresAt();
            long expiresIn = expiresAt != null ? expiresAt.getEpochSecond() - Instant.now().getEpochSecond() : 0;

            log.info("Successfully obtained Facebook access token, expires in {} seconds", expiresIn);

            // TODO: In production, store the token in database associated with the user
            // For now, redirect to frontend with the token
            String redirectUrl = frontendUrl + "/settings?success=true&instagram_token=" +
                    URLEncoder.encode(accessToken, StandardCharsets.UTF_8);

            response.sendRedirect(redirectUrl);

        } catch (Exception e) {
            log.error("Error processing Facebook OAuth success: {}", e.getMessage(), e);
            redirectWithError(response, e.getMessage());
        }
    }

    private void redirectWithError(HttpServletResponse response, String error) throws IOException {
        String redirectUrl = frontendUrl + "/settings?error=" +
                URLEncoder.encode(error, StandardCharsets.UTF_8);
        response.sendRedirect(redirectUrl);
    }
}
