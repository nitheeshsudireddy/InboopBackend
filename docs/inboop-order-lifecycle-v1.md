# Inboop — Order Lifecycle & Fulfillment Behavior (V1)

> This document defines how Orders behave in Inboop.
> It is a behavioral specification, not a UI or implementation guide.

---

## Core Principle

An Order is a transactional record.
It captures what was agreed to sell and fulfill, not conversation state.

Orders:
- Always belong to a Conversation
- May close a Lead
- Are immutable in intent but allow controlled edits
- Maintain an append-only audit timeline

---

## Order Status (Fulfillment Lifecycle)

NEW → CONFIRMED → SHIPPED → DELIVERED
        ↘
         CANCELLED

Statuses:
- NEW
- CONFIRMED
- SHIPPED
- DELIVERED
- CANCELLED

DELIVERED and CANCELLED are terminal.

---

## Payment Status (Financial Lifecycle)

UNPAID → PAID → REFUNDED

PaymentStatus:
- UNPAID
- PAID
- REFUNDED

DELIVERED + UNPAID is never valid.

---

## Payment Method (Workspace-Gated)

paymentMethod:
- ONLINE
- COD

Rules:
- COD is allowed only if workspace.allowCOD = true
- Default allowCOD = false
- COD is a regional/business configuration, not a core order state
- For COD orders, paymentStatus becomes PAID on delivery or manual confirmation

---

## Order Creation Rules

- Orders can be created from Inbox, Orders page, or from a Lead
- On creation:
  - orderStatus = NEW
  - paymentStatus = UNPAID
  - paymentMethod = ONLINE (default)
- Order must be linked to conversationId
- If linked to a Lead:
  - Lead is automatically marked CONVERTED

---

## Allowed Status Transitions

NEW → CONFIRMED
NEW → CANCELLED

CONFIRMED → SHIPPED
CONFIRMED → CANCELLED

SHIPPED → DELIVERED

Disallowed:
- DELIVERED → anything
- CANCELLED → anything

---

## Controlled Mutability

Editable before SHIPPED:
- Items
- Shipping address

Editable anytime:
- Payment status
- Notes / metadata

Locked fields:
- conversationId
- leadId (once set)
- orderId
- createdAt

---

## Order Detail (Conceptual Requirements)

Order detail views must be able to expose:

- Order summary (status, payment, channel, dates)
- Items (qty, price, totals)
- Shipping / fulfillment details
- Timeline / audit events

This is a behavioral requirement, not a UI design.

---

## Timeline (Audit Trail)

Timeline is append-only and records:
- Order creation
- Status changes
- Payment changes
- Edits and refunds

Each event includes:
- type
- description
- timestamp
- actor (USER | SYSTEM)
- source (INBOX | ORDERS)
