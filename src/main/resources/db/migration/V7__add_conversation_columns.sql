-- Add missing columns to conversations table
-- Per inboop-domain-model-v1.md and inboop-ai-intent-system-v1.md

-- Contact reference (optional FK to contacts)
ALTER TABLE conversations ADD COLUMN IF NOT EXISTS contact_id BIGINT REFERENCES contacts(id);

-- Team assignment
ALTER TABLE conversations ADD COLUMN IF NOT EXISTS assigned_to_user_id BIGINT REFERENCES users(id);

-- AI Intent fields
ALTER TABLE conversations ADD COLUMN IF NOT EXISTS intent_label VARCHAR(50);
ALTER TABLE conversations ADD COLUMN IF NOT EXISTS intent_confidence DECIMAL(5,4);
ALTER TABLE conversations ADD COLUMN IF NOT EXISTS intent_evaluated_at TIMESTAMP;

-- Timing
ALTER TABLE conversations ADD COLUMN IF NOT EXISTS first_message_at TIMESTAMP;

-- Denormalized counts
ALTER TABLE conversations ADD COLUMN IF NOT EXISTS lead_count INTEGER DEFAULT 0;
ALTER TABLE conversations ADD COLUMN IF NOT EXISTS order_count INTEGER DEFAULT 0;

-- Extensibility
ALTER TABLE conversations ADD COLUMN IF NOT EXISTS metadata JSONB;

-- Timestamps (if missing from baseline)
ALTER TABLE conversations ADD COLUMN IF NOT EXISTS created_at TIMESTAMP;
ALTER TABLE conversations ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP;

-- Indexes for new columns
CREATE INDEX IF NOT EXISTS idx_conversations_contact_id ON conversations(contact_id);
CREATE INDEX IF NOT EXISTS idx_conversations_assigned_to ON conversations(assigned_to_user_id);
CREATE INDEX IF NOT EXISTS idx_conversations_intent_label ON conversations(intent_label);
CREATE INDEX IF NOT EXISTS idx_conversations_business_intent ON conversations(business_id, intent_label);
