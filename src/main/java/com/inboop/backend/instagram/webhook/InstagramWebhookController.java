package com.inboop.backend.instagram.webhook;

import com.inboop.backend.instagram.dto.WebhookPayload;
import com.inboop.backend.instagram.service.InstagramWebhookService;
import com.inboop.backend.shared.dto.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/webhooks/instagram")
public class InstagramWebhookController {

    private static final Logger logger = LoggerFactory.getLogger(InstagramWebhookController.class);

    private final InstagramWebhookService webhookService;

    @Value("${instagram.webhook.verify-token:inboop_verify_token}")
    private String verifyToken;

    public InstagramWebhookController(InstagramWebhookService webhookService) {
        this.webhookService = webhookService;
    }

    /**
     * Webhook verification endpoint for Instagram
     * GET /api/v1/webhooks/instagram
     */
    @GetMapping
    public ResponseEntity<String> verifyWebhook(
            @RequestParam("hub.mode") String mode,
            @RequestParam("hub.verify_token") String token,
            @RequestParam("hub.challenge") String challenge) {

        logger.info("Webhook verification request received");

        if ("subscribe".equals(mode) && verifyToken.equals(token)) {
            logger.info("Webhook verified successfully");
            return ResponseEntity.ok(challenge);
        } else {
            logger.warn("Webhook verification failed");
            return ResponseEntity.status(403).body("Forbidden");
        }
    }

    /**
     * Webhook endpoint to receive Instagram messages
     * POST /api/v1/webhooks/instagram
     *
     * IMPORTANT: Always return 200 OK to Meta, even if processing fails.
     * Meta will retry failed webhooks, which could cause duplicate processing issues.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<String>> receiveWebhook(@RequestBody WebhookPayload payload) {
        logger.info("Received webhook payload: object={}", payload.getObject());

        try {
            // Process the webhook synchronously for now
            // TODO: Move to async processing with message queue for high volume
            webhookService.processWebhook(payload);

            return ResponseEntity.ok(ApiResponse.success("Webhook received"));
        } catch (Exception e) {
            logger.error("Error processing webhook", e);
            // Always return 200 to prevent Meta from retrying
            return ResponseEntity.ok(ApiResponse.success("Webhook received"));
        }
    }
}
