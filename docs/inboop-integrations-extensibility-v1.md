# Inboop — Integrations & Extensibility (V1)

> This document defines integrations and extensibility in Inboop.
> It is a behavioral specification, not a UI or implementation guide.

---

## Core Principle

Integrations are edges, not core logic.

Core workflows must work even if integrations fail.

---

## Integration Categories

### A) Channel Integrations (V1 Critical)

Examples:
- Instagram
- WhatsApp
- Facebook Messenger

Role:
- Message ingestion
- Thread identity
- Webhooks as source of truth

Rules:
- Channels create/update Conversations and Messages
- Channels never create Leads or Orders directly
- Channel failures must not corrupt internal state

---

### B) Payments & Commerce Providers (V1 Tracking-Only)

Businesses can choose payment providers at the workspace level.

WorkspacePaymentConfig (conceptual):
- enabledProviders (e.g., STRIPE, RAZORPAY, MANUAL; extensible)
- defaultProvider
- allowCOD (workspace-gated, region-based)

V1 constraints:
- Provider choice is for tracking/reporting only
- Inboop does NOT generate payment links or consume payment webhooks in V1
- Users manually update paymentStatus (or via COD delivery logic)
- Payment configuration must never block order creation

Future:
- V2+ may add provider-specific integrations (payment links + webhooks) without changing core workflows

---

### C) Export / Outbound (Pro+ / Future)

Examples:
- CSV exports (Pro)
- Webhooks / API (Enterprise later)

Rules:
- Exports are read-only in V1
- External systems cannot mutate core state in V1

---

## Webhook Philosophy

### Inbound Webhooks (V1)

Used for:
- New messages
- Thread updates

Must be:
- Idempotent
- Retry-safe
- Order-aware (handle out-of-order delivery)

### Outbound Webhooks (Future)

Triggered on events like:
- LeadConverted
- OrderCreated
- OrderDelivered

Rules:
- Opt-in and non-blocking
- Failure never blocks core workflows

---

## Failure Handling

Integration failures should:
- Be logged
- Be visible in admin diagnostics
- Never block Inbox, Lead creation, or Order creation

---

## Extensibility (Future-Safe)

Design supports:
- Event-based hooks
- Metadata fields on entities
- Versioned APIs

Extensibility is designed now but not exposed in V1.

---

## Non-Goals (V1)

Out of scope:
- Two-way order sync
- Workflow automation engine
- Integration marketplace
- Multi-provider payment webhooks
