package com.inboop.backend.lead.entity;

import com.inboop.backend.auth.entity.User;
import com.inboop.backend.business.entity.Business;
import com.inboop.backend.contact.entity.Contact;
import com.inboop.backend.lead.enums.ChannelType;
import com.inboop.backend.lead.enums.IntentLabel;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "conversations")
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Conversation belongs to a business (for multi-tenant support)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    // Optional reference to unified contact profile
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contact_id")
    private Contact contact;

    // Team assignment
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to_user_id")
    private User assignedTo;

    @Column(name = "instagram_conversation_id", unique = true)
    private String instagramConversationId;

    @Column(name = "instagram_scoped_user_id")
    private String instagramScopedUserId;

    // One conversation can have multiple leads over time
    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL)
    private List<Lead> leads = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChannelType channel = ChannelType.INSTAGRAM;

    @Column(name = "customer_handle")
    private String customerHandle;

    @Column(name = "customer_name")
    private String customerName;

    @Column(name = "profile_picture")
    private String profilePicture;

    @Column(name = "last_message")
    private String lastMessage;

    @Column(name = "unread_count")
    private Integer unreadCount = 0;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "started_at", updatable = false)
    private LocalDateTime startedAt;

    @Column(name = "last_message_at")
    private LocalDateTime lastMessageAt;

    @Column(name = "first_message_at")
    private LocalDateTime firstMessageAt;

    // AI Intent fields
    @Enumerated(EnumType.STRING)
    @Column(name = "intent_label")
    private IntentLabel intentLabel;

    @Column(name = "intent_confidence", precision = 5, scale = 4)
    private BigDecimal intentConfidence;

    @Column(name = "intent_evaluated_at")
    private LocalDateTime intentEvaluatedAt;

    // Denormalized counts
    @Column(name = "lead_count")
    private Integer leadCount = 0;

    @Column(name = "order_count")
    private Integer orderCount = 0;

    // Extensibility
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "archived_at")
    private LocalDateTime archivedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Message> messages = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        startedAt = LocalDateTime.now();
        lastMessageAt = LocalDateTime.now();
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

    public List<Lead> getLeads() {
        return leads;
    }

    public void setLeads(List<Lead> leads) {
        this.leads = leads;
    }

    public String getInstagramConversationId() {
        return instagramConversationId;
    }

    public void setInstagramConversationId(String instagramConversationId) {
        this.instagramConversationId = instagramConversationId;
    }

    public String getInstagramScopedUserId() {
        return instagramScopedUserId;
    }

    public void setInstagramScopedUserId(String instagramScopedUserId) {
        this.instagramScopedUserId = instagramScopedUserId;
    }

    public ChannelType getChannel() {
        return channel;
    }

    public void setChannel(ChannelType channel) {
        this.channel = channel;
    }

    public String getCustomerHandle() {
        return customerHandle;
    }

    public void setCustomerHandle(String customerHandle) {
        this.customerHandle = customerHandle;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public Integer getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(Integer unreadCount) {
        this.unreadCount = unreadCount;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getLastMessageAt() {
        return lastMessageAt;
    }

    public void setLastMessageAt(LocalDateTime lastMessageAt) {
        this.lastMessageAt = lastMessageAt;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
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

    public Contact getContact() {
        return contact;
    }

    public void setContact(Contact contact) {
        this.contact = contact;
    }

    public User getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(User assignedTo) {
        this.assignedTo = assignedTo;
    }

    public LocalDateTime getFirstMessageAt() {
        return firstMessageAt;
    }

    public void setFirstMessageAt(LocalDateTime firstMessageAt) {
        this.firstMessageAt = firstMessageAt;
    }

    public IntentLabel getIntentLabel() {
        return intentLabel;
    }

    public void setIntentLabel(IntentLabel intentLabel) {
        this.intentLabel = intentLabel;
    }

    public BigDecimal getIntentConfidence() {
        return intentConfidence;
    }

    public void setIntentConfidence(BigDecimal intentConfidence) {
        this.intentConfidence = intentConfidence;
    }

    public LocalDateTime getIntentEvaluatedAt() {
        return intentEvaluatedAt;
    }

    public void setIntentEvaluatedAt(LocalDateTime intentEvaluatedAt) {
        this.intentEvaluatedAt = intentEvaluatedAt;
    }

    public Integer getLeadCount() {
        return leadCount;
    }

    public void setLeadCount(Integer leadCount) {
        this.leadCount = leadCount;
    }

    public Integer getOrderCount() {
        return orderCount;
    }

    public void setOrderCount(Integer orderCount) {
        this.orderCount = orderCount;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
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
}
