package com.inboop.backend.lead.entity;

import com.inboop.backend.auth.entity.User;
import com.inboop.backend.business.entity.Business;
import com.inboop.backend.lead.enums.ChannelType;
import com.inboop.backend.lead.enums.LeadStatus;
import com.inboop.backend.lead.enums.LeadType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class LeadTest {

    private Lead lead;
    private Business business;
    private User user;

    @BeforeEach
    void setUp() {
        lead = new Lead();
        business = new Business();
        business.setId(1L);
        business.setName("Test Business");

        user = new User();
        user.setId(1L);
        user.setName("Test User");
        user.setEmail("test@example.com");
    }

    @Test
    void testLeadCreation() {
        lead.setId(1L);
        lead.setBusiness(business);
        lead.setInstagramUserId("123456789");
        lead.setInstagramUsername("testuser");
        lead.setCustomerName("Test Customer");

        assertEquals(1L, lead.getId());
        assertEquals(business, lead.getBusiness());
        assertEquals("123456789", lead.getInstagramUserId());
        assertEquals("testuser", lead.getInstagramUsername());
        assertEquals("Test Customer", lead.getCustomerName());
    }

    @Test
    void testLeadDefaultValues() {
        assertEquals(LeadStatus.NEW, lead.getStatus());
        assertEquals(LeadType.OTHER, lead.getType());
        assertEquals(ChannelType.INSTAGRAM, lead.getChannel());
        assertNotNull(lead.getLabels());
        assertTrue(lead.getLabels().isEmpty());
        assertNull(lead.getConversation());
    }

    @Test
    void testLeadStatusChange() {
        lead.setStatus(LeadStatus.CONTACTED);
        assertEquals(LeadStatus.CONTACTED, lead.getStatus());

        lead.setStatus(LeadStatus.QUALIFIED);
        assertEquals(LeadStatus.QUALIFIED, lead.getStatus());

        lead.setStatus(LeadStatus.CONVERTED);
        assertEquals(LeadStatus.CONVERTED, lead.getStatus());
    }

    @Test
    void testLeadTypeChange() {
        lead.setType(LeadType.INQUIRY);
        assertEquals(LeadType.INQUIRY, lead.getType());

        lead.setType(LeadType.ORDER_REQUEST);
        assertEquals(LeadType.ORDER_REQUEST, lead.getType());

        lead.setType(LeadType.SUPPORT);
        assertEquals(LeadType.SUPPORT, lead.getType());
    }

    @Test
    void testChannelType() {
        lead.setChannel(ChannelType.INSTAGRAM);
        assertEquals(ChannelType.INSTAGRAM, lead.getChannel());

        lead.setChannel(ChannelType.WHATSAPP);
        assertEquals(ChannelType.WHATSAPP, lead.getChannel());

        lead.setChannel(ChannelType.MESSENGER);
        assertEquals(ChannelType.MESSENGER, lead.getChannel());
    }

    @Test
    void testLeadValue() {
        assertNull(lead.getValue());

        BigDecimal value = new BigDecimal("1500.50");
        lead.setValue(value);
        assertEquals(value, lead.getValue());

        lead.setValue(BigDecimal.ZERO);
        assertEquals(BigDecimal.ZERO, lead.getValue());
    }

    @Test
    void testLeadNotes() {
        assertNull(lead.getNotes());

        lead.setNotes("Customer interested in bulk order");
        assertEquals("Customer interested in bulk order", lead.getNotes());

        lead.setNotes("");
        assertEquals("", lead.getNotes());
    }

    @Test
    void testProfilePicture() {
        assertNull(lead.getProfilePicture());

        lead.setProfilePicture("https://example.com/profile.jpg");
        assertEquals("https://example.com/profile.jpg", lead.getProfilePicture());
    }

    @Test
    void testLastMessageSnippet() {
        assertNull(lead.getLastMessageSnippet());

        lead.setLastMessageSnippet("Hi, I'm interested in...");
        assertEquals("Hi, I'm interested in...", lead.getLastMessageSnippet());
    }

    @Test
    void testAssignedTo() {
        assertNull(lead.getAssignedTo());

        lead.setAssignedTo(user);
        assertEquals(user, lead.getAssignedTo());
        assertEquals("Test User", lead.getAssignedTo().getName());
    }

    @Test
    void testLabels() {
        Set<String> labels = new HashSet<>();
        labels.add("VIP");
        labels.add("High Priority");

        lead.setLabels(labels);

        assertEquals(2, lead.getLabels().size());
        assertTrue(lead.getLabels().contains("VIP"));
        assertTrue(lead.getLabels().contains("High Priority"));
    }

    @Test
    void testDetectedLanguage() {
        assertNull(lead.getDetectedLanguage());

        lead.setDetectedLanguage("en");
        assertEquals("en", lead.getDetectedLanguage());

        lead.setDetectedLanguage("es");
        assertEquals("es", lead.getDetectedLanguage());
    }

    @Test
    void testLastMessageAt() {
        assertNull(lead.getLastMessageAt());

        LocalDateTime now = LocalDateTime.now();
        lead.setLastMessageAt(now);
        assertEquals(now, lead.getLastMessageAt());
    }

    @Test
    void testTimestamps() {
        LocalDateTime now = LocalDateTime.now();

        lead.setCreatedAt(now);
        lead.setUpdatedAt(now);

        assertEquals(now, lead.getCreatedAt());
        assertEquals(now, lead.getUpdatedAt());
    }

    @Test
    void testConversationAssociation() {
        Conversation conversation = new Conversation();
        conversation.setId(1L);

        lead.setConversation(conversation);

        assertEquals(conversation, lead.getConversation());
        assertEquals(1L, lead.getConversation().getId());
    }
}
