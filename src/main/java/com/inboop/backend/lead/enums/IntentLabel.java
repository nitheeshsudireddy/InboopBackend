package com.inboop.backend.lead.enums;

/**
 * Intent classification labels for conversations.
 * Per inboop-ai-intent-system-v1.md
 */
public enum IntentLabel {
    BUYING,     // Customer shows purchase intent
    SUPPORT,    // Customer needs help or has issues
    BROWSING,   // Customer is exploring, not ready to buy
    SPAM,       // Spam or irrelevant messages
    OTHER       // Uncategorized or mixed intent
}
