# Inboop — Data Retention, Archiving & Compliance (V1)

> This document defines data retention, archiving, deletion, and compliance behavior in Inboop.
> It is a behavioral specification, not a UI or implementation guide.

---

## Core Principle

Hide when not needed. Delete only when required. Never surprise the user.

- Archiving is the default cleanup mechanism
- Deletion is explicit, intentional, and compliance-driven
- History should remain trustworthy and predictable

---

## Archive vs Delete

### Archive
- Reversible
- Hides items from default views
- Preserves history and analytics

### Delete
- Irreversible (after grace period)
- Used for compliance (e.g., GDPR)
- Removes items from analytics and search

---

## Archiving Behavior (V1 Default)

Archiving is the primary cleanup mechanism.

Entities that can be archived:
- Conversations
- Leads
- Orders
- Contacts

Effects of archiving:
- Archived items do not appear in default lists
- Archived conversations are excluded from unread counts
- Archived conversations are excluded from SLA tracking
- Archived items are included in historical analytics (unless deleted)

Search behavior:
- Archived items are searchable only when "Include archived" is enabled

Archiving is reversible.

---

## Automatic Archiving Rules (V1)

Automatic archiving keeps views clean without deleting data.

### Conversations
Auto-archive if:
- No inbound messages for N days

Default:
- N = 30 days

### Leads
Auto-archive if:
- Lead status is CONVERTED / LOST / CLOSED
- AND no activity for N days

Default:
- N = 30 days

### Orders
Auto-archive if:
- Order status is DELIVERED or CANCELLED
- AND older than N days

Default:
- N = 90 days

Auto-archiving never deletes data and can be reversed by manual unarchive.

---

## Deletion Rules (Compliance-First)

Deletion is explicit and intentional.

Who can delete:
- ADMIN only

Entities that can be deleted:
- Contacts
- Conversations
- Leads
- Orders

Cascade behavior:

Deleting a Contact:
- Deletes all linked Conversations
- Deletes all linked Leads
- Deletes all linked Orders

Deleting a Conversation:
- Deletes its Messages
- Deletes its Leads
- Deletes its Orders

---

## Soft Delete & Grace Period (Recommended)

Deletion flow:
1. Entity is marked with deletedAt
2. Grace period applies (recommended: 7–14 days)
3. Background purge job permanently removes data

During grace period:
- Entity is hidden from UI
- Entity is excluded from analytics and search
- ADMIN can restore

After purge:
- Deletion is permanent

---

## GDPR & Privacy Readiness

Design must support:
- Right to be forgotten
- Full deletion of personal data

Rules:
- Contact deletion removes PII
- Messages are deleted
- Orders may retain anonymized financial data (future option)
- Deletion actions must be auditable

---

## Audit & Traceability

All destructive actions must record:
- action type (ARCHIVE / UNARCHIVE / DELETE / RESTORE / PURGE)
- entity type
- entity id
- performedByUserId
- timestamp

This supports compliance and Enterprise audit logs later.

---

## Visibility & UX Rules (Conceptual)

- Archived items are hidden by default
- "Show archived" toggle exists on list views
- Deleted items never appear in UI
- Strong warnings shown before deletion:
  - "This action cannot be undone"

---

## Non-Goals (V1)

Out of scope:
- Legal holds
- Region-specific retention law workflows
- Automatic silent expiry without user visibility

---

## Design Intent

- Keep the product clean without losing business history
- Ensure compliance readiness without complexity
- Make archiving reversible and deletion deliberate
