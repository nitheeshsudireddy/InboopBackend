# Inboop — Inbox Lifecycle & Behavior (V1)

> This document defines how the Inbox behaves in Inboop.
> It is a behavioral specification, not a UI or implementation guide.
> All Inbox-related features must conform to this document.

---

## Core Principle

The Inbox is the control center of Inboop.

- Inbox is conversation-centric
- Conversations are long-lived
- Leads and Orders are created from conversations
- Users should always understand the next best action

---

## Conversation State Model

A conversation can simultaneously have:

- Intent state (AI-derived)
- Lead state (business-driven)
- Order state (transaction-driven)

These states are orthogonal.

Example:
- Intent = BUYING
- Lead = OPEN
- Orders = 2 (1 delivered, 1 pending)

---

## New Inbound Message Flow

When a customer sends a new inbound message:

1. Message is stored
2. Conversation `lastMessageAt` is updated
3. Conversation moves to top of Inbox

---

## Intent Evaluation Triggers

AI intent classification runs ONLY when one of the following is true:

- First inbound message in the conversation
- New inbound message after idle threshold
- New inbound message after a Lead is CLOSED
- Manual re-evaluate (future feature)

Intent classification does NOT run:
- On every message
- On outbound messages
- During active back-and-forth

Intent is stored on the Conversation as IntentState:
- label
- confidence
- lastEvaluatedAt
- reasonShort

---

## Inbox Context (Conceptual)

Each open conversation conceptually exposes:

- Channel (Instagram / WhatsApp / Facebook)
- Customer identity (from Contact)
- Intent label + confidence
- Lead status (if any)
- Order count

This context drives available actions.

---

## Lead-Related Behavior in Inbox

### Case 1: Intent = BUYING, no OPEN Lead

System behavior:
- Suggests action: "Create Lead"
- Lead source = AI

User behavior:
- User may create a Lead
- No auto-creation of Lead in V1

---

### Case 2: OPEN Lead exists

System behavior:
- Shows Lead status = OPEN
- Disables "Create Lead"

User actions:
- View Lead
- Create Order

AI behavior:
- No new lead suggestions while an OPEN Lead exists

---

### Case 3: Lead is CLOSED (WON / LOST / CLOSED)

System behavior:
- Next inbound message may trigger intent re-evaluation
- If intent becomes BUYING again:
  - Suggest "Create New Lead"
- Previous Leads remain historical

---

## Order-Related Behavior in Inbox

### Create Order from Inbox

User can create an Order when:
- An OPEN Lead exists, OR
- User explicitly creates Order without Lead (allowed but discouraged)

System behavior:
- Order is linked to conversationId
- If an OPEN Lead exists:
  - Order is linked to leadId
  - Lead is automatically marked WON

---

### Viewing Orders from Inbox

From a conversation, user can:
- See Order count
- View all Orders filtered by conversationId
- Jump to specific Order details

---

## Guardrails

- Only one OPEN Lead per conversation
- Cannot auto-create Lead if one is already OPEN
- Orders may exist even if Lead is CLOSED (manual override)
- Conversation history is never reset or overwritten

---

## Design Intent

- Inbox remains the primary workflow surface
- AI assists but does not take irreversible actions
- Leads are controlled and intentional
- Orders are first-class but always contextual to conversations
