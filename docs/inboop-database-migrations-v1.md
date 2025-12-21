# Inboop — Database Schema & Migration Strategy (V1)

> This document defines how Inboop's database schema evolves over time.
> It is a design and engineering specification, not an implementation guide.
> Existing schemas must be aligned incrementally, never rewritten.

---

## Core Principle

The database evolves additively toward the target design.

Existing tables and data are preserved.
Design documents define the target state, not an immediate rewrite.

---

## Source of Truth

- Postgres is the system of record
- Flyway SQL migrations are the ONLY mechanism for schema changes
- Design docs define intent; migrations implement incrementally

---

## Migration Rules (Authoritative)

1. SQL-only Flyway migrations
2. Never edit an applied migration
3. One logical change per migration
4. Additive by default
5. No table drops or column renames without explicit approval
6. Add columns as nullable first
7. Backfills occur in separate migrations or jobs

Violations of these rules are not allowed.

---

## Migration File Conventions

Recommended naming pattern:

V001__initial_schema.sql
V002__add_archived_at_to_conversations.sql
V003__create_contacts_table.sql
V004__add_contact_id_to_conversations.sql

Rules:
- One intent per file
- Descriptive names
- Ordered by creation time

---

## Target Schema Alignment (High-Level)

### Conversations
Target columns (additive if missing):
- contact_id
- assigned_to_user_id
- intent_label
- intent_confidence
- intent_evaluated_at
- first_message_at
- lead_count
- order_count
- archived_at
- deleted_at

---

### Messages
Target columns:
- direction
- sender_type
- content_type
- channel_message_id
- sent_at
- metadata (JSONB)

---

### Contacts
- Introduced as a new table
- Referenced optionally by Conversations and Orders
- No impact on existing workflows

---

### Leads
Target columns:
- status (NEW / CONVERTED / CLOSED / LOST)
- source
- created_by
- assigned_to_user_id
- archived_at
- deleted_at

Status transitions are enforced in service logic, not DB triggers.

---

### Orders
Target columns:
- payment_status
- payment_method
- external_order_id
- channel (denormalized)
- assigned_to_user_id
- archived_at
- deleted_at

Order status and payment status must remain independent.

---

## Enums vs Lookup Tables

Use Postgres ENUMs for:
- Lead status
- Order status
- Payment status
- Intent label

Rules:
- Enums are append-only
- Never rename or remove enum values

---

## Indexing Strategy

Indexes should support:
- Inbox queries
- Assignment filters
- Archive exclusion
- Status filtering

Examples:
- (workspace_id, archived_at)
- (assigned_to_user_id, archived_at)
- (status, archived_at)
- (conversation_id)

Indexes are added incrementally.

---

## Foreign Keys & Constraints

- Use FK constraints for identity, not workflow enforcement
- Nullable FKs allowed initially
- Cascading deletes only where explicitly defined in the retention spec

---

## Backfill Strategy

- Backfills are optional
- Performed separately from schema changes
- Never bundled with structural migrations

Example:
- Add contact_id column → later backfill best-effort

---

## Rollback Philosophy

- No automatic rollbacks in production
- Fix mistakes with forward migrations
- Rollbacks only for local/dev environments

---

## How Claude Must Work With DB Changes

Every DB-related task must follow this order:

1. Inspect existing schema
2. Compare against design docs
3. Propose minimal additive migrations
4. Wait for approval
5. Generate Flyway SQL migration files

Claude must never:
- Rewrite tables
- Edit existing migrations
- Assume a clean database

---

## Design Intent

- Preserve existing backend work
- Avoid production risk
- Enable gradual convergence to the designed model
- Keep migrations readable and auditable
