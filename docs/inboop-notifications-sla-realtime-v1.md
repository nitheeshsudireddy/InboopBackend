# Inboop — Notifications, SLAs & Real-Time Behavior (V1)

> This document defines how notifications, unread state, assignments, and responsiveness work in Inboop.
> It is a behavioral specification, not a UI or implementation guide.

---

## Core Principle

Signal what matters. Do not create noise.

Notifications should:
- Drive timely responses
- Respect assignments
- Avoid notification fatigue

---

## Unread Model

### Message-Level
A message is considered unread for a user if:
- It is inbound
- The user has not viewed the conversation since it arrived

### Conversation-Level
A conversation is unread for a user if:
- There exists at least one unread inbound message for that user

Unread state is per-user, not global.

---

## Assignment-Aware Behavior

Assignments influence who is notified, not who can access data.

### Assignment Rules
- Assigned user is the primary owner
- Other users retain visibility and ability to act
- Assignment never hides data in V1

### Notification Routing
- New inbound message:
  - If assigned → notify assigned user
  - If unassigned → notify all users
- Lead created or assigned:
  - Notify assigned user
- Order assigned or status updated:
  - Notify assigned user

---

## Notification Types (V1)

### In-App Notifications
- New inbound message
- Conversation assigned to you
- Lead assigned to you
- Order assigned to you
- Order status change (if assigned)

### Out of Scope (V1)
- Email notifications
- Push notifications

---

## SLA & Response Expectations (Informational)

SLA is informational and never enforced.

Each workspace may define:
- expectedFirstResponseMinutes

System tracks:
- Time from first inbound message → first outbound reply

If SLA is breached:
- Conversation is marked as "Over SLA"
- Visual indicator only
- No blocking or penalties

---

## Badges & Counters (Derived)

The system may surface:
- Unread conversation count
- Assigned-to-me count
- Over-SLA count

These values are derived, not stored.

---

## Lead & Order Event Notifications

### Lead Events
- Lead created
- Lead converted
- Lead closed or lost

Notify:
- Assigned user

### Order Events
- Order created
- Order confirmed
- Order shipped
- Order delivered
- Order refunded

Notify:
- Assigned user
- Order creator (if different)

---

## Noise Prevention Rules

Do NOT notify on:
- Outbound messages
- Message edits
- Internal notes

Additional rules:
- Batch notifications when possible
- Avoid notifying multiple users for the same event unless unassigned

---

## Real-Time vs Polling (Design Constraint)

Instagram and other platforms send webhooks to the backend in real time.

However, the user interface must NOT depend on real-time delivery to function correctly.

V1 assumptions:
- Backend receives events instantly via webhooks
- UI may update via polling, refresh, or delayed fetch
- Product behavior must remain correct even with delayed UI updates

This ensures:
- Robustness
- Simpler infrastructure
- Fewer race conditions

Real-time UI updates (WebSockets/SSE) may be added later without changing product behavior.

---

## Design Intent

- Keep the app responsive but resilient
- Avoid assumptions of instant UI updates
- Ensure workflows remain correct under polling
- Allow real-time to be an enhancement, not a dependency
