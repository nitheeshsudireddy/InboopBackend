package com.inboop.backend.lead.entity;

import com.inboop.backend.business.entity.Business;
import com.inboop.backend.lead.enums.ChannelType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ConversationTest {

    private Conversation conversation;
    private Lead lead;
    private Business business;

    @BeforeEach
    void setUp() {
        conversation = new Conversation();
        lead = new Lead();
        lead.setId(1L);
        lead.setInstagramUserId("123456789");

        business = new Business();
        business.setId(1L);
        business.setName("Test Business");
    }

    @Test
    void testConversationCreation() {
        conversation.setId(1L);
        conversation.setBusiness(business);
        conversation.setInstagramConversationId("conv_123");

        assertEquals(1L, conversation.getId());
        assertEquals(business, conversation.getBusiness());
        assertEquals("conv_123", conversation.getInstagramConversationId());
    }

    @Test
    void testConversationDefaultValues() {
        assertEquals(ChannelType.INSTAGRAM, conversation.getChannel());
        assertTrue(conversation.getIsActive());
        assertEquals(0, conversation.getUnreadCount());
        assertNotNull(conversation.getMessages());
        assertTrue(conversation.getMessages().isEmpty());
        assertNotNull(conversation.getLeads());
        assertTrue(conversation.getLeads().isEmpty());
    }

    @Test
    void testChannelType() {
        conversation.setChannel(ChannelType.INSTAGRAM);
        assertEquals(ChannelType.INSTAGRAM, conversation.getChannel());

        conversation.setChannel(ChannelType.WHATSAPP);
        assertEquals(ChannelType.WHATSAPP, conversation.getChannel());

        conversation.setChannel(ChannelType.MESSENGER);
        assertEquals(ChannelType.MESSENGER, conversation.getChannel());
    }

    @Test
    void testCustomerHandle() {
        assertNull(conversation.getCustomerHandle());

        conversation.setCustomerHandle("@testuser");
        assertEquals("@testuser", conversation.getCustomerHandle());

        // WhatsApp phone number
        conversation.setCustomerHandle("+1234567890");
        assertEquals("+1234567890", conversation.getCustomerHandle());
    }

    @Test
    void testCustomerName() {
        assertNull(conversation.getCustomerName());

        conversation.setCustomerName("John Doe");
        assertEquals("John Doe", conversation.getCustomerName());
    }

    @Test
    void testProfilePicture() {
        assertNull(conversation.getProfilePicture());

        conversation.setProfilePicture("https://example.com/avatar.jpg");
        assertEquals("https://example.com/avatar.jpg", conversation.getProfilePicture());
    }

    @Test
    void testLastMessage() {
        assertNull(conversation.getLastMessage());

        conversation.setLastMessage("Hello, I need help with my order");
        assertEquals("Hello, I need help with my order", conversation.getLastMessage());
    }

    @Test
    void testUnreadCount() {
        assertEquals(0, conversation.getUnreadCount());

        conversation.setUnreadCount(5);
        assertEquals(5, conversation.getUnreadCount());

        conversation.setUnreadCount(0);
        assertEquals(0, conversation.getUnreadCount());
    }

    @Test
    void testIsActive() {
        assertTrue(conversation.getIsActive());

        conversation.setIsActive(false);
        assertFalse(conversation.getIsActive());

        conversation.setIsActive(true);
        assertTrue(conversation.getIsActive());
    }

    @Test
    void testTimestamps() {
        LocalDateTime now = LocalDateTime.now();

        conversation.setStartedAt(now);
        conversation.setLastMessageAt(now);

        assertEquals(now, conversation.getStartedAt());
        assertEquals(now, conversation.getLastMessageAt());
    }

    @Test
    void testMessagesAssociation() {
        Message message1 = new Message();
        message1.setId(1L);
        message1.setConversation(conversation);
        message1.setOriginalText("Hello");

        Message message2 = new Message();
        message2.setId(2L);
        message2.setConversation(conversation);
        message2.setOriginalText("Hi there!");

        conversation.getMessages().add(message1);
        conversation.getMessages().add(message2);

        assertEquals(2, conversation.getMessages().size());
    }

    @Test
    void testLeadsAssociation() {
        Lead lead1 = new Lead();
        lead1.setId(1L);
        lead1.setConversation(conversation);

        Lead lead2 = new Lead();
        lead2.setId(2L);
        lead2.setConversation(conversation);

        conversation.getLeads().add(lead1);
        conversation.getLeads().add(lead2);

        assertEquals(2, conversation.getLeads().size());
    }

    @Test
    void testBusinessAssociation() {
        conversation.setBusiness(business);

        assertEquals(business, conversation.getBusiness());
        assertEquals(1L, conversation.getBusiness().getId());
        assertEquals("Test Business", conversation.getBusiness().getName());
    }

    @Test
    void testInstagramConversationId() {
        assertNull(conversation.getInstagramConversationId());

        conversation.setInstagramConversationId("ig_conv_12345");
        assertEquals("ig_conv_12345", conversation.getInstagramConversationId());
    }
}
