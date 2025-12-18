-- V4__add_business_connection_context.sql
-- Add connection context fields to businesses table for Instagram integration status tracking

-- Add new columns for connection context
ALTER TABLE businesses ADD COLUMN IF NOT EXISTS available_page_ids VARCHAR(2000);
ALTER TABLE businesses ADD COLUMN IF NOT EXISTS selected_page_id VARCHAR(255);
ALTER TABLE businesses ADD COLUMN IF NOT EXISTS last_ig_account_id_seen VARCHAR(255);
ALTER TABLE businesses ADD COLUMN IF NOT EXISTS connection_retry_at TIMESTAMP;
ALTER TABLE businesses ADD COLUMN IF NOT EXISTS last_connection_error VARCHAR(255);
ALTER TABLE businesses ADD COLUMN IF NOT EXISTS last_status_check_at TIMESTAMP;

-- Add comments for documentation
COMMENT ON COLUMN businesses.available_page_ids IS 'Comma-separated list of all Facebook Page IDs user has access to';
COMMENT ON COLUMN businesses.selected_page_id IS 'The Facebook Page ID user selected for this business';
COMMENT ON COLUMN businesses.last_ig_account_id_seen IS 'Last known Instagram Business Account ID (for ownership mismatch detection)';
COMMENT ON COLUMN businesses.connection_retry_at IS 'If in cooldown period, when to retry connection';
COMMENT ON COLUMN businesses.last_connection_error IS 'Last connection error reason code';
COMMENT ON COLUMN businesses.last_status_check_at IS 'Timestamp of last status verification';
