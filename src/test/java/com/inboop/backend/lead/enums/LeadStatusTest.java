package com.inboop.backend.lead.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LeadStatusTest {

    @Test
    void testLeadStatusValues() {
        LeadStatus[] values = LeadStatus.values();
        assertEquals(7, values.length);
    }

    @Test
    void testNewStatus() {
        assertEquals("NEW", LeadStatus.NEW.name());
        assertEquals(0, LeadStatus.NEW.ordinal());
    }

    @Test
    void testContactedStatus() {
        assertEquals("CONTACTED", LeadStatus.CONTACTED.name());
        assertEquals(1, LeadStatus.CONTACTED.ordinal());
    }

    @Test
    void testQualifiedStatus() {
        assertEquals("QUALIFIED", LeadStatus.QUALIFIED.name());
        assertEquals(2, LeadStatus.QUALIFIED.ordinal());
    }

    @Test
    void testNegotiatingStatus() {
        assertEquals("NEGOTIATING", LeadStatus.NEGOTIATING.name());
        assertEquals(3, LeadStatus.NEGOTIATING.ordinal());
    }

    @Test
    void testConvertedStatus() {
        assertEquals("CONVERTED", LeadStatus.CONVERTED.name());
        assertEquals(4, LeadStatus.CONVERTED.ordinal());
    }

    @Test
    void testLostStatus() {
        assertEquals("LOST", LeadStatus.LOST.name());
        assertEquals(5, LeadStatus.LOST.ordinal());
    }

    @Test
    void testSpamStatus() {
        assertEquals("SPAM", LeadStatus.SPAM.name());
        assertEquals(6, LeadStatus.SPAM.ordinal());
    }

    @Test
    void testValueOf() {
        assertEquals(LeadStatus.NEW, LeadStatus.valueOf("NEW"));
        assertEquals(LeadStatus.CONTACTED, LeadStatus.valueOf("CONTACTED"));
        assertEquals(LeadStatus.QUALIFIED, LeadStatus.valueOf("QUALIFIED"));
        assertEquals(LeadStatus.NEGOTIATING, LeadStatus.valueOf("NEGOTIATING"));
        assertEquals(LeadStatus.CONVERTED, LeadStatus.valueOf("CONVERTED"));
        assertEquals(LeadStatus.LOST, LeadStatus.valueOf("LOST"));
        assertEquals(LeadStatus.SPAM, LeadStatus.valueOf("SPAM"));
    }

    @Test
    void testValueOfInvalid() {
        assertThrows(IllegalArgumentException.class, () -> LeadStatus.valueOf("INVALID"));
        assertThrows(IllegalArgumentException.class, () -> LeadStatus.valueOf("new"));
    }
}
