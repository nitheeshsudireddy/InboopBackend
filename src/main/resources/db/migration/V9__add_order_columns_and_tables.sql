-- Add missing columns to orders table and create related tables
-- Per inboop-order-lifecycle-v1.md

-- Payment tracking (separate from fulfillment status)
ALTER TABLE orders ADD COLUMN IF NOT EXISTS payment_status VARCHAR(50) DEFAULT 'UNPAID';
ALTER TABLE orders ADD COLUMN IF NOT EXISTS payment_method VARCHAR(50);
ALTER TABLE orders ADD COLUMN IF NOT EXISTS paid_at TIMESTAMP;

-- Direct conversation link
ALTER TABLE orders ADD COLUMN IF NOT EXISTS conversation_id BIGINT REFERENCES conversations(id);

-- Contact reference
ALTER TABLE orders ADD COLUMN IF NOT EXISTS contact_id BIGINT REFERENCES contacts(id);

-- Channel denormalized from conversation
ALTER TABLE orders ADD COLUMN IF NOT EXISTS channel VARCHAR(50);

-- Currency support
ALTER TABLE orders ADD COLUMN IF NOT EXISTS currency VARCHAR(10) DEFAULT 'INR';

-- External system reference
ALTER TABLE orders ADD COLUMN IF NOT EXISTS external_order_id VARCHAR(255);

-- Team assignment
ALTER TABLE orders ADD COLUMN IF NOT EXISTS assigned_to_user_id BIGINT REFERENCES users(id);

-- Cancelled timestamp
ALTER TABLE orders ADD COLUMN IF NOT EXISTS cancelled_at TIMESTAMP;

-- Indexes for new columns
CREATE INDEX IF NOT EXISTS idx_orders_payment_status ON orders(payment_status);
CREATE INDEX IF NOT EXISTS idx_orders_conversation_id ON orders(conversation_id);
CREATE INDEX IF NOT EXISTS idx_orders_contact_id ON orders(contact_id);
CREATE INDEX IF NOT EXISTS idx_orders_channel ON orders(channel);
CREATE INDEX IF NOT EXISTS idx_orders_external_order_id ON orders(external_order_id);
CREATE INDEX IF NOT EXISTS idx_orders_assigned_to ON orders(assigned_to_user_id);
CREATE INDEX IF NOT EXISTS idx_orders_status_payment ON orders(status, payment_status);

-- Order items table (line items)
CREATE TABLE IF NOT EXISTS order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    quantity INTEGER NOT NULL DEFAULT 1,
    unit_price DECIMAL(10,2) NOT NULL,
    total_price DECIMAL(10,2) NOT NULL,
    sku VARCHAR(100),
    metadata JSONB,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_order_items_order_id ON order_items(order_id);

-- Order timeline table (status history)
CREATE TABLE IF NOT EXISTS order_timeline (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    status VARCHAR(50) NOT NULL,
    payment_status VARCHAR(50),
    note TEXT,
    performed_by_user_id BIGINT REFERENCES users(id),
    created_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_order_timeline_order_id ON order_timeline(order_id);
