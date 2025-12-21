# Inboop — Core Domain Model (V1)

> This document defines the authoritative core domain model for Inboop.
> All frontend, backend, AI, and UX decisions MUST align with this document.
> If something conflicts, this document wins.

---

## 1. Tenant Model

- Organization → Workspace → Users
- All data is scoped to `workspaceId`
- Workspace exists even if V1 supports only a single workspace

---

## 2. Contact (Customer Profile)

Represents a real person (customer), independent of channel.

**Fields**
- contactId
- workspaceId
- name
- email (nullable)
- phone (nullable)
- notes
- lifetimeValue (derived later)
- createdAt, updatedAt
- archivedAt, deletedAt
- metadata (JSON)

**Relationships**
- Contact 1 → * Conversations
- Contact 1 → * Orders

---

## 3. Conversation (Inbox Thread)

Represents one DM thread in one channel.

**Fields**
- conversationId
- workspaceId
- channel (INSTAGRAM | WHATSAPP | FACEBOOK)
- channelThreadId
- contactId (nullable)
- intentState
- assignedToUserId (optional)
- tags[]
- firstMessageAt
- lastMessageAt
- leadCount (denormalized)
- orderCount (denormalized)
- archivedAt, deletedAt
- metadata (JSON)

**Relationships**
- Conversation 1 → * Messages
- Conversation 1 → * Leads
- Conversation 1 → * Orders

---

## 4. Message

Represents an individual DM.

**Fields**
- messageId
- conversationId
- channelMessageId
- direction (INBOUND | OUTBOUND)
- senderType (CUSTOMER | USER | SYSTEM | AI)
- content
- contentType (TEXT | IMAGE | VIDEO | VOICE | FILE)
- sentAt
- deliveredAt (nullable)
- readAt (nullable)
- metadata (JSON)

---

## 5. Lead (Opportunity Snapshot)

Represents a time-bounded buying opportunity.

**Fields**
- leadId
- workspaceId
- conversationId
- contactId (optional)
- status (OPEN | WON | LOST | CLOSED)
- source (AI | MANUAL)
- createdBy (AI | USER_ID)
- createdAt, closedAt
- notes
- valueEstimate
- archivedAt, deletedAt
- metadata (JSON)

**Rules**
- A conversation can have multiple leads over time
- At most ONE OPEN lead per conversation
- Creating a new lead auto-closes existing OPEN lead as CLOSED
- Creating an Order with leadId auto-marks that lead as WON

---

## 6. Order (Transaction)

Represents a transaction.

**Fields**
- orderId (human-readable, e.g. ORD-1001)
- workspaceId
- conversationId
- contactId
- leadId (nullable)
- channel (denormalized)
- customerName / customerHandle
- currency
- externalOrderId (nullable)
- status (NEW | PENDING | CONFIRMED | SHIPPED | DELIVERED | CANCELLED)
- paymentStatus (PAID | UNPAID | COD | REFUNDED)
- items[]
- totals
- shippingAddress, tracking
- timeline[]
- createdAt, updatedAt
- archivedAt, deletedAt
- metadata (JSON)

---

## 7. AI Intent Classification

Intent exists at CONVERSATION level.

**IntentState**
- label (BUYING | SUPPORT | BROWSING | SPAM | OTHER)
- confidence (0–1)
- lastEvaluatedAt
- reasonShort

**Triggers**
- First inbound message
- New inbound after idle threshold
- New inbound after lead closed
- Manual re-evaluate (future)

**Idle threshold**
- Configurable per workspace
- Default: 24h

---

## 8. Product / Catalog (Optional, Defined)

- productId
- workspaceId
- sku
- name
- price
- currency
- active
- metadata

Orders may reference products later but can remain freeform in V1.

---

## Design Principles

- Conversation = relationship timeline
- Lead = opportunity snapshot
- Order = transaction record
- Intent is stateful and event-driven
- Denormalization is allowed for UX speed
- Avoid premature backend rigidity
