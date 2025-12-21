package com.inboop.backend.lead.entity;

import com.inboop.backend.lead.enums.ContentType;
import com.inboop.backend.lead.enums.MessageDirection;
import com.inboop.backend.lead.enums.MessageSentiment;
import com.inboop.backend.lead.enums.SenderType;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "messages")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    @Column(name = "instagram_message_id", unique = true)
    private String instagramMessageId;

    @Column(name = "sender_id", nullable = false)
    private String senderId;

    @Column(name = "is_from_customer", nullable = false)
    private Boolean isFromCustomer;

    // Direction enum (INBOUND/OUTBOUND)
    @Enumerated(EnumType.STRING)
    @Column(name = "direction")
    private MessageDirection direction;

    // Sender type (CUSTOMER/BUSINESS/SYSTEM)
    @Enumerated(EnumType.STRING)
    @Column(name = "sender_type")
    private SenderType senderType;

    // Content type (TEXT/IMAGE/VIDEO/etc.)
    @Enumerated(EnumType.STRING)
    @Column(name = "content_type")
    private ContentType contentType;

    // Generalized channel message ID
    @Column(name = "channel_message_id")
    private String channelMessageId;

    @Column(name = "original_text", columnDefinition = "TEXT")
    private String originalText;

    @Column(name = "translated_text", columnDefinition = "TEXT")
    private String translatedText;

    @Column(name = "detected_language")
    private String detectedLanguage;

    @Enumerated(EnumType.STRING)
    private MessageSentiment sentiment;

    @Column(name = "ai_classification", columnDefinition = "TEXT")
    private String aiClassification;

    @Column(name = "has_attachment")
    private Boolean hasAttachment = false;

    @Column(name = "attachment_type")
    private String attachmentType;

    @Column(name = "attachment_url", length = 1000)
    private String attachmentUrl;

    @Column(name = "is_read")
    private Boolean isRead = false;

    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;

    // Extensibility
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (sentAt == null) {
            sentAt = LocalDateTime.now();
        }
        // Sync direction and senderType from isFromCustomer if not set
        if (direction == null && isFromCustomer != null) {
            direction = isFromCustomer ? MessageDirection.INBOUND : MessageDirection.OUTBOUND;
        }
        if (senderType == null && isFromCustomer != null) {
            senderType = isFromCustomer ? SenderType.CUSTOMER : SenderType.BUSINESS;
        }
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

    public Conversation getConversation() {
        return conversation;
    }

    public void setConversation(Conversation conversation) {
        this.conversation = conversation;
    }

    public String getInstagramMessageId() {
        return instagramMessageId;
    }

    public void setInstagramMessageId(String instagramMessageId) {
        this.instagramMessageId = instagramMessageId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public Boolean getIsFromCustomer() {
        return isFromCustomer;
    }

    public void setIsFromCustomer(Boolean isFromCustomer) {
        this.isFromCustomer = isFromCustomer;
    }

    public String getOriginalText() {
        return originalText;
    }

    public void setOriginalText(String originalText) {
        this.originalText = originalText;
    }

    public String getTranslatedText() {
        return translatedText;
    }

    public void setTranslatedText(String translatedText) {
        this.translatedText = translatedText;
    }

    public String getDetectedLanguage() {
        return detectedLanguage;
    }

    public void setDetectedLanguage(String detectedLanguage) {
        this.detectedLanguage = detectedLanguage;
    }

    public MessageSentiment getSentiment() {
        return sentiment;
    }

    public void setSentiment(MessageSentiment sentiment) {
        this.sentiment = sentiment;
    }

    public String getAiClassification() {
        return aiClassification;
    }

    public void setAiClassification(String aiClassification) {
        this.aiClassification = aiClassification;
    }

    public Boolean getHasAttachment() {
        return hasAttachment;
    }

    public void setHasAttachment(Boolean hasAttachment) {
        this.hasAttachment = hasAttachment;
    }

    public String getAttachmentType() {
        return attachmentType;
    }

    public void setAttachmentType(String attachmentType) {
        this.attachmentType = attachmentType;
    }

    public String getAttachmentUrl() {
        return attachmentUrl;
    }

    public void setAttachmentUrl(String attachmentUrl) {
        this.attachmentUrl = attachmentUrl;
    }

    public Boolean getIsRead() {
        return isRead;
    }

    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
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

    public MessageDirection getDirection() {
        return direction;
    }

    public void setDirection(MessageDirection direction) {
        this.direction = direction;
    }

    public SenderType getSenderType() {
        return senderType;
    }

    public void setSenderType(SenderType senderType) {
        this.senderType = senderType;
    }

    public ContentType getContentType() {
        return contentType;
    }

    public void setContentType(ContentType contentType) {
        this.contentType = contentType;
    }

    public String getChannelMessageId() {
        return channelMessageId;
    }

    public void setChannelMessageId(String channelMessageId) {
        this.channelMessageId = channelMessageId;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    /**
     * Check if this message is from customer (inbound).
     */
    public boolean isInbound() {
        return direction == MessageDirection.INBOUND ||
               (isFromCustomer != null && isFromCustomer);
    }

    /**
     * Check if this message is from business (outbound).
     */
    public boolean isOutbound() {
        return direction == MessageDirection.OUTBOUND ||
               (isFromCustomer != null && !isFromCustomer);
    }
}
