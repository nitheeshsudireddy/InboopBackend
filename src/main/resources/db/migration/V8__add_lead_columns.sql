-- Add missing columns to leads table
-- Per inboop-lead-lifecycle-v1.md

-- Source tracking (AI or MANUAL)
ALTER TABLE leads ADD COLUMN IF NOT EXISTS source VARCHAR(50);

-- Who created this lead
ALTER TABLE leads ADD COLUMN IF NOT EXISTS created_by BIGINT REFERENCES users(id);

-- Contact reference (optional FK to contacts)
ALTER TABLE leads ADD COLUMN IF NOT EXISTS contact_id BIGINT REFERENCES contacts(id);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_leads_source ON leads(source);
CREATE INDEX IF NOT EXISTS idx_leads_created_by ON leads(created_by);
CREATE INDEX IF NOT EXISTS idx_leads_contact_id ON leads(contact_id);
CREATE INDEX IF NOT EXISTS idx_leads_status_archived ON leads(status, archived_at);
