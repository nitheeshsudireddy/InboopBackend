# Inboop — Roles, Permissions & Pricing Gates (V1)

> This document defines how users, roles, assignments, and pricing plans work in Inboop.
> It is a behavioral specification, not a UI or implementation guide.

---

## Core Principle

Pricing gates capability, not data ownership.

Data models remain consistent across plans.
Plans restrict actions and limits, not access to historical data.

---

## Roles (V1)

### ADMIN
- Exactly one per workspace
- Manages billing, workspace settings, users, and channels
- Full data access

### MEMBER
- Day-to-day operations
- Inbox, Leads, Orders
- Can be assigned work
- No access to billing or workspace settings

All users belong to exactly one Workspace and have exactly one role.

---

## Assignment Model

The following entities support assignment:
- Conversation
- Lead
- Order

Each entity may have:
- assignedToUserId (nullable)

Rules:
- Assignment is optional
- Assigned items remain visible to all users
- Assignment indicates ownership, not exclusivity
- Unassigned items act as a shared queue

---

## Pricing Plans

### Starter
- 1 Workspace
- 1 User (ADMIN only)
- Limited AI intent evaluations
- Soft cap on Leads
- Unlimited Orders
- No COD
- No exports

---

### Pro
- 1 Workspace
- Up to 5 users total
  - Exactly 1 ADMIN
  - Up to 4 MEMBERS
- Increased AI limits
- Unlimited Leads and Orders
- Assignment enabled
- COD allowed (region-based)
- Export (CSV / JSON)

---

### Enterprise
- Multiple Workspaces (optional)
- Unlimited users
- Multiple admins (optional)
- Unlimited AI
- Custom roles
- API access and Webhooks
- Audit logs

---

## Permissions Overview

Inbox:
- ADMIN: full access
- MEMBER: full access
- Assignment supported

Leads:
- ADMIN: full access
- MEMBER: create, update, close
- Assignment supported

Orders:
- ADMIN: full access including refunds
- MEMBER: create and update
- Refunds may be ADMIN-only
- Assignment supported

Contacts:
- ADMIN: view, edit, merge
- MEMBER: view only

Settings and Billing:
- ADMIN only

---

## Downgrade Behavior

- Data is never deleted on downgrade
- Restricted actions become disabled
- Extra users become inactive
- Assignment fields become read-only
- UI explains restrictions and offers upgrade CTA

---

## Non-Goals (V1)

- Per-field permissions
- Approval chains
- Territory-based access
- Workflow automation by role
