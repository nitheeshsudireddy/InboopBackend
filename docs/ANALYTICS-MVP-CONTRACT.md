# Analytics MVP Contract

> Version: 1.0
> Date: 2025-12-22
> Status: Draft

This document defines the V1 analytics metrics for Inboop. All metrics are derived from existing tables without new data structures.

---

## Date Attribution Rules

All metrics use **event timestamp** for date attribution:

| Metric Type | Attribution Field | Table |
|-------------|-------------------|-------|
| Orders created | `orders.created_at` | orders |
| Orders delivered | `orders.delivered_at` | orders |
| Orders cancelled | `orders.cancelled_at` | orders |
| Revenue (paid) | `orders.paid_at` | orders |
| Leads created | `leads.created_at` | leads |
| Leads converted | `leads.converted_at` | leads |
| Conversations started | `conversations.started_at` | conversations |
| Messages received | `messages.sent_at` | messages |

**Time zone**: All timestamps are stored as `LocalDateTime` (server timezone). Aggregations should use consistent timezone handling.

---

## Global Exclusions

The following records are **excluded from all metrics** unless explicitly stated:

| Exclusion | Condition | Rationale |
|-----------|-----------|-----------|
| Deleted orders | `orders.deleted_at IS NOT NULL` | Soft-deleted records |
| Deleted leads | `leads.deleted_at IS NOT NULL` | Soft-deleted records |
| Deleted conversations | `conversations.deleted_at IS NOT NULL` | Soft-deleted records |

**Note**: Archived records (`archived_at IS NOT NULL`) are **included** in metrics.

---

## Orders & Revenue Metrics

### ORD-01: Orders Created

**Definition**: Count of orders created within the period.

| Attribute | Value |
|-----------|-------|
| Source Table | `orders` |
| Filter | `deleted_at IS NULL` |
| Date Field | `created_at` |
| Aggregation | `COUNT(*)` |

**Formula**:
```sql
SELECT COUNT(*)
FROM orders
WHERE deleted_at IS NULL
  AND business_id = :businessId
  AND created_at >= :periodStart
  AND created_at < :periodEnd
```

---

### ORD-02: Orders Delivered

**Definition**: Count of orders that reached DELIVERED status within the period.

| Attribute | Value |
|-----------|-------|
| Source Table | `orders` |
| Filter | `status = 'DELIVERED'`, `deleted_at IS NULL` |
| Date Field | `delivered_at` |
| Aggregation | `COUNT(*)` |

**Formula**:
```sql
SELECT COUNT(*)
FROM orders
WHERE deleted_at IS NULL
  AND status = 'DELIVERED'
  AND business_id = :businessId
  AND delivered_at >= :periodStart
  AND delivered_at < :periodEnd
```

---

### ORD-03: Orders Cancelled

**Definition**: Count of orders that reached CANCELLED status within the period.

| Attribute | Value |
|-----------|-------|
| Source Table | `orders` |
| Filter | `status = 'CANCELLED'`, `deleted_at IS NULL` |
| Date Field | `cancelled_at` |
| Aggregation | `COUNT(*)` |

**Formula**:
```sql
SELECT COUNT(*)
FROM orders
WHERE deleted_at IS NULL
  AND status = 'CANCELLED'
  AND business_id = :businessId
  AND cancelled_at >= :periodStart
  AND cancelled_at < :periodEnd
```

---

### ORD-04: Cancellation Rate

**Definition**: Percentage of orders cancelled out of orders that reached a terminal state (DELIVERED or CANCELLED) within the period.

| Attribute | Value |
|-----------|-------|
| Source Tables | `orders` |
| Numerator | Orders cancelled (ORD-03) |
| Denominator | Orders delivered (ORD-02) + Orders cancelled (ORD-03) |
| Unit | Percentage (0-100) |

**Formula**:
```
Cancellation Rate = (ORD-03 / (ORD-02 + ORD-03)) * 100
```

**Edge case**: If denominator is 0, return `NULL` (not calculable).

---

### REV-01: Gross Revenue

**Definition**: Sum of `total_amount` for all orders with `payment_status = 'PAID'`, attributed to the payment date.

| Attribute | Value |
|-----------|-------|
| Source Table | `orders` |
| Filter | `payment_status = 'PAID'`, `deleted_at IS NULL` |
| Date Field | `paid_at` |
| Amount Field | `total_amount` |
| Currency Field | `currency` |
| Aggregation | `SUM(total_amount)` |

**Formula**:
```sql
SELECT COALESCE(SUM(total_amount), 0)
FROM orders
WHERE deleted_at IS NULL
  AND payment_status = 'PAID'
  AND business_id = :businessId
  AND paid_at >= :periodStart
  AND paid_at < :periodEnd
```

**Note**: Revenue is **not** split by currency in V1. Assumes single-currency business. Multi-currency support deferred.

---

### REV-02: Refunded Amount

**Definition**: Sum of `total_amount` for orders where `payment_status = 'REFUNDED'`.

| Attribute | Value |
|-----------|-------|
| Source Table | `orders` |
| Filter | `payment_status = 'REFUNDED'`, `deleted_at IS NULL` |
| Date Field | `updated_at` (approximation - no refunded_at field) |
| Amount Field | `total_amount` |
| Aggregation | `SUM(total_amount)` |

**Formula**:
```sql
SELECT COALESCE(SUM(total_amount), 0)
FROM orders
WHERE deleted_at IS NULL
  AND payment_status = 'REFUNDED'
  AND business_id = :businessId
  AND updated_at >= :periodStart
  AND updated_at < :periodEnd
```

**Limitation**: Uses `updated_at` as proxy for refund date. Accurate refund date attribution requires adding `refunded_at` field (deferred).

---

### REV-03: Net Revenue

**Definition**: Gross revenue minus refunded amount for the period.

| Attribute | Value |
|-----------|-------|
| Formula | `REV-01 - REV-02` |
| Unit | Currency amount |

---

### REV-04: Average Order Value (AOV)

**Definition**: Average `total_amount` per paid order within the period.

| Attribute | Value |
|-----------|-------|
| Source Table | `orders` |
| Filter | `payment_status = 'PAID'`, `deleted_at IS NULL` |
| Date Field | `paid_at` |
| Aggregation | `AVG(total_amount)` |

**Formula**:
```sql
SELECT AVG(total_amount)
FROM orders
WHERE deleted_at IS NULL
  AND payment_status = 'PAID'
  AND business_id = :businessId
  AND paid_at >= :periodStart
  AND paid_at < :periodEnd
```

**Edge case**: If no paid orders, return `NULL`.

---

### ORD-05: Orders by Channel

**Definition**: Count of orders created, grouped by channel (INSTAGRAM, WHATSAPP, MESSENGER).

| Attribute | Value |
|-----------|-------|
| Source Table | `orders` |
| Filter | `deleted_at IS NULL` |
| Date Field | `created_at` |
| Group By | `channel` |
| Aggregation | `COUNT(*)` |

**Formula**:
```sql
SELECT channel, COUNT(*) as order_count
FROM orders
WHERE deleted_at IS NULL
  AND business_id = :businessId
  AND created_at >= :periodStart
  AND created_at < :periodEnd
GROUP BY channel
```

---

### ORD-06: Orders by Status

**Definition**: Count of orders grouped by current status (snapshot, not time-bounded).

| Attribute | Value |
|-----------|-------|
| Source Table | `orders` |
| Filter | `deleted_at IS NULL` |
| Date Field | N/A (current snapshot) |
| Group By | `status` |
| Aggregation | `COUNT(*)` |

**Formula**:
```sql
SELECT status, COUNT(*) as order_count
FROM orders
WHERE deleted_at IS NULL
  AND business_id = :businessId
GROUP BY status
```

---

## Leads & Conversion Metrics

### LEAD-01: Leads Created

**Definition**: Count of leads created within the period.

| Attribute | Value |
|-----------|-------|
| Source Table | `leads` |
| Filter | `deleted_at IS NULL` |
| Date Field | `created_at` |
| Aggregation | `COUNT(*)` |

**Formula**:
```sql
SELECT COUNT(*)
FROM leads
WHERE deleted_at IS NULL
  AND business_id = :businessId
  AND created_at >= :periodStart
  AND created_at < :periodEnd
```

---

### LEAD-02: Leads Converted

**Definition**: Count of leads that reached CONVERTED status within the period.

| Attribute | Value |
|-----------|-------|
| Source Table | `leads` |
| Filter | `status = 'CONVERTED'`, `deleted_at IS NULL` |
| Date Field | `converted_at` |
| Aggregation | `COUNT(*)` |

**Formula**:
```sql
SELECT COUNT(*)
FROM leads
WHERE deleted_at IS NULL
  AND status = 'CONVERTED'
  AND business_id = :businessId
  AND converted_at >= :periodStart
  AND converted_at < :periodEnd
```

---

### LEAD-03: Leads Lost

**Definition**: Count of leads that reached LOST status within the period.

| Attribute | Value |
|-----------|-------|
| Source Table | `leads` |
| Filter | `status = 'LOST'`, `deleted_at IS NULL` |
| Date Field | `updated_at` (no lost_at field) |
| Aggregation | `COUNT(*)` |

**Formula**:
```sql
SELECT COUNT(*)
FROM leads
WHERE deleted_at IS NULL
  AND status = 'LOST'
  AND business_id = :businessId
  AND updated_at >= :periodStart
  AND updated_at < :periodEnd
```

**Limitation**: Uses `updated_at` as proxy. Accurate attribution requires adding `lost_at` field (deferred).

---

### LEAD-04: Leads Closed

**Definition**: Count of leads that reached CLOSED status within the period.

| Attribute | Value |
|-----------|-------|
| Source Table | `leads` |
| Filter | `status = 'CLOSED'`, `deleted_at IS NULL` |
| Date Field | `updated_at` (no closed_at field) |
| Aggregation | `COUNT(*)` |

**Formula**:
```sql
SELECT COUNT(*)
FROM leads
WHERE deleted_at IS NULL
  AND status = 'CLOSED'
  AND business_id = :businessId
  AND updated_at >= :periodStart
  AND updated_at < :periodEnd
```

**Limitation**: Uses `updated_at` as proxy. Accurate attribution requires adding `closed_at` field (deferred).

---

### LEAD-05: Conversion Rate

**Definition**: Percentage of leads converted out of leads that reached a terminal state (CONVERTED, CLOSED, or LOST) within the period.

| Attribute | Value |
|-----------|-------|
| Numerator | Leads converted (LEAD-02) |
| Denominator | Leads converted + Leads closed + Leads lost |
| Unit | Percentage (0-100) |

**Formula**:
```
Conversion Rate = (LEAD-02 / (LEAD-02 + LEAD-03 + LEAD-04)) * 100
```

**Edge case**: If denominator is 0, return `NULL`.

---

### LEAD-06: Open Leads

**Definition**: Count of leads currently in NEW status (snapshot metric).

| Attribute | Value |
|-----------|-------|
| Source Table | `leads` |
| Filter | `status = 'NEW'`, `deleted_at IS NULL` |
| Date Field | N/A (current snapshot) |
| Aggregation | `COUNT(*)` |

**Formula**:
```sql
SELECT COUNT(*)
FROM leads
WHERE deleted_at IS NULL
  AND status = 'NEW'
  AND business_id = :businessId
```

---

### LEAD-07: Leads by Channel

**Definition**: Count of leads created, grouped by channel.

| Attribute | Value |
|-----------|-------|
| Source Table | `leads` |
| Filter | `deleted_at IS NULL` |
| Date Field | `created_at` |
| Group By | `channel` |
| Aggregation | `COUNT(*)` |

**Formula**:
```sql
SELECT channel, COUNT(*) as lead_count
FROM leads
WHERE deleted_at IS NULL
  AND business_id = :businessId
  AND created_at >= :periodStart
  AND created_at < :periodEnd
GROUP BY channel
```

---

### LEAD-08: Leads by Type

**Definition**: Count of leads, grouped by type (INQUIRY, ORDER_REQUEST, SUPPORT, COMPLAINT, OTHER).

| Attribute | Value |
|-----------|-------|
| Source Table | `leads` |
| Filter | `deleted_at IS NULL` |
| Date Field | `created_at` |
| Group By | `type` |
| Aggregation | `COUNT(*)` |

**Formula**:
```sql
SELECT type, COUNT(*) as lead_count
FROM leads
WHERE deleted_at IS NULL
  AND business_id = :businessId
  AND created_at >= :periodStart
  AND created_at < :periodEnd
GROUP BY type
```

---

## Inbox & SLA Metrics

### INBOX-01: Active Conversations

**Definition**: Count of conversations with `is_active = true` (snapshot metric).

| Attribute | Value |
|-----------|-------|
| Source Table | `conversations` |
| Filter | `is_active = true`, `deleted_at IS NULL` |
| Date Field | N/A (current snapshot) |
| Aggregation | `COUNT(*)` |

**Formula**:
```sql
SELECT COUNT(*)
FROM conversations
WHERE deleted_at IS NULL
  AND is_active = true
  AND business_id = :businessId
```

---

### INBOX-02: Total Unread Messages

**Definition**: Sum of `unread_count` across all active conversations (snapshot metric).

| Attribute | Value |
|-----------|-------|
| Source Table | `conversations` |
| Filter | `is_active = true`, `deleted_at IS NULL` |
| Date Field | N/A (current snapshot) |
| Aggregation | `SUM(unread_count)` |

**Formula**:
```sql
SELECT COALESCE(SUM(unread_count), 0)
FROM conversations
WHERE deleted_at IS NULL
  AND is_active = true
  AND business_id = :businessId
```

---

### INBOX-03: Conversations Started

**Definition**: Count of conversations started within the period.

| Attribute | Value |
|-----------|-------|
| Source Table | `conversations` |
| Filter | `deleted_at IS NULL` |
| Date Field | `started_at` |
| Aggregation | `COUNT(*)` |

**Formula**:
```sql
SELECT COUNT(*)
FROM conversations
WHERE deleted_at IS NULL
  AND business_id = :businessId
  AND started_at >= :periodStart
  AND started_at < :periodEnd
```

---

### INBOX-04: Inbound Messages

**Definition**: Count of messages received from customers within the period.

| Attribute | Value |
|-----------|-------|
| Source Table | `messages` |
| Join | `conversations` (for business_id) |
| Filter | `direction = 'INBOUND'` OR `is_from_customer = true` |
| Date Field | `sent_at` |
| Aggregation | `COUNT(*)` |

**Formula**:
```sql
SELECT COUNT(*)
FROM messages m
JOIN conversations c ON m.conversation_id = c.id
WHERE c.deleted_at IS NULL
  AND c.business_id = :businessId
  AND (m.direction = 'INBOUND' OR m.is_from_customer = true)
  AND m.sent_at >= :periodStart
  AND m.sent_at < :periodEnd
```

---

### INBOX-05: Outbound Messages

**Definition**: Count of messages sent by the business within the period.

| Attribute | Value |
|-----------|-------|
| Source Table | `messages` |
| Join | `conversations` (for business_id) |
| Filter | `direction = 'OUTBOUND'` OR `is_from_customer = false` |
| Date Field | `sent_at` |
| Aggregation | `COUNT(*)` |

**Formula**:
```sql
SELECT COUNT(*)
FROM messages m
JOIN conversations c ON m.conversation_id = c.id
WHERE c.deleted_at IS NULL
  AND c.business_id = :businessId
  AND (m.direction = 'OUTBOUND' OR m.is_from_customer = false)
  AND m.sent_at >= :periodStart
  AND m.sent_at < :periodEnd
```

---

### INBOX-06: Conversations by Channel

**Definition**: Count of conversations started, grouped by channel.

| Attribute | Value |
|-----------|-------|
| Source Table | `conversations` |
| Filter | `deleted_at IS NULL` |
| Date Field | `started_at` |
| Group By | `channel` |
| Aggregation | `COUNT(*)` |

**Formula**:
```sql
SELECT channel, COUNT(*) as conversation_count
FROM conversations
WHERE deleted_at IS NULL
  AND business_id = :businessId
  AND started_at >= :periodStart
  AND started_at < :periodEnd
GROUP BY channel
```

---

### SLA-01: Average First Response Time

**Definition**: Average time between first inbound message and first outbound message per conversation, for conversations started within the period.

| Attribute | Value |
|-----------|-------|
| Source Tables | `conversations`, `messages` |
| Date Field | `conversations.started_at` |
| Unit | Minutes |

**Formula**:
```sql
SELECT AVG(EXTRACT(EPOCH FROM (first_outbound.sent_at - first_inbound.sent_at)) / 60) as avg_minutes
FROM conversations c
JOIN (
  SELECT conversation_id, MIN(sent_at) as sent_at
  FROM messages
  WHERE (direction = 'INBOUND' OR is_from_customer = true)
  GROUP BY conversation_id
) first_inbound ON c.id = first_inbound.conversation_id
JOIN (
  SELECT conversation_id, MIN(sent_at) as sent_at
  FROM messages
  WHERE (direction = 'OUTBOUND' OR is_from_customer = false)
  GROUP BY conversation_id
) first_outbound ON c.id = first_outbound.conversation_id
WHERE c.deleted_at IS NULL
  AND c.business_id = :businessId
  AND c.started_at >= :periodStart
  AND c.started_at < :periodEnd
  AND first_outbound.sent_at > first_inbound.sent_at
```

**Exclusions**: Conversations without outbound messages are excluded from calculation.

---

### SLA-02: Conversations Without Response

**Definition**: Count of active conversations with at least one unread inbound message but no outbound message.

| Attribute | Value |
|-----------|-------|
| Source Tables | `conversations`, `messages` |
| Filter | `is_active = true`, `unread_count > 0` |
| Date Field | N/A (current snapshot) |

**Formula**:
```sql
SELECT COUNT(DISTINCT c.id)
FROM conversations c
WHERE c.deleted_at IS NULL
  AND c.is_active = true
  AND c.unread_count > 0
  AND c.business_id = :businessId
  AND NOT EXISTS (
    SELECT 1 FROM messages m
    WHERE m.conversation_id = c.id
    AND (m.direction = 'OUTBOUND' OR m.is_from_customer = false)
  )
```

---

## Explicit Exclusions (V1 Scope)

The following are **explicitly out of scope** for V1:

| Excluded | Reason |
|----------|--------|
| Multi-currency revenue splits | Assumes single-currency business |
| Cohort analysis | Requires complex date range handling |
| Funnel metrics (e.g., stage duration) | Requires timeline tracking |
| User/agent-level metrics | Deferred for team analytics |
| Real-time metrics (< 1 minute) | Polling-based only in V1 |
| Percentile calculations (P50, P95) | Simple averages in V1 |
| Year-over-year comparisons | Simple period-based only |
| Custom date ranges beyond 90 days | Performance considerations |

---

## Summary: All V1 Metrics

| ID | Metric Name | Type | Table(s) |
|----|-------------|------|----------|
| ORD-01 | Orders Created | Period | orders |
| ORD-02 | Orders Delivered | Period | orders |
| ORD-03 | Orders Cancelled | Period | orders |
| ORD-04 | Cancellation Rate | Derived | orders |
| ORD-05 | Orders by Channel | Period | orders |
| ORD-06 | Orders by Status | Snapshot | orders |
| REV-01 | Gross Revenue | Period | orders |
| REV-02 | Refunded Amount | Period | orders |
| REV-03 | Net Revenue | Derived | orders |
| REV-04 | Average Order Value | Period | orders |
| LEAD-01 | Leads Created | Period | leads |
| LEAD-02 | Leads Converted | Period | leads |
| LEAD-03 | Leads Lost | Period | leads |
| LEAD-04 | Leads Closed | Period | leads |
| LEAD-05 | Conversion Rate | Derived | leads |
| LEAD-06 | Open Leads | Snapshot | leads |
| LEAD-07 | Leads by Channel | Period | leads |
| LEAD-08 | Leads by Type | Period | leads |
| INBOX-01 | Active Conversations | Snapshot | conversations |
| INBOX-02 | Total Unread Messages | Snapshot | conversations |
| INBOX-03 | Conversations Started | Period | conversations |
| INBOX-04 | Inbound Messages | Period | messages |
| INBOX-05 | Outbound Messages | Period | messages |
| INBOX-06 | Conversations by Channel | Period | conversations |
| SLA-01 | Avg First Response Time | Period | messages |
| SLA-02 | Conversations Without Response | Snapshot | conversations, messages |
