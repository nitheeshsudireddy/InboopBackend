package com.inboop.backend.lead.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LeadTypeTest {

    @Test
    void testLeadTypeValues() {
        LeadType[] values = LeadType.values();
        assertEquals(7, values.length);
    }

    @Test
    void testInquiryType() {
        assertEquals("INQUIRY", LeadType.INQUIRY.name());
        assertEquals(0, LeadType.INQUIRY.ordinal());
    }

    @Test
    void testOrderRequestType() {
        assertEquals("ORDER_REQUEST", LeadType.ORDER_REQUEST.name());
        assertEquals(1, LeadType.ORDER_REQUEST.ordinal());
    }

    @Test
    void testSupportType() {
        assertEquals("SUPPORT", LeadType.SUPPORT.name());
        assertEquals(2, LeadType.SUPPORT.ordinal());
    }

    @Test
    void testComplaintType() {
        assertEquals("COMPLAINT", LeadType.COMPLAINT.name());
        assertEquals(3, LeadType.COMPLAINT.ordinal());
    }

    @Test
    void testFeedbackType() {
        assertEquals("FEEDBACK", LeadType.FEEDBACK.name());
        assertEquals(4, LeadType.FEEDBACK.ordinal());
    }

    @Test
    void testQuoteRequestType() {
        assertEquals("QUOTE_REQUEST", LeadType.QUOTE_REQUEST.name());
        assertEquals(5, LeadType.QUOTE_REQUEST.ordinal());
    }

    @Test
    void testOtherType() {
        assertEquals("OTHER", LeadType.OTHER.name());
        assertEquals(6, LeadType.OTHER.ordinal());
    }

    @Test
    void testValueOf() {
        assertEquals(LeadType.INQUIRY, LeadType.valueOf("INQUIRY"));
        assertEquals(LeadType.ORDER_REQUEST, LeadType.valueOf("ORDER_REQUEST"));
        assertEquals(LeadType.SUPPORT, LeadType.valueOf("SUPPORT"));
        assertEquals(LeadType.COMPLAINT, LeadType.valueOf("COMPLAINT"));
        assertEquals(LeadType.FEEDBACK, LeadType.valueOf("FEEDBACK"));
        assertEquals(LeadType.QUOTE_REQUEST, LeadType.valueOf("QUOTE_REQUEST"));
        assertEquals(LeadType.OTHER, LeadType.valueOf("OTHER"));
    }

    @Test
    void testValueOfInvalid() {
        assertThrows(IllegalArgumentException.class, () -> LeadType.valueOf("INVALID"));
        assertThrows(IllegalArgumentException.class, () -> LeadType.valueOf("inquiry"));
    }
}
