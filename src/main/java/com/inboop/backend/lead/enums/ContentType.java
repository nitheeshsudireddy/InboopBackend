package com.inboop.backend.lead.enums;

/**
 * Content type enum for messages.
 * Per inboop-domain-model-v1.md
 */
public enum ContentType {
    TEXT,      // Plain text message
    IMAGE,     // Image attachment
    VIDEO,     // Video attachment
    AUDIO,     // Audio/voice message
    FILE,      // File attachment
    STICKER,   // Sticker
    LOCATION,  // Location share
    CONTACT,   // Contact card
    STORY_MENTION,  // Instagram story mention
    STORY_REPLY,    // Reply to Instagram story
    REEL_SHARE,     // Instagram reel share
    POST_SHARE      // Instagram post share
}
