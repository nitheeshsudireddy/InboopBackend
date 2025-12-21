# Inboop — Gap Analysis: Current Implementation vs Target Design (V1)

> This document compares the current backend implementation against the target design specs.
> It is a READ-ONLY analysis. No code changes, migrations, or refactoring are proposed here.
> Date: 2025-12-21

---

## 1. Summary (High-Level)

The current backend implementation provides a solid foundation but diverges from the target design in several areas:

| Area | Status |
|------|--------|
| Core Entities | Partially aligned — Lead, Order, Conversation, Message exist but lack target fields |
| Contact Entity | **Missing** — Not implemented; no contact aggregation layer |
| Tenant Model | Partially aligned — `Business` serves as Workspace equivalent; naming differs |
| Enums | Divergent — LeadStatus/OrderStatus have different values than spec |
| Archiving/Soft Delete | **Missing** — No `archivedAt` or `deletedAt` fields on entities |
| Intent System | **Missing** — No intent fields on Conversation; LeadType used differently |
| Payment Tracking | **Missing** — No `paymentStatus` or `paymentMethod` on Order |
| Audit Fields | Partial — `createdAt`/`updatedAt` exist; `createdBy`/`assignedTo` varies |

---

## 2. Already Aligned

### 2.1 Entity Structure
- **Conversation** exists with proper relationship to Business and Messages
- **Lead** exists with relationship to Conversation and Business
- **Order** exists with relationship to Lead and Business
- **Message** exists with relationship to Conversation
- **User** and **Business** entities exist for authentication and tenancy

### 2.2 Channel Support
- `ChannelType` enum correctly includes: `INSTAGRAM`, `WHATSAPP`, `MESSENGER`
- Matches target design exactly

### 2.3 Timestamp Management
- All entities use `@PrePersist` and `@PreUpdate` for automatic timestamp management
- `createdAt` and `updatedAt` fields present on core entities

### 2.4 Infrastructure
- Spring Boot modular monolith architecture in place
- Flyway migration system configured (though not heavily used yet)
- PostgreSQL support for production
- Instagram webhook integration started

### 2.5 Conversation Fields (Existing)
- `instagramConversationId` / `channelConversationId` — channel thread ID
- `customerUsername`, `customerName` — customer identifiers
- `channel` — ChannelType enum
- `lastMessageAt` — for sorting

### 2.6 Order Fields (Existing)
- `totalAmount` — order total
- `status` — OrderStatus enum (though values differ from spec)
- `notes`, `shippingAddress`, `trackingNumber` — operational fields
- Relationship to `Lead` and `Business`

---

## 3. Gaps (Additive Only)

These gaps can be addressed with **additive migrations only** (no destructive changes).

### 3.1 Contact Entity — NOT IMPLEMENTED

**Target Design** (from `inboop-domain-model-v1.md`):
```
Contact
├── name
├── email (optional)
├── phone (optional)
├── handles[] (channel-specific identifiers)
├── firstSeenAt
├── lastSeenAt
├── totalOrders
├── totalRevenue
├── archived_at, deleted_at
```

**Current State**: No Contact entity exists.

**Gap**: Contact aggregation layer is missing. Conversations, Leads, and Orders cannot be linked to a unified customer profile.

---

### 3.2 Conversation — Missing Target Columns

| Target Column | Current State | Notes |
|---------------|---------------|-------|
| `contact_id` | Missing | FK to Contact (when Contact exists) |
| `assigned_to_user_id` | Missing | Team assignment |
| `intent_label` | Missing | AI-classified intent |
| `intent_confidence` | Missing | Classification confidence score |
| `intent_evaluated_at` | Missing | When intent was last evaluated |
| `first_message_at` | Missing | First message timestamp |
| `lead_count` | Missing | Denormalized count |
| `order_count` | Missing | Denormalized count |
| `archived_at` | Missing | Soft archive timestamp |
| `deleted_at` | Missing | Soft delete timestamp |
| `metadata` | Missing | JSONB for extensibility |

**Current columns that map differently**:
- `instagramConversationId` → could be generalized to `channel_conversation_id`
- Has `isUnread` which is useful but not in target spec

---

### 3.3 Lead — Status Enum Mismatch + Missing Columns

**Target LeadStatus** (from `inboop-lead-lifecycle-v1.md`):
```
NEW → CONVERTED / LOST / CLOSED (terminal states only)
```

**Current LeadStatus**:
```java
NEW, CONTACTED, QUALIFIED, NEGOTIATING, CONVERTED, LOST, SPAM
```

**Gap Analysis**:
| Current Value | Target Equivalent | Action Needed |
|---------------|-------------------|---------------|
| NEW | NEW | Aligned |
| CONTACTED | — | Not in target; consider deprecating |
| QUALIFIED | — | Not in target; consider deprecating |
| NEGOTIATING | — | Not in target; consider deprecating |
| CONVERTED | CONVERTED | Aligned |
| LOST | LOST | Aligned |
| SPAM | — | Not in target; may map to conversation intent |
| — | CLOSED | Missing; needs to be added |

**Missing Lead Columns**:
| Target Column | Current State |
|---------------|---------------|
| `source` | Missing (AI / MANUAL) |
| `created_by` | Missing (User FK) |
| `archived_at` | Missing |
| `deleted_at` | Missing |

**Note**: `LeadType` enum exists (INQUIRY, ORDER_REQUEST, SUPPORT, etc.) but target design puts intent classification on Conversation, not Lead.

---

### 3.4 Order — Missing Target Columns + Status Mismatch

**Target OrderStatus** (from `inboop-order-lifecycle-v1.md`):
```
NEW → CONFIRMED → SHIPPED → DELIVERED
         ↓
     CANCELLED (can branch from any pre-delivered state)
```

**Current OrderStatus**:
```java
PENDING, CONFIRMED, PROCESSING, SHIPPED, DELIVERED, CANCELLED, REFUNDED
```

**Gap Analysis**:
| Current Value | Target Equivalent | Notes |
|---------------|-------------------|-------|
| PENDING | NEW | Rename candidate (or keep as alias) |
| CONFIRMED | CONFIRMED | Aligned |
| PROCESSING | — | Not in target; intermediate state |
| SHIPPED | SHIPPED | Aligned |
| DELIVERED | DELIVERED | Aligned |
| CANCELLED | CANCELLED | Aligned |
| REFUNDED | — | Should be `paymentStatus`, not `orderStatus` |

**Missing Order Columns**:
| Target Column | Current State |
|---------------|---------------|
| `conversation_id` | Missing — Order links to Lead, not directly to Conversation |
| `payment_status` | Missing — Currently conflated with order status |
| `payment_method` | Missing (ONLINE / COD / etc.) |
| `channel` | Missing — Denormalized from Conversation |
| `currency` | Missing |
| `external_order_id` | Missing |
| `items[]` | Missing — No OrderItem entity |
| `timeline[]` | Missing — No OrderTimeline entity |
| `assigned_to_user_id` | Missing |
| `archived_at` | Missing |
| `deleted_at` | Missing |

**Note**: Current Order is linked to Lead (which links to Conversation). Target design suggests direct `conversation_id` on Order as well.

---

### 3.5 Message — Missing Target Columns

| Target Column | Current State | Notes |
|---------------|---------------|-------|
| `direction` | `isFromCustomer` (boolean) | Should be enum: INBOUND/OUTBOUND |
| `sender_type` | Missing | CUSTOMER / BUSINESS / SYSTEM |
| `content_type` | `messageType` (TEXT/IMAGE/etc.) | Exists but may need review |
| `channel_message_id` | `instagramMessageId` | Naming could be generalized |
| `sent_at` | Missing | Distinct from `createdAt` |
| `metadata` | Missing | JSONB for extensibility |

---

### 3.6 User & Workspace — Structural Gaps

**Target Tenant Model**:
```
Organization (billing entity)
└── Workspace (operational unit)
    └── Users (with roles: ADMIN / MEMBER)
```

**Current State**:
- `Business` entity serves as Workspace equivalent (Instagram-focused)
- `User` has simple `role` string field
- No explicit Organization layer
- No formal ADMIN/MEMBER role enum

**Missing**:
- `WorkspaceMembership` (or equivalent) for user-workspace relationship with roles
- `archived_at` / `deleted_at` on User

---

### 3.7 Intent System — Not Implemented

**Target** (from `inboop-ai-intent-system-v1.md`):
- Intent taxonomy: BUYING, SUPPORT, BROWSING, SPAM, OTHER
- Intent stored at Conversation level (`intent_label`, `intent_confidence`, `intent_evaluated_at`)
- Trigger conditions for AI evaluation

**Current State**:
- `LeadType` enum exists but is used for lead classification, not conversation intent
- No intent fields on Conversation
- No AI evaluation trigger logic

---

### 3.8 Archiving & Soft Delete — Not Implemented

**Target** (from `inboop-data-retention-archiving-compliance-v1.md`):
- All major entities should have `archived_at` and `deleted_at`
- Archive hides from default views; delete is compliance-driven
- Auto-archive rules for stale conversations, closed leads, delivered orders

**Current State**:
- No `archived_at` or `deleted_at` on any entity
- No archive/delete distinction in the model

---

### 3.9 Payment Status — Not Separated from Order Status

**Target** (from `inboop-order-lifecycle-v1.md`):
```
Payment Status (independent track):
UNPAID → PAID → REFUNDED

Order Status (fulfillment track):
NEW → CONFIRMED → SHIPPED → DELIVERED
```

**Current State**:
- `REFUNDED` is in `OrderStatus` enum (conflated)
- No separate `PaymentStatus` enum
- No `payment_status` or `payment_method` columns on Order

---

## 4. Deferred or Optional (Safe to Skip for V1)

These items are mentioned in design docs but explicitly marked as future or optional:

| Item | Design Doc Reference | Reason to Defer |
|------|---------------------|-----------------|
| Organization entity | roles-permissions-pricing | V1 can use single-workspace model |
| Saved Views | search-filters-analytics | Explicitly out of scope for V1 |
| Outbound webhooks | integrations-extensibility | Future feature |
| Payment link generation | integrations-extensibility | V1 is tracking-only |
| Analytics dashboards | search-filters-analytics | Metrics derived, no new tables |
| Legal holds | data-retention-archiving | Out of scope for V1 |
| Workflow automation | integrations-extensibility | Non-goal for V1 |
| Multi-provider payment webhooks | integrations-extensibility | Non-goal for V1 |

---

## 5. Do NOT Change

These elements are aligned or should be preserved as-is:

### 5.1 Preserve Existing Entity Relationships
- `Lead` → `Conversation` relationship works
- `Order` → `Lead` relationship works
- `Message` → `Conversation` relationship works
- `Business` as workspace-equivalent is functional

### 5.2 Preserve Existing Columns
- All timestamp columns (`createdAt`, `updatedAt`)
- All identifier columns (`instagramUserId`, `instagramUsername`, etc.)
- All operational columns (`notes`, `shippingAddress`, `trackingNumber`, etc.)
- `ChannelType` enum values (exact match with target)

### 5.3 Preserve Infrastructure
- Flyway migration setup
- Spring Boot modular structure
- Security configuration
- Webhook endpoints

### 5.4 Do Not Rename
Per migration rules:
- Do not rename `Business` to `Workspace` (additive approach: can add workspace abstraction later)
- Do not rename `isFromCustomer` to `direction` (can add new column and deprecate)
- Do not rename `instagramMessageId` (can add generalized column)

### 5.5 Do Not Drop
- No table drops
- No column drops
- No enum value removals (append-only)

---

## Appendix: Entity File Locations

| Entity | File Path |
|--------|-----------|
| Conversation | `src/main/java/com/inboop/backend/lead/entity/Conversation.java` |
| Lead | `src/main/java/com/inboop/backend/lead/entity/Lead.java` |
| Message | `src/main/java/com/inboop/backend/lead/entity/Message.java` |
| Order | `src/main/java/com/inboop/backend/order/entity/Order.java` |
| User | `src/main/java/com/inboop/backend/auth/entity/User.java` |
| Business | `src/main/java/com/inboop/backend/business/entity/Business.java` |
| LeadStatus | `src/main/java/com/inboop/backend/lead/entity/LeadStatus.java` |
| OrderStatus | `src/main/java/com/inboop/backend/order/entity/OrderStatus.java` |
| ChannelType | `src/main/java/com/inboop/backend/shared/enums/ChannelType.java` |
| LeadType | `src/main/java/com/inboop/backend/lead/entity/LeadType.java` |

---

## Appendix: Design Document References

| Document | Key Topics |
|----------|------------|
| `inboop-domain-model-v1.md` | Entity definitions, relationships |
| `inboop-inbox-lifecycle-v1.md` | Inbox behavior, unread logic |
| `inboop-lead-lifecycle-v1.md` | Lead states, transitions |
| `inboop-order-lifecycle-v1.md` | Order states, payment status |
| `inboop-contact-journey-v1.md` | Contact aggregation |
| `inboop-ai-intent-system-v1.md` | Intent taxonomy, triggers |
| `inboop-roles-permissions-pricing-v1.md` | Roles, permissions |
| `inboop-notifications-sla-realtime-v1.md` | SLA tracking |
| `inboop-search-filters-analytics-v1.md` | Search, filters |
| `inboop-data-retention-archiving-compliance-v1.md` | Archive/delete behavior |
| `inboop-integrations-extensibility-v1.md` | Channel integrations |
| `inboop-backend-architecture-v1.md` | Module boundaries |
| `inboop-database-migrations-v1.md` | Migration rules |
