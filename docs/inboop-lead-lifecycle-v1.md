# Inboop — Lead Lifecycle & Transitions (V1)

> This document defines how Leads behave in Inboop.
> It is a behavioral specification, not an implementation guide.

---

## Core Principle

A Lead represents a single, time-bounded sales opportunity within a conversation.

Leads are deliberate and controlled.
They are not automatically spam-created by AI.

---

## Lead Statuses (Authoritative)

A Lead can be in exactly one of the following states:

- NEW
  The lead has been created and is actively being worked on.

- CONVERTED
  The lead resulted in one or more Orders.

- LOST
  The customer explicitly declined or the opportunity was lost.

- CLOSED
  The lead ended without explicit win or loss.
  Examples:
  - Superseded by a newer lead
  - Conversation went inactive
  - Internal decision to stop pursuing

CLOSED is distinct from LOST.

---

## Lead Creation Rules

A Lead can be created when:
- Intent = BUYING
- No NEW Lead exists for the conversation

Ways to create a Lead:
- AI-suggested (user confirms)
- Manually by user

If a NEW Lead already exists:
- Creating a new Lead auto-marks the previous NEW Lead as CLOSED
- A system note is added: "Superseded by Lead X"

---

## Lead State Transitions

Allowed transitions:

NEW → CONVERTED
NEW → LOST
NEW → CLOSED

CONVERTED / LOST / CLOSED are terminal states.

Leads are never reopened.

A new buying cycle always creates a new Lead.

---

## Lead → Order Interaction

- When an Order is created with a leadId:
  - That Lead is automatically marked CONVERTED
- Orders created without a leadId:
  - Do not affect Lead state

---

## Lead Behavior

- Leads always belong to a Conversation
- Historical Leads are read-only
- Only the NEW Lead (if any) is actionable

---

## Design Intent

- Prevent lead spam
- Preserve accurate funnel analytics
- Support multiple buying cycles per conversation
- Keep behavior predictable for users
