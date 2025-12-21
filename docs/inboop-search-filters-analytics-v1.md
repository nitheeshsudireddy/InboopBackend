# Inboop — Search, Filters & Analytics Foundations (V1)

> This document defines search, filtering, and analytics foundations for Inboop.
> It is a behavioral specification, not a UI or implementation guide.

---

## Core Principle

Everything should be searchable.
Filters should match the user mental model.
Metrics are derived from existing events and states.

---

## Scoped Search vs Global Search

V1 uses scoped search per surface:
- Inbox search → Conversations and Messages
- Leads search → Leads
- Orders search → Orders
- Contacts search → Contacts

There is no global "search everything" bar in V1.

---

## Inbox Search & Filters

Searchable fields:
- Customer name
- Handle / username
- Message content (inbound and outbound)
- Order ID references in messages (if present)

Filters:
- Channel (Instagram / WhatsApp / Facebook)
- Intent (BUYING / SUPPORT / BROWSING / SPAM / OTHER)
- Assigned to (Me / Unassigned / Any)
- Unread only
- Over SLA only
- Has NEW Lead (yes/no)
- Has Orders (yes/no)

Default sort:
- lastMessageAt descending

---

## Leads Search & Filters

Searchable fields:
- Customer name
- Conversation reference
- Lead notes
- Lead ID (if exposed)

Filters:
- Status (NEW / CONVERTED / CLOSED / LOST)
- Assigned to
- Source (AI / MANUAL)
- Created date range
- Has Orders (yes/no)

Default sort:
- createdAt descending

---

## Orders Search & Filters

Searchable fields:
- Order ID
- Customer name / handle
- Item names
- Tracking ID (if present)
- External order ID (future)

Filters:
- Order status
- Payment status (UNPAID / PAID / REFUNDED)
- Payment method (ONLINE / COD if enabled)
- Channel
- Assigned to
- Date range (created / delivered)

Default sort:
- createdAt descending

---

## Contacts Search & Filters

Searchable fields:
- Name
- Email
- Phone
- Handles

Filters:
- Has Orders
- Last interaction date (future-friendly)

---

## Saved Views (Not V1)

Saved filters/views are out of scope for V1.

Design constraint:
- Filter models should be serializable (e.g., query params)
- This enables Saved Views later without redesign.

---

## Analytics Foundations (Derived Metrics)

Analytics should be derived from:
- Messages
- Leads
- Orders
- Timelines
- Timestamps

Core derived metrics (no dashboards required in V1):

Inbox / Ops:
- Unread conversations
- Average first response time
- SLA breach rate

Leads:
- Leads created
- Leads converted
- Conversion rate
- Time to conversion

Orders:
- Orders created
- Revenue (sum of PAID orders)
- Average order value
- Fulfillment time
- Refund rate

Contacts:
- Repeat customers
- Lifetime value
- Orders per contact

No metric should require a new table solely for counting.

---

## Time & Attribution Rules

- All timestamps are stored in UTC
- User timezone affects display only

Attribution rules:
- Lead conversion time = Lead.createdAt → first linked Order.createdAt
- Fulfillment time = time from CONFIRMED → DELIVERED
- SLA timing = first inbound → first outbound reply

These rules must remain consistent across the product.

---

## Performance & Indexing (Design Constraints)

Even in V1:
- Searchable fields should be indexable
- Avoid full-table scans in design
- Prefer denormalized fields already defined (e.g., customerName and channel on Order)
