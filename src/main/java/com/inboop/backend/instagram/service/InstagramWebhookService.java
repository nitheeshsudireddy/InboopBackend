package com.inboop.backend.instagram.service;

import com.inboop.backend.business.entity.Business;
import com.inboop.backend.business.repository.BusinessRepository;
import com.inboop.backend.instagram.dto.WebhookPayload;
import com.inboop.backend.lead.entity.Conversation;
import com.inboop.backend.lead.entity.Lead;
import com.inboop.backend.lead.entity.Message;
import com.inboop.backend.lead.enums.ChannelType;
import com.inboop.backend.lead.enums.LeadStatus;
import com.inboop.backend.lead.enums.LeadType;
import com.inboop.backend.lead.repository.ConversationRepository;
import com.inboop.backend.lead.repository.LeadRepository;
import com.inboop.backend.lead.repository.MessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

@Service
public class InstagramWebhookService {

    private static final Logger logger = LoggerFactory.getLogger(InstagramWebhookService.class);

    private final BusinessRepository businessRepository;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final LeadRepository leadRepository;

    public InstagramWebhookService(
            BusinessRepository businessRepository,
            ConversationRepository conversationRepository,
            MessageRepository messageRepository,
            LeadRepository leadRepository) {
        this.businessRepository = businessRepository;
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.leadRepository = leadRepository;
    }

    /**
     * Process incoming Instagram webhook payload.
     * This method handles the entire flow:
     * 1. Identify the business account receiving the message
     * 2. Find or create a conversation with the customer
     * 3. Create the message record
     * 4. Find or create a lead for the customer
     */
    @Transactional
    public void processWebhook(WebhookPayload payload) {
        if (!"instagram".equals(payload.getObject())) {
            logger.debug("Ignoring non-Instagram webhook: {}", payload.getObject());
            return;
        }

        if (payload.getEntry() == null) {
            logger.warn("Webhook payload has no entries");
            return;
        }

        for (WebhookPayload.Entry entry : payload.getEntry()) {
            processEntry(entry);
        }
    }

    private void processEntry(WebhookPayload.Entry entry) {
        if (entry.getMessaging() == null || entry.getMessaging().isEmpty()) {
            logger.debug("Entry has no messaging events");
            return;
        }

        // The entry ID is the Instagram Page ID (IGSID) of the business
        String recipientPageId = entry.getId();

        for (WebhookPayload.Messaging messaging : entry.getMessaging()) {
            try {
                processMessagingEvent(recipientPageId, messaging);
            } catch (Exception e) {
                logger.error("Error processing messaging event", e);
            }
        }
    }

    private void processMessagingEvent(String pageId, WebhookPayload.Messaging messaging) {
        String senderId = messaging.getSender().getId();
        String recipientId = messaging.getRecipient().getId();
        WebhookPayload.MessageData messageData = messaging.getMessage();

        if (messageData == null) {
            logger.debug("Messaging event has no message data (possibly a read receipt or delivery)");
            return;
        }

        // Check if message already exists (idempotency)
        String instagramMessageId = messageData.getMid();
        if (instagramMessageId != null && messageRepository.findByInstagramMessageId(instagramMessageId).isPresent()) {
            logger.debug("Message {} already exists, skipping", instagramMessageId);
            return;
        }

        // Determine if message is from customer or from business
        // If sender ID equals the page ID, it's an outgoing message from the business
        boolean isFromCustomer = !senderId.equals(pageId);
        String customerId = isFromCustomer ? senderId : recipientId;
        String businessPageId = isFromCustomer ? recipientId : senderId;

        // Find the business by Instagram Page ID
        // Note: findByInstagramPageId returns List because multiple businesses could share a page
        // but in practice we take the first one
        List<Business> businesses = businessRepository.findByInstagramPageId(businessPageId);
        Optional<Business> businessOpt = businesses.isEmpty() ? Optional.empty() : Optional.of(businesses.get(0));

        if (businessOpt.isEmpty()) {
            logger.warn("No business found for Instagram Page ID: {}", businessPageId);
            return;
        }

        Business business = businessOpt.get();

        // Find or create conversation
        Conversation conversation = findOrCreateConversation(business, customerId);

        // Create message
        Message message = createMessage(conversation, messaging, isFromCustomer);

        // Update conversation with last message info
        updateConversationLastMessage(conversation, message, isFromCustomer);

        // Find or create lead if message is from customer
        if (isFromCustomer) {
            findOrCreateLead(business, conversation, customerId, message);
        }

        logger.info("Processed message {} from {} to business {}",
                instagramMessageId, customerId, business.getInstagramUsername());
    }

    private Conversation findOrCreateConversation(Business business, String customerId) {
        return conversationRepository
                .findByBusinessIdAndInstagramScopedUserId(business.getId(), customerId)
                .orElseGet(() -> {
                    Conversation newConversation = new Conversation();
                    newConversation.setBusiness(business);
                    newConversation.setInstagramScopedUserId(customerId);
                    newConversation.setChannel(ChannelType.INSTAGRAM);
                    newConversation.setCustomerHandle(customerId); // Will be updated with username later via API
                    newConversation.setUnreadCount(0);
                    newConversation.setIsActive(true);
                    return conversationRepository.save(newConversation);
                });
    }

    private Message createMessage(Conversation conversation, WebhookPayload.Messaging messaging, boolean isFromCustomer) {
        WebhookPayload.MessageData messageData = messaging.getMessage();

        Message message = new Message();
        message.setConversation(conversation);
        message.setInstagramMessageId(messageData.getMid());
        message.setSenderId(messaging.getSender().getId());
        message.setIsFromCustomer(isFromCustomer);
        message.setOriginalText(messageData.getText());
        message.setIsRead(!isFromCustomer); // Outgoing messages are "read" by default

        // Handle timestamp
        if (messaging.getTimestamp() != null) {
            LocalDateTime sentAt = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(messaging.getTimestamp()),
                    ZoneId.systemDefault()
            );
            message.setSentAt(sentAt);
        } else {
            message.setSentAt(LocalDateTime.now());
        }

        // Handle attachments
        if (messageData.getAttachments() != null && !messageData.getAttachments().isEmpty()) {
            WebhookPayload.Attachment attachment = messageData.getAttachments().get(0);
            message.setHasAttachment(true);
            message.setAttachmentType(attachment.getType());
            if (attachment.getPayload() != null) {
                message.setAttachmentUrl(attachment.getPayload().getUrl());
            }
        }

        return messageRepository.save(message);
    }

    private void updateConversationLastMessage(Conversation conversation, Message message, boolean isFromCustomer) {
        String snippet = message.getOriginalText();
        if (snippet == null && message.getHasAttachment()) {
            snippet = "[" + (message.getAttachmentType() != null ? message.getAttachmentType() : "attachment") + "]";
        }

        conversation.setLastMessage(snippet != null && snippet.length() > 100
                ? snippet.substring(0, 100) + "..."
                : snippet);
        conversation.setLastMessageAt(message.getSentAt());

        if (isFromCustomer) {
            conversation.setUnreadCount(conversation.getUnreadCount() + 1);
        }

        conversationRepository.save(conversation);
    }

    private void findOrCreateLead(Business business, Conversation conversation, String customerId, Message message) {
        // Check if lead already exists for this customer in this business
        Optional<Lead> existingLead = leadRepository
                .findByBusinessIdAndInstagramUserId(business.getId(), customerId);

        if (existingLead.isPresent()) {
            // Update existing lead with latest message info
            Lead lead = existingLead.get();
            lead.setLastMessageSnippet(message.getOriginalText());
            lead.setLastMessageAt(message.getSentAt());
            leadRepository.save(lead);
        } else {
            // Create new lead
            Lead lead = new Lead();
            lead.setBusiness(business);
            lead.setConversation(conversation);
            lead.setInstagramUserId(customerId);
            lead.setChannel(ChannelType.INSTAGRAM);
            lead.setStatus(LeadStatus.NEW);
            lead.setType(LeadType.OTHER); // Will be classified by AI later
            lead.setLastMessageSnippet(message.getOriginalText());
            lead.setLastMessageAt(message.getSentAt());
            leadRepository.save(lead);

            logger.info("Created new lead for customer {} in business {}",
                    customerId, business.getInstagramUsername());
        }
    }
}
