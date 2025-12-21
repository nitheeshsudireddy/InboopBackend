-- Create contacts table
-- Per inboop-domain-model-v1.md and inboop-contact-journey-v1.md

CREATE TABLE IF NOT EXISTS contacts (
    id BIGSERIAL PRIMARY KEY,
    business_id BIGINT NOT NULL REFERENCES businesses(id),

    -- Identity
    name VARCHAR(255),
    email VARCHAR(255),
    phone VARCHAR(50),

    -- Channel handles stored as JSONB for flexibility
    -- Example: {"instagram": "username", "whatsapp": "+1234567890"}
    handles JSONB,

    -- Engagement tracking
    first_seen_at TIMESTAMP,
    last_seen_at TIMESTAMP,

    -- Derived metrics (denormalized for performance)
    total_orders INTEGER DEFAULT 0,
    total_revenue DECIMAL(12,2) DEFAULT 0,

    -- Soft delete / archive
    archived_at TIMESTAMP,
    deleted_at TIMESTAMP,

    -- Timestamps
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

-- Indexes for contact lookups
CREATE INDEX IF NOT EXISTS idx_contacts_business_id ON contacts(business_id);
CREATE INDEX IF NOT EXISTS idx_contacts_email ON contacts(email);
CREATE INDEX IF NOT EXISTS idx_contacts_phone ON contacts(phone);
CREATE INDEX IF NOT EXISTS idx_contacts_business_archived ON contacts(business_id, archived_at);

-- GIN index for JSONB handles field (for searching by channel handle)
CREATE INDEX IF NOT EXISTS idx_contacts_handles ON contacts USING GIN (handles);
