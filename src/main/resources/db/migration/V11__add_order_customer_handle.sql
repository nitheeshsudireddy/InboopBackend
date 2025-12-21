-- Add customer_handle to orders for direct customer identification
-- This is denormalized from conversation for query performance

ALTER TABLE orders ADD COLUMN IF NOT EXISTS customer_handle VARCHAR(255);

-- Add carrier column for shipping provider name
ALTER TABLE orders ADD COLUMN IF NOT EXISTS carrier VARCHAR(255);

-- Add tracking URL for external tracking links
ALTER TABLE orders ADD COLUMN IF NOT EXISTS tracking_url VARCHAR(500);

-- Add shipping address columns (structured instead of single text field)
ALTER TABLE orders ADD COLUMN IF NOT EXISTS shipping_address_line1 VARCHAR(255);
ALTER TABLE orders ADD COLUMN IF NOT EXISTS shipping_address_line2 VARCHAR(255);
ALTER TABLE orders ADD COLUMN IF NOT EXISTS shipping_city VARCHAR(100);
ALTER TABLE orders ADD COLUMN IF NOT EXISTS shipping_state VARCHAR(100);
ALTER TABLE orders ADD COLUMN IF NOT EXISTS shipping_postal_code VARCHAR(20);
ALTER TABLE orders ADD COLUMN IF NOT EXISTS shipping_country VARCHAR(100);

-- Index for customer handle search
CREATE INDEX IF NOT EXISTS idx_orders_customer_handle ON orders(customer_handle);
CREATE INDEX IF NOT EXISTS idx_orders_customer_name ON orders(customer_name);
CREATE INDEX IF NOT EXISTS idx_orders_tracking_number ON orders(tracking_number);
