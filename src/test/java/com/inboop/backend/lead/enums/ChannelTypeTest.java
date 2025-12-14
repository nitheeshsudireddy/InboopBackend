package com.inboop.backend.lead.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ChannelTypeTest {

    @Test
    void testChannelTypeValues() {
        ChannelType[] values = ChannelType.values();
        assertEquals(3, values.length);
    }

    @Test
    void testInstagramChannel() {
        ChannelType channel = ChannelType.INSTAGRAM;
        assertEquals("INSTAGRAM", channel.name());
        assertEquals(0, channel.ordinal());
    }

    @Test
    void testWhatsAppChannel() {
        ChannelType channel = ChannelType.WHATSAPP;
        assertEquals("WHATSAPP", channel.name());
        assertEquals(1, channel.ordinal());
    }

    @Test
    void testMessengerChannel() {
        ChannelType channel = ChannelType.MESSENGER;
        assertEquals("MESSENGER", channel.name());
        assertEquals(2, channel.ordinal());
    }

    @Test
    void testValueOf() {
        assertEquals(ChannelType.INSTAGRAM, ChannelType.valueOf("INSTAGRAM"));
        assertEquals(ChannelType.WHATSAPP, ChannelType.valueOf("WHATSAPP"));
        assertEquals(ChannelType.MESSENGER, ChannelType.valueOf("MESSENGER"));
    }

    @Test
    void testValueOfInvalid() {
        assertThrows(IllegalArgumentException.class, () -> ChannelType.valueOf("INVALID"));
        assertThrows(IllegalArgumentException.class, () -> ChannelType.valueOf("instagram"));
    }
}
