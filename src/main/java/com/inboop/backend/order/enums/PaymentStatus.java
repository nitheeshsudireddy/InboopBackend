package com.inboop.backend.order.enums;

/**
 * Payment status enum - separate from order fulfillment status.
 * Per inboop-order-lifecycle-v1.md
 */
public enum PaymentStatus {
    UNPAID,    // Payment not received
    PAID,      // Payment received
    REFUNDED   // Payment refunded
}
