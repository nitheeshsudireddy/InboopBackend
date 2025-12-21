# Inboop — AI Intent System (V1)

> This document defines how AI intent classification works in Inboop.
> It is a behavioral and intelligence specification, not a UI or implementation guide.

---

## Core Principle

AI suggests. Humans decide.

AI exists to assist prioritization and insight, not to take irreversible business actions.

---

## Intent Taxonomy (V1)

The intent taxonomy is intentionally small and stable.

Intent labels:
- BUYING
  Examples: price inquiries, availability, shipping questions, purchase intent

- SUPPORT
  Examples: order status, complaints, refunds, returns

- BROWSING
  Examples: general questions, product discovery, "just looking"

- SPAM
  Examples: promotions, irrelevant or malicious messages

- OTHER
  Used when intent cannot be confidently classified

If confidence is below threshold, intent defaults to OTHER.

---

## Intent Output Structure

Each AI evaluation produces a single IntentState stored on the Conversation.

IntentState fields:
- label
- confidence (0.0–1.0)
- reasonShort (one-line explanation, user-visible)
- evaluatedAt
- modelVersion (optional)

---

## When AI Runs (Authoritative Rules)

AI intent evaluation runs ONLY on:

1. First inbound message in a conversation
2. Inbound message after idle threshold
3. Inbound message after a Lead is CLOSED
4. Manual re-evaluate (future feature)

AI does NOT run:
- On outbound messages
- On every back-and-forth
- While a NEW Lead exists

---

## Idle Threshold

Each workspace defines:
- intentIdleThresholdHours

Defaults:
- 24 hours

Meaning:
If the last inbound message was older than the idle threshold, the next inbound message triggers AI evaluation.

---

## Confidence Thresholds & Behavior

Two confidence thresholds are defined:

- High confidence threshold: 0.75
- Low confidence floor: 0.40

Behavior:
- confidence ≥ 0.75
  - Show intent prominently
  - Suggest next action (e.g., Create Lead)

- 0.40 ≤ confidence < 0.75
  - Show intent subtly
  - No automatic suggestions

- confidence < 0.40
  - Intent = OTHER
  - No suggestions

Thresholds may become workspace-configurable later.

---

## Intent → Product Behavior Mapping

BUYING:
- If no NEW Lead exists:
  - Suggest "Create Lead"
- If NEW Lead exists:
  - No suggestion

SUPPORT:
- No Lead or Order suggestions

BROWSING:
- No automatic suggestions
- Manual Lead creation allowed

SPAM:
- No Lead or Order suggestions
- May enable mute/archive actions in future

OTHER:
- No automatic suggestions

---

## Intent Stability Rules

- Intent labels are sticky
- New AI evaluations may overwrite intent only if:
  - Confidence is significantly higher, OR
  - Previous intent is stale (older than idle threshold)

This prevents intent flickering.

---

## Manual Overrides (Future-Safe)

Users may:
- Manually set intent label
- Trigger "Re-evaluate intent"

Rules:
- Manual intent temporarily overrides AI intent
- Future AI evaluations may overwrite manual intent
- Manual intent source = USER
- AI intent source = SYSTEM

---

## Explicit Non-Goals (V1)

The following are out of scope for V1:
- Multi-intent classification
- Sentiment analysis
- Auto-replies
- AI-generated messages
- Automatic Lead or Order creation

---

## Design Intent

- Keep AI explainable and predictable
- Avoid accidental automation
- Preserve user trust
- Enable future intelligence without refactoring core flows
