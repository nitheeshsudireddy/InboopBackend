-- Add missing columns to messages table
-- Per inboop-domain-model-v1.md

-- Direction enum (INBOUND/OUTBOUND) - complements is_from_customer
ALTER TABLE messages ADD COLUMN IF NOT EXISTS direction VARCHAR(50);

-- Sender type enum (CUSTOMER/BUSINESS/SYSTEM)
ALTER TABLE messages ADD COLUMN IF NOT EXISTS sender_type VARCHAR(50);

-- Content type enum (TEXT/IMAGE/VIDEO/etc.)
ALTER TABLE messages ADD COLUMN IF NOT EXISTS content_type VARCHAR(50);

-- Generalized channel message ID (in addition to instagram_message_id)
ALTER TABLE messages ADD COLUMN IF NOT EXISTS channel_message_id VARCHAR(255);

-- Extensibility
ALTER TABLE messages ADD COLUMN IF NOT EXISTS metadata JSONB;

-- Updated timestamp
ALTER TABLE messages ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP;

-- Indexes
CREATE INDEX IF NOT EXISTS idx_messages_direction ON messages(direction);
CREATE INDEX IF NOT EXISTS idx_messages_sender_type ON messages(sender_type);
CREATE INDEX IF NOT EXISTS idx_messages_content_type ON messages(content_type);
CREATE INDEX IF NOT EXISTS idx_messages_channel_message_id ON messages(channel_message_id);
CREATE INDEX IF NOT EXISTS idx_messages_sent_at ON messages(sent_at);

-- Backfill direction from is_from_customer for existing data
UPDATE messages SET direction = 'INBOUND' WHERE is_from_customer = true AND direction IS NULL;
UPDATE messages SET direction = 'OUTBOUND' WHERE is_from_customer = false AND direction IS NULL;

-- Backfill sender_type from is_from_customer for existing data
UPDATE messages SET sender_type = 'CUSTOMER' WHERE is_from_customer = true AND sender_type IS NULL;
UPDATE messages SET sender_type = 'BUSINESS' WHERE is_from_customer = false AND sender_type IS NULL;
