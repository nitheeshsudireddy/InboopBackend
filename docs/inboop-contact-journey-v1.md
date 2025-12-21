# Inboop — Contact & Customer Journey (V1)

> This document defines how customer identity and history are modeled in Inboop.
> It is a behavioral specification, not a UI or implementation guide.

---

## Core Principle

Conversation is the interaction.
Contact is the customer.

Contacts represent real people and aggregate long-term history.
Conversations are channel-specific interaction threads and never merge.

---

## Contact vs Conversation

### Conversation
- One DM thread
- One channel (Instagram, WhatsApp, Facebook)
- Short- to medium-lived
- Owns workflow actions (lead creation, order creation)

### Contact
- Represents a real person
- Long-lived
- Aggregates:
  - Conversations
  - Leads
  - Orders

Contacts do not own workflows.

---

## Contact Creation & Linking (V1 Behavior)

When a new inbound conversation is created:

1. System attempts to link to an existing Contact using strong signals:
   - Phone number (WhatsApp)
   - Email (if available)

2. If no strong signal exists, weak signals may be considered:
   - Same Instagram handle
   - Same Facebook profile

3. If a confident match exists:
   - Link conversation to existing Contact

4. If no confident match exists:
   - Create a new Contact

V1 Guardrail:
- No automatic cross-channel merging without strong signals
- Manual merge may be added in the future

---

## Contact Timeline (Conceptual)

Each Contact has an aggregated customer timeline containing:
- Conversations started
- Leads created, closed, or converted
- Orders created, delivered, or refunded

This timeline is read-only and informational.

---

## Contact-Level Insights (Future-Safe)

Even if not surfaced in UI yet, Contacts must support:
- Total orders count
- Lifetime value (sum of PAID orders)
- Last interaction date
- Lead conversion history

This is why Contact is a first-class entity.

---

## Relationship Rules

- A Contact can have multiple Conversations
- A Contact can have multiple Leads (across conversations)
- A Contact can have multiple Orders
- Orders always belong to Conversations
- Contacts aggregate data but never drive workflows

---

## Non-Goals (V1)

The following are explicitly out of scope for V1:
- Automatic cross-channel Contact merging without strong signals
- Editing Conversations from Contact view
- Creating Leads or Orders directly from Contact view

These constraints prevent data corruption and UX confusion.

---

## Design Intent

- Preserve Inbox as the primary workflow surface
- Build CRM insight without premature complexity
- Allow future expansion without refactoring core models
