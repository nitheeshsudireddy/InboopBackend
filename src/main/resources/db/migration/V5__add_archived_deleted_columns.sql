-- Add archived_at and deleted_at columns to core entities
-- Per inboop-data-retention-archiving-compliance-v1.md

-- Conversations: Add archiving and soft delete support
ALTER TABLE conversations ADD COLUMN IF NOT EXISTS archived_at TIMESTAMP;
ALTER TABLE conversations ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;

-- Leads: Add archiving and soft delete support
ALTER TABLE leads ADD COLUMN IF NOT EXISTS archived_at TIMESTAMP;
ALTER TABLE leads ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;

-- Orders: Add archiving and soft delete support
ALTER TABLE orders ADD COLUMN IF NOT EXISTS archived_at TIMESTAMP;
ALTER TABLE orders ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;

-- Users: Add archiving and soft delete support
ALTER TABLE users ADD COLUMN IF NOT EXISTS archived_at TIMESTAMP;
ALTER TABLE users ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;

-- Businesses: Add archiving and soft delete support
ALTER TABLE businesses ADD COLUMN IF NOT EXISTS archived_at TIMESTAMP;
ALTER TABLE businesses ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;

-- Indexes for efficient archive/delete filtering
CREATE INDEX IF NOT EXISTS idx_conversations_archived_at ON conversations(archived_at);
CREATE INDEX IF NOT EXISTS idx_conversations_deleted_at ON conversations(deleted_at);
CREATE INDEX IF NOT EXISTS idx_leads_archived_at ON leads(archived_at);
CREATE INDEX IF NOT EXISTS idx_leads_deleted_at ON leads(deleted_at);
CREATE INDEX IF NOT EXISTS idx_orders_archived_at ON orders(archived_at);
CREATE INDEX IF NOT EXISTS idx_orders_deleted_at ON orders(deleted_at);

-- Composite indexes for common queries (workspace + archive status)
CREATE INDEX IF NOT EXISTS idx_conversations_business_archived ON conversations(business_id, archived_at);
CREATE INDEX IF NOT EXISTS idx_leads_business_archived ON leads(business_id, archived_at);
CREATE INDEX IF NOT EXISTS idx_orders_business_archived ON orders(business_id, archived_at);
