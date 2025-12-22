package com.inboop.backend.order.dto;

import com.inboop.backend.order.enums.PaymentMethod;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO for creating a new order.
 * Required: conversationId (order must be linked to a conversation)
 * Optional: items, totalAmount, currency, notes, paymentMethod, leadId
 */
public class CreateOrderRequest {

    // Required: Link to conversation (provides customer info, channel, assignee)
    private Long conversationId;

    // Optional: Link to lead (if provided and lead is NEW, auto-mark as CONVERTED)
    private Long leadId;

    // Optional: Order items
    private List<OrderItemRequest> items;

    // Optional: Total amount (if not provided, calculated from items)
    private BigDecimal totalAmount;

    // Optional: Currency (defaults to INR)
    private String currency;

    // Optional: Order notes
    private String notes;

    // Optional: Payment method
    private PaymentMethod paymentMethod;

    // Idempotency key to prevent duplicate orders on retry
    private String idempotencyKey;

    // Nested DTO for order items
    public static class OrderItemRequest {
        private String name;
        private Integer quantity;
        private BigDecimal unitPrice;

        public OrderItemRequest() {}

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }

        public BigDecimal getUnitPrice() {
            return unitPrice;
        }

        public void setUnitPrice(BigDecimal unitPrice) {
            this.unitPrice = unitPrice;
        }
    }

    // Getters and Setters
    public Long getConversationId() {
        return conversationId;
    }

    public void setConversationId(Long conversationId) {
        this.conversationId = conversationId;
    }

    public Long getLeadId() {
        return leadId;
    }

    public void setLeadId(Long leadId) {
        this.leadId = leadId;
    }

    public List<OrderItemRequest> getItems() {
        return items;
    }

    public void setItems(List<OrderItemRequest> items) {
        this.items = items;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }
}
