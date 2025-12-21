package com.inboop.backend.order.enums;

/**
 * Payment method enum.
 * Per inboop-order-lifecycle-v1.md
 */
public enum PaymentMethod {
    ONLINE,    // Online payment (Stripe, Razorpay, etc.)
    COD,       // Cash on delivery
    MANUAL,    // Manual/other payment method
    BANK_TRANSFER // Bank transfer
}
