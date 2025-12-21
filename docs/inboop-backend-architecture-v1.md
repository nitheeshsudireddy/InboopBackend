# Inboop — Backend Architecture & Service Boundaries (V1)

> This document defines backend architecture and service boundaries for Inboop.
> It is a design specification, not an implementation guide.
> It is also explicitly incremental-friendly: existing implementations should be aligned gradually, not rewritten.

---

## Core Principle

Optimize for clarity first, scale second.

A clean modular monolith beats premature microservices.

---

## Architecture Choice (V1)

- Spring Boot modular monolith
- Single deployable service
- Postgres as the primary datastore

---

## Module Boundaries (Package-Level)

Suggested modules:

- auth, users, workspaces
- contacts
- conversations, messages
- leads
- orders
- intent
- notifications
- integrations (Meta webhooks)
- search, analytics
- audit, common

Rule:
- Modules interact via services/interfaces, not direct cross-module repository access.

---

## Data Ownership Rules

- Each module owns its tables/entities and business invariants.
- Other modules may read via DTOs or projections.
- Mutations to another module's domain happen through the owning module's service methods.

Example:
- Order creation may call LeadService to mark a lead CONVERTED rather than updating lead tables directly.

---

## Webhook Ingestion Pipeline (Meta)

Pipeline:

Webhook Controller (fast 200 OK)
→ Signature validation
→ Enqueue/store event (optional raw event persistence)
→ Async processor:
   - Upsert Conversation + Message
   - Evaluate intent trigger eligibility
   - Emit domain events (e.g., MessageReceived)

Rules:
- Idempotent and retry-safe
- Heavy work must be async
- Duplicate events must not create duplicates

---

## Async Jobs & Scheduling

Async candidates:
- Webhook processing
- Intent evaluation
- Notification fan-out
- Auto-archiving
- Analytics rollups (later)

V1 approach:
- Spring async + scheduled jobs
Future:
- Swap to queue-based workers (SQS/Rabbit/Kafka) without changing business logic.

---

## Intent Boundary

- Only the intent module executes AI evaluation.
- Other modules read intent state.
- Intent triggers follow the AI Intent System spec.

---

## Notifications Boundary

- Notifications are derived from domain events (MessageReceived, AssignmentChanged, LeadConverted, OrderStatusChanged, etc.)
- Notifications must be non-blocking and eventually consistent.

---

## Transactions & Consistency

- Use local DB transactions only.
- Eventual consistency is acceptable for:
  - Notifications
  - Analytics
  - Intent
- Strong consistency required for:
  - Lead transitions
  - Order status and payment updates
  - Assignments

---

## API Design Rules

- REST resources with explicit action endpoints for state changes.
Examples:
- POST /conversations/{id}/assign
- POST /leads/{id}/close
- POST /orders/{id}/confirm

Avoid generic "update everything" endpoints where possible.

---

## Incremental Alignment Rules (Protect Existing Work)

- No table drops
- No renames unless explicitly approved
- Prefer additive migrations:
  - Add nullable columns
  - Add new tables
  - Add indexes
- Preserve backward compatibility
- Refactor module-by-module, not big bang rewrites
