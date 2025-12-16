-- V3: Add attachment_type to messages and instagram_scoped_user_id to conversations
-- This migration adds fields needed for proper webhook message processing

-- Add attachment_type column to messages table
-- Stores the type of attachment: image, video, audio, sticker, file, etc.
ALTER TABLE messages ADD COLUMN IF NOT EXISTS attachment_type VARCHAR(50);

-- Add instagram_scoped_user_id to conversations
-- This is the Instagram-scoped user ID for the customer (different from username)
ALTER TABLE conversations ADD COLUMN IF NOT EXISTS instagram_scoped_user_id VARCHAR(255);

-- Create index for faster lookup by Instagram scoped user ID
CREATE INDEX IF NOT EXISTS idx_conversations_instagram_scoped_user_id
    ON conversations(instagram_scoped_user_id);

-- Add index on business_id + instagram_scoped_user_id for finding existing conversations
CREATE INDEX IF NOT EXISTS idx_conversations_business_customer
    ON conversations(business_id, instagram_scoped_user_id);
