# Inboop — Enterprise Plan: Organizations & Multiple Workspaces (V1 Design)

> This document defines the Enterprise plan structure for Inboop.
> Enterprise introduces Organizations that can contain multiple Workspaces.
> This is a design specification, not an implementation guide.

---

## Core Principle

Enterprise is composition, not redesign.

- Existing Workspace-scoped functionality remains unchanged
- Enterprise adds an Organization layer above Workspaces
- Pro and Starter behavior must remain backwards compatible

---

## Conceptual Model

Current (Pro / Starter):
- Workspace → Users → Conversations / Leads / Orders

Enterprise adds:
- Organization → Workspaces → Users → Conversations / Leads / Orders

Enterprise does NOT automatically mean shared inboxes or shared data across workspaces.

---

## Entity Relationships

### Organization
- An Organization is the top-level container for Enterprise customers.
- An Organization can have multiple Workspaces.
- Billing is Organization-level (future; not required in V1).

### Workspace
- A Workspace belongs to an Organization (Enterprise) OR has no organizationId (Pro/Starter).
- All operational data remains workspace-scoped:
  - Conversations
  - Messages
  - Leads
  - Orders
  - Contacts
  - Analytics (V1)

---

## Roles & Permissions

Enterprise introduces organization-level roles.

### Organization Roles
- ORG_ADMIN
  - Manages organization settings (future)
  - Manages billing (future)
  - Can create workspaces (future)
  - Can manage workspace membership (future)
- ORG_MEMBER (optional)
  - May belong to the org without global admin access

### Workspace Roles (unchanged)
- WORKSPACE_ADMIN
- WORKSPACE_USER (regular)

A single user may have different roles across workspaces.

Important:
- Organization roles do not automatically grant access to workspace data unless membership exists for that workspace.

---

## Plan Capabilities (Conceptual)

Pro:
- 1 workspace
- Up to 5 users
- Workspace-scoped analytics

Enterprise:
- Multiple workspaces under one organization
- Users per workspace configurable
- Centralized billing (future)
- Cross-workspace analytics (future)
- Advanced audit logs / SSO (future)

---

## Data Model Additions (Minimal)

Enterprise requires minimal schema additions:

1) organization
- id
- name
- plan (ENTERPRISE)
- createdAt

2) organization_user
- organizationId
- userId
- role (ORG_ADMIN / ORG_MEMBER)
- createdAt

3) workspace change
- workspace.organizationId (nullable)
  - NULL for Pro/Starter
  - Set for Enterprise

All changes must be additive and Flyway-compatible.

---

## Request Scoping Rules

All reads/writes remain workspace-scoped in V1.

- User authenticates
- User selects a workspace context
- All APIs operate within that workspace context

Enterprise must not introduce implicit cross-workspace access.

---

## Analytics Implications (V1 vs Future)

V1:
- Analytics remain workspace-scoped using existing endpoints.

Future:
- Org-level analytics can be implemented as aggregation across workspaces:
  - SUM/AVG across all workspaces where workspace.organizationId = X

No ingestion logic changes are required for this.

---

## Non-Goals (V1)

Explicitly out of scope for V1:
- Workspace switcher UI
- Org-level billing UI
- SSO / SCIM provisioning
- Cross-workspace unified inbox
- Cross-workspace global search
- Org-level automation engine

---

## Design Intent

- Keep current product behavior stable
- Enable Enterprise as an additive layer
- Avoid breaking changes or large refactors
- Preserve workspace as the operational boundary

---
