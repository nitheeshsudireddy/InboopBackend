package com.inboop.backend.instagram.service;

import com.inboop.backend.auth.entity.User;
import com.inboop.backend.business.entity.Business;
import com.inboop.backend.business.repository.BusinessRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Service for fetching Facebook Pages and Instagram Business accounts.
 *
 * Flow:
 * 1. Use Facebook user access token to fetch Pages via /me/accounts
 * 2. For each Page, fetch instagram_business_account field
 * 3. Persist the mapping (User -> Page -> Instagram Business Account)
 */
@Service
public class InstagramBusinessService {

    private static final Logger log = LoggerFactory.getLogger(InstagramBusinessService.class);
    private static final String GRAPH_API_BASE = "https://graph.facebook.com/v21.0";

    private final BusinessRepository businessRepository;
    private final RestTemplate restTemplate;

    @Value("${instagram.api.version:v21.0}")
    private String apiVersion;

    public InstagramBusinessService(BusinessRepository businessRepository) {
        this.businessRepository = businessRepository;
        this.restTemplate = new RestTemplate();
    }

    /**
     * Fetch Facebook Pages and linked Instagram Business accounts for the user.
     * Stores connection context in database for later status checks.
     *
     * @param user           The Inboop user who authorized the connection
     * @param facebookUserId The Facebook user ID from OAuth
     * @param accessToken    The Facebook user access token
     * @param tokenExpiresAt When the token expires (nullable)
     * @return List of connected Business entities
     */
    @Transactional
    public List<Business> connectInstagramAccounts(User user, String facebookUserId,
                                                    String accessToken, Instant tokenExpiresAt) {
        log.info("[OAuth] Starting Instagram connection for user_id={}, facebook_user_id={}",
                user.getId(), facebookUserId);

        List<Business> connectedBusinesses = new ArrayList<>();
        List<String> allPageIds = new ArrayList<>();

        try {
            // Step 1: Fetch Facebook Pages the user manages
            List<FacebookPage> pages = fetchFacebookPages(accessToken);

            // Collect all page IDs for connection context
            for (FacebookPage page : pages) {
                allPageIds.add(page.id);
            }
            String availablePageIds = String.join(",", allPageIds);

            log.info("[OAuth] Found {} Facebook Pages for user_id={}, page_ids={}",
                    pages.size(), user.getId(), allPageIds);

            if (pages.isEmpty()) {
                log.warn("[OAuth] No pages found for user_id={}. Check pages_show_list permission.", user.getId());
                // Store a placeholder business with error context for later status checks
                storeConnectionErrorContext(user, facebookUserId, accessToken, tokenExpiresAt,
                        null, "NO_PAGES_FOUND");
                return connectedBusinesses;
            }

            // Step 2: For each Page, check for linked Instagram Business Account
            int pagesWithIg = 0;
            int pagesWithoutIg = 0;

            for (FacebookPage page : pages) {
                try {
                    log.debug("[OAuth] Checking page_id={} ({}) for Instagram account", page.id, page.name);
                    InstagramAccount igAccount = fetchInstagramBusinessAccount(page.id, accessToken);

                    if (igAccount != null) {
                        pagesWithIg++;
                        // Step 3: Persist the mapping with connection context
                        Business business = findOrCreateBusiness(user, facebookUserId, page, igAccount);
                        business.setAccessToken(accessToken);
                        if (tokenExpiresAt != null) {
                            business.setTokenExpiresAt(LocalDateTime.ofInstant(tokenExpiresAt, ZoneId.systemDefault()));
                        }
                        business.setIsActive(true);

                        // Store connection context
                        business.setAvailablePageIds(availablePageIds);
                        business.setSelectedPageId(page.id);
                        business.setLastIgAccountIdSeen(igAccount.id);
                        business.setLastConnectionError(null); // Clear any previous error
                        business.setConnectionRetryAt(null); // Clear any cooldown
                        business.setLastStatusCheckAt(LocalDateTime.now());

                        businessRepository.save(business);
                        connectedBusinesses.add(business);

                        log.info("[OAuth] Connected: ig_account_id={}, ig_username={}, page_id={}, page_name={}",
                                igAccount.id, igAccount.username, page.id, page.name);
                    } else {
                        pagesWithoutIg++;
                        log.debug("[OAuth] Page {} ({}) has no linked Instagram Business Account",
                                page.id, page.name);
                    }
                } catch (Exception e) {
                    log.warn("[OAuth] Failed to fetch Instagram for page_id={}: {}", page.id, e.getMessage());
                }
            }

            log.info("[OAuth] Connection complete for user_id={}: pages_with_ig={}, pages_without_ig={}, total_connected={}",
                    user.getId(), pagesWithIg, pagesWithoutIg, connectedBusinesses.size());

            // If no IG accounts found, store error context
            if (connectedBusinesses.isEmpty() && !pages.isEmpty()) {
                log.warn("[OAuth] No Instagram accounts found for user_id={} despite having {} pages",
                        user.getId(), pages.size());
                storeConnectionErrorContext(user, facebookUserId, accessToken, tokenExpiresAt,
                        availablePageIds, "IG_NOT_LINKED_TO_PAGE");
            }

        } catch (Exception e) {
            log.error("[OAuth] Failed to connect Instagram for user_id={}: {}", user.getId(), e.getMessage(), e);
            throw new RuntimeException("Failed to connect Instagram accounts: " + e.getMessage(), e);
        }

        return connectedBusinesses;
    }

    /**
     * Store connection error context when OAuth succeeds but no IG accounts found.
     * This allows the status endpoint to provide meaningful feedback.
     */
    private void storeConnectionErrorContext(User user, String facebookUserId, String accessToken,
                                             Instant tokenExpiresAt, String availablePageIds, String errorReason) {
        // Find or create a placeholder business for this user
        List<Business> existingBusinesses = businessRepository.findByOwnerId(user.getId());
        Business business;

        if (existingBusinesses.isEmpty()) {
            business = new Business();
            business.setOwner(user);
            business.setName("Pending Connection");
        } else {
            business = existingBusinesses.get(0);
        }

        business.setFacebookUserId(facebookUserId);
        business.setAccessToken(accessToken);
        if (tokenExpiresAt != null) {
            business.setTokenExpiresAt(LocalDateTime.ofInstant(tokenExpiresAt, ZoneId.systemDefault()));
        }
        business.setIsActive(false); // Not active since no IG account linked
        business.setAvailablePageIds(availablePageIds);
        business.setLastConnectionError(errorReason);
        business.setLastStatusCheckAt(LocalDateTime.now());

        businessRepository.save(business);
        log.info("[OAuth] Stored connection error context: user_id={}, error={}", user.getId(), errorReason);
    }

    /**
     * Fetch all Facebook Pages the user manages.
     * GET https://graph.facebook.com/v21.0/me/accounts
     */
    private List<FacebookPage> fetchFacebookPages(String accessToken) {
        String url = GRAPH_API_BASE + "/me/accounts?access_token=" + accessToken;
        log.debug("Fetching Facebook Pages from: {}", GRAPH_API_BASE + "/me/accounts");

        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            Map<String, Object> body = response.getBody();

            log.debug("Facebook /me/accounts response: {}", body);

            if (body == null) {
                log.warn("Null response from Facebook /me/accounts API");
                return List.of();
            }

            // Check for error in response
            if (body.containsKey("error")) {
                Map<String, Object> error = (Map<String, Object>) body.get("error");
                log.error("Facebook API error: {} - {}", error.get("code"), error.get("message"));
                throw new RuntimeException("Facebook API error: " + error.get("message"));
            }

            if (!body.containsKey("data")) {
                log.warn("No 'data' field in Facebook response. Full response: {}", body);
                return List.of();
            }

            List<Map<String, Object>> data = (List<Map<String, Object>>) body.get("data");

            if (data == null || data.isEmpty()) {
                log.warn("Facebook returned empty pages list. User may not have any Pages or lacks pages_show_list permission.");
                return List.of();
            }

            List<FacebookPage> pages = new ArrayList<>();

            for (Map<String, Object> pageData : data) {
                FacebookPage page = new FacebookPage();
                page.id = (String) pageData.get("id");
                page.name = (String) pageData.get("name");
                page.accessToken = (String) pageData.get("access_token"); // Page access token
                pages.add(page);
                log.debug("Found Facebook Page: {} (ID: {})", page.name, page.id);
            }

            return pages;
        } catch (RestClientException e) {
            log.error("Failed to fetch Facebook Pages. Error: {} - Response: {}", e.getMessage(), e.getClass().getSimpleName());
            throw new RuntimeException("Failed to fetch Facebook Pages: " + e.getMessage(), e);
        }
    }

    /**
     * Fetch the Instagram Business Account linked to a Facebook Page.
     *
     * Step 1: GET /{page-id}?fields=instagram_business_account to get IG account ID
     * Step 2: GET /{ig-account-id}?fields=id,username,name,profile_picture_url to get details
     *
     * @return InstagramAccount with id and username, or null if not linked
     */
    private InstagramAccount fetchInstagramBusinessAccount(String pageId, String accessToken) {
        // Step 1: Get Instagram Business Account ID from the Page
        String pageUrl = GRAPH_API_BASE + "/" + pageId + "?fields=instagram_business_account&access_token=" + accessToken;

        try {
            ResponseEntity<Map> pageResponse = restTemplate.getForEntity(pageUrl, Map.class);
            Map<String, Object> pageBody = pageResponse.getBody();

            if (pageBody == null) {
                return null;
            }

            Map<String, Object> instagramAccountRef = (Map<String, Object>) pageBody.get("instagram_business_account");
            if (instagramAccountRef == null) {
                return null;
            }

            String igAccountId = (String) instagramAccountRef.get("id");
            if (igAccountId == null) {
                return null;
            }

            // Step 2: Fetch Instagram account details (username, name, profile picture)
            String igUrl = GRAPH_API_BASE + "/" + igAccountId + "?fields=id,username,name,profile_picture_url&access_token=" + accessToken;

            try {
                ResponseEntity<Map> igResponse = restTemplate.getForEntity(igUrl, Map.class);
                Map<String, Object> igBody = igResponse.getBody();

                InstagramAccount account = new InstagramAccount();
                account.id = igAccountId;

                if (igBody != null) {
                    account.username = (String) igBody.get("username");
                    account.name = (String) igBody.get("name");
                    account.profilePictureUrl = (String) igBody.get("profile_picture_url");
                }

                return account;
            } catch (RestClientException e) {
                // If we can't get details, still return the account with just the ID
                log.warn("Failed to fetch Instagram account details for {}: {}", igAccountId, e.getMessage());
                InstagramAccount account = new InstagramAccount();
                account.id = igAccountId;
                return account;
            }

        } catch (RestClientException e) {
            log.warn("Failed to fetch Instagram account for page {}: {}", pageId, e.getMessage());
            return null;
        }
    }

    /**
     * Find existing business or create a new one.
     */
    private Business findOrCreateBusiness(User user, String facebookUserId,
                                          FacebookPage page, InstagramAccount igAccount) {
        // First try to find by Instagram Business Account ID
        return businessRepository.findByInstagramBusinessAccountId(igAccount.id)
                .map(existing -> {
                    // Update existing business with latest info
                    existing.setFacebookUserId(facebookUserId);
                    existing.setFacebookPageId(page.id);
                    existing.setName(page.name);
                    if (igAccount.username != null) {
                        existing.setInstagramUsername(igAccount.username);
                    }
                    return existing;
                })
                .orElseGet(() -> {
                    // Try to find by Facebook Page ID
                    return businessRepository.findByFacebookPageId(page.id)
                            .map(existing -> {
                                // Update existing business
                                existing.setInstagramBusinessAccountId(igAccount.id);
                                if (igAccount.username != null) {
                                    existing.setInstagramUsername(igAccount.username);
                                }
                                return existing;
                            })
                            .orElseGet(() -> {
                                // Create new business
                                Business newBusiness = new Business();
                                newBusiness.setOwner(user);
                                newBusiness.setName(page.name);
                                newBusiness.setFacebookUserId(facebookUserId);
                                newBusiness.setFacebookPageId(page.id);
                                newBusiness.setInstagramBusinessAccountId(igAccount.id);
                                if (igAccount.username != null) {
                                    newBusiness.setInstagramUsername(igAccount.username);
                                }
                                return newBusiness;
                            });
                });
    }

    /**
     * Simple DTO for Facebook Page data.
     */
    private static class FacebookPage {
        String id;
        String name;
        String accessToken;
    }

    /**
     * Simple DTO for Instagram Business Account data.
     */
    private static class InstagramAccount {
        String id;
        String username;
        String name;
        String profilePictureUrl;
    }
}
