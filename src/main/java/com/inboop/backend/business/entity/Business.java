package com.inboop.backend.business.entity;

import com.inboop.backend.auth.entity.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "businesses")
public class Business {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "facebook_user_id")
    private String facebookUserId;

    @Column(name = "facebook_page_id")
    private String facebookPageId;

    @Column(name = "instagram_business_account_id", unique = true)
    private String instagramBusinessAccountId;

    @Column(name = "instagram_username")
    private String instagramUsername;

    @Column(name = "access_token", length = 1000)
    private String accessToken;

    @Column(name = "token_expires_at")
    private LocalDateTime tokenExpiresAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(name = "webhook_verified")
    private Boolean webhookVerified = false;

    @Column(name = "is_active")
    private Boolean isActive = true;

    // Connection context fields - stored after OAuth for status checks
    @Column(name = "available_page_ids", length = 2000)
    private String availablePageIds; // Comma-separated list of all page IDs user has access to

    @Column(name = "selected_page_id")
    private String selectedPageId; // The page ID user selected (if multi-page)

    @Column(name = "last_ig_account_id_seen")
    private String lastIgAccountIdSeen; // Last known IG account ID (for ownership mismatch detection)

    @Column(name = "connection_retry_at")
    private LocalDateTime connectionRetryAt; // If in cooldown, when to retry

    @Column(name = "last_connection_error")
    private String lastConnectionError; // Last error reason code

    @Column(name = "last_status_check_at")
    private LocalDateTime lastStatusCheckAt; // When status was last verified

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "archived_at")
    private LocalDateTime archivedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFacebookUserId() {
        return facebookUserId;
    }

    public void setFacebookUserId(String facebookUserId) {
        this.facebookUserId = facebookUserId;
    }

    public String getFacebookPageId() {
        return facebookPageId;
    }

    public void setFacebookPageId(String facebookPageId) {
        this.facebookPageId = facebookPageId;
    }

    public String getInstagramBusinessAccountId() {
        return instagramBusinessAccountId;
    }

    public void setInstagramBusinessAccountId(String instagramBusinessAccountId) {
        this.instagramBusinessAccountId = instagramBusinessAccountId;
    }

    public String getInstagramUsername() {
        return instagramUsername;
    }

    public void setInstagramUsername(String instagramUsername) {
        this.instagramUsername = instagramUsername;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public LocalDateTime getTokenExpiresAt() {
        return tokenExpiresAt;
    }

    public void setTokenExpiresAt(LocalDateTime tokenExpiresAt) {
        this.tokenExpiresAt = tokenExpiresAt;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public Boolean getWebhookVerified() {
        return webhookVerified;
    }

    public void setWebhookVerified(Boolean webhookVerified) {
        this.webhookVerified = webhookVerified;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getAvailablePageIds() {
        return availablePageIds;
    }

    public void setAvailablePageIds(String availablePageIds) {
        this.availablePageIds = availablePageIds;
    }

    public String getSelectedPageId() {
        return selectedPageId;
    }

    public void setSelectedPageId(String selectedPageId) {
        this.selectedPageId = selectedPageId;
    }

    public String getLastIgAccountIdSeen() {
        return lastIgAccountIdSeen;
    }

    public void setLastIgAccountIdSeen(String lastIgAccountIdSeen) {
        this.lastIgAccountIdSeen = lastIgAccountIdSeen;
    }

    public LocalDateTime getConnectionRetryAt() {
        return connectionRetryAt;
    }

    public void setConnectionRetryAt(LocalDateTime connectionRetryAt) {
        this.connectionRetryAt = connectionRetryAt;
    }

    public String getLastConnectionError() {
        return lastConnectionError;
    }

    public void setLastConnectionError(String lastConnectionError) {
        this.lastConnectionError = lastConnectionError;
    }

    public LocalDateTime getLastStatusCheckAt() {
        return lastStatusCheckAt;
    }

    public void setLastStatusCheckAt(LocalDateTime lastStatusCheckAt) {
        this.lastStatusCheckAt = lastStatusCheckAt;
    }

    public LocalDateTime getArchivedAt() {
        return archivedAt;
    }

    public void setArchivedAt(LocalDateTime archivedAt) {
        this.archivedAt = archivedAt;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }

    public boolean isArchived() {
        return archivedAt != null;
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }
}
