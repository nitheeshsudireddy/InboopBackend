package com.inboop.backend.contact.entity;

import com.inboop.backend.business.entity.Business;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Contact entity representing a unified customer profile across channels.
 * Per inboop-domain-model-v1.md and inboop-contact-journey-v1.md
 */
@Entity
@Table(name = "contacts")
public class Contact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    // Identity
    @Column(name = "name")
    private String name;

    @Column(name = "email")
    private String email;

    @Column(name = "phone")
    private String phone;

    // Channel handles as JSONB map
    // Example: {"instagram": "username", "whatsapp": "+1234567890"}
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "handles", columnDefinition = "jsonb")
    private Map<String, String> handles = new HashMap<>();

    // Engagement tracking
    @Column(name = "first_seen_at")
    private LocalDateTime firstSeenAt;

    @Column(name = "last_seen_at")
    private LocalDateTime lastSeenAt;

    // Derived metrics (denormalized for performance)
    @Column(name = "total_orders")
    private Integer totalOrders = 0;

    @Column(name = "total_revenue", precision = 12, scale = 2)
    private BigDecimal totalRevenue = BigDecimal.ZERO;

    // Soft delete / archive
    @Column(name = "archived_at")
    private LocalDateTime archivedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // Timestamps
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (firstSeenAt == null) {
            firstSeenAt = LocalDateTime.now();
        }
        lastSeenAt = LocalDateTime.now();
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

    public Business getBusiness() {
        return business;
    }

    public void setBusiness(Business business) {
        this.business = business;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Map<String, String> getHandles() {
        return handles;
    }

    public void setHandles(Map<String, String> handles) {
        this.handles = handles;
    }

    public LocalDateTime getFirstSeenAt() {
        return firstSeenAt;
    }

    public void setFirstSeenAt(LocalDateTime firstSeenAt) {
        this.firstSeenAt = firstSeenAt;
    }

    public LocalDateTime getLastSeenAt() {
        return lastSeenAt;
    }

    public void setLastSeenAt(LocalDateTime lastSeenAt) {
        this.lastSeenAt = lastSeenAt;
    }

    public Integer getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(Integer totalOrders) {
        this.totalOrders = totalOrders;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
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

    public boolean isArchived() {
        return archivedAt != null;
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    // Helper methods for handles
    public void addHandle(String channel, String handle) {
        if (handles == null) {
            handles = new HashMap<>();
        }
        handles.put(channel.toLowerCase(), handle);
    }

    public String getHandle(String channel) {
        if (handles == null) {
            return null;
        }
        return handles.get(channel.toLowerCase());
    }
}
