package com.inboop.backend.order.dto;

import com.inboop.backend.order.enums.PaymentStatus;

/**
 * Request DTOs for order actions.
 */
public class OrderActionRequest {

    /**
     * Request for updating payment status.
     */
    public static class UpdatePaymentStatusRequest {
        private PaymentStatus paymentStatus;

        public PaymentStatus getPaymentStatus() {
            return paymentStatus;
        }

        public void setPaymentStatus(PaymentStatus paymentStatus) {
            this.paymentStatus = paymentStatus;
        }
    }

    /**
     * Request for assigning an order.
     */
    public static class AssignOrderRequest {
        private Long assignedToUserId;

        public Long getAssignedToUserId() {
            return assignedToUserId;
        }

        public void setAssignedToUserId(Long assignedToUserId) {
            this.assignedToUserId = assignedToUserId;
        }
    }

    /**
     * Request for shipping an order (includes tracking info).
     */
    public static class ShipOrderRequest {
        private String carrier;
        private String trackingNumber;
        private String trackingUrl;
        private String note;

        public String getCarrier() {
            return carrier;
        }

        public void setCarrier(String carrier) {
            this.carrier = carrier;
        }

        public String getTrackingNumber() {
            return trackingNumber;
        }

        public void setTrackingNumber(String trackingNumber) {
            this.trackingNumber = trackingNumber;
        }

        public String getTrackingUrl() {
            return trackingUrl;
        }

        public void setTrackingUrl(String trackingUrl) {
            this.trackingUrl = trackingUrl;
        }

        public String getNote() {
            return note;
        }

        public void setNote(String note) {
            this.note = note;
        }
    }

    /**
     * Request for cancelling an order.
     */
    public static class CancelOrderRequest {
        private String reason;

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }
    }

    /**
     * Request for refunding an order.
     */
    public static class RefundOrderRequest {
        private String reason;

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }
    }
}
