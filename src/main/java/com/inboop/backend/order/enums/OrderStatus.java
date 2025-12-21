package com.inboop.backend.order.enums;

/**
 * Order fulfillment status enum.
 * Per inboop-order-lifecycle-v1.md:
 * - Fulfillment track: NEW → CONFIRMED → SHIPPED → DELIVERED
 * - CANCELLED can branch from any pre-delivered state
 *
 * Note: REFUNDED is now in PaymentStatus. Legacy REFUNDED preserved
 * for backward compatibility but should not be used for new orders.
 * PENDING is legacy, use NEW for new orders.
 * PROCESSING is legacy intermediate state.
 */
public enum OrderStatus {
    NEW,        // Order created, awaiting confirmation
    PENDING,    // @deprecated - Legacy: Use NEW instead
    CONFIRMED,  // Order confirmed by business
    PROCESSING, // @deprecated - Legacy: Intermediate state
    SHIPPED,    // Order shipped
    DELIVERED,  // Order delivered (terminal)
    CANCELLED,  // Order cancelled (terminal)
    REFUNDED    // @deprecated - Legacy: Use PaymentStatus.REFUNDED instead
}
