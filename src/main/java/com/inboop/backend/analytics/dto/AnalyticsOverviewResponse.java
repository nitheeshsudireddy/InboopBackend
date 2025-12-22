package com.inboop.backend.analytics.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Response DTO for analytics overview endpoint.
 * Contains all V1 metrics as defined in ANALYTICS-MVP-CONTRACT.md
 */
public class AnalyticsOverviewResponse {

    // Period info
    private LocalDateTime periodStart;
    private LocalDateTime periodEnd;

    // Orders metrics
    private OrderMetrics orders;

    // Revenue metrics
    private RevenueMetrics revenue;

    // Leads metrics
    private LeadMetrics leads;

    // Inbox metrics
    private InboxMetrics inbox;

    public AnalyticsOverviewResponse() {}

    // Getters and Setters
    public LocalDateTime getPeriodStart() {
        return periodStart;
    }

    public void setPeriodStart(LocalDateTime periodStart) {
        this.periodStart = periodStart;
    }

    public LocalDateTime getPeriodEnd() {
        return periodEnd;
    }

    public void setPeriodEnd(LocalDateTime periodEnd) {
        this.periodEnd = periodEnd;
    }

    public OrderMetrics getOrders() {
        return orders;
    }

    public void setOrders(OrderMetrics orders) {
        this.orders = orders;
    }

    public RevenueMetrics getRevenue() {
        return revenue;
    }

    public void setRevenue(RevenueMetrics revenue) {
        this.revenue = revenue;
    }

    public LeadMetrics getLeads() {
        return leads;
    }

    public void setLeads(LeadMetrics leads) {
        this.leads = leads;
    }

    public InboxMetrics getInbox() {
        return inbox;
    }

    public void setInbox(InboxMetrics inbox) {
        this.inbox = inbox;
    }

    /**
     * Order-related metrics (ORD-01 to ORD-06)
     */
    public static class OrderMetrics {
        private long created;           // ORD-01
        private long delivered;         // ORD-02
        private long cancelled;         // ORD-03
        private BigDecimal cancellationRate; // ORD-04 (percentage, 0-100)
        private Map<String, Long> byChannel; // ORD-05
        private Map<String, Long> byStatus;  // ORD-06 (snapshot)

        public OrderMetrics() {}

        public long getCreated() {
            return created;
        }

        public void setCreated(long created) {
            this.created = created;
        }

        public long getDelivered() {
            return delivered;
        }

        public void setDelivered(long delivered) {
            this.delivered = delivered;
        }

        public long getCancelled() {
            return cancelled;
        }

        public void setCancelled(long cancelled) {
            this.cancelled = cancelled;
        }

        public BigDecimal getCancellationRate() {
            return cancellationRate;
        }

        public void setCancellationRate(BigDecimal cancellationRate) {
            this.cancellationRate = cancellationRate;
        }

        public Map<String, Long> getByChannel() {
            return byChannel;
        }

        public void setByChannel(Map<String, Long> byChannel) {
            this.byChannel = byChannel;
        }

        public Map<String, Long> getByStatus() {
            return byStatus;
        }

        public void setByStatus(Map<String, Long> byStatus) {
            this.byStatus = byStatus;
        }
    }

    /**
     * Revenue-related metrics (REV-01 to REV-04)
     */
    public static class RevenueMetrics {
        private BigDecimal gross;       // REV-01
        private BigDecimal refunded;    // REV-02
        private BigDecimal net;         // REV-03
        private BigDecimal averageOrderValue; // REV-04
        private String currency;

        public RevenueMetrics() {}

        public BigDecimal getGross() {
            return gross;
        }

        public void setGross(BigDecimal gross) {
            this.gross = gross;
        }

        public BigDecimal getRefunded() {
            return refunded;
        }

        public void setRefunded(BigDecimal refunded) {
            this.refunded = refunded;
        }

        public BigDecimal getNet() {
            return net;
        }

        public void setNet(BigDecimal net) {
            this.net = net;
        }

        public BigDecimal getAverageOrderValue() {
            return averageOrderValue;
        }

        public void setAverageOrderValue(BigDecimal averageOrderValue) {
            this.averageOrderValue = averageOrderValue;
        }

        public String getCurrency() {
            return currency;
        }

        public void setCurrency(String currency) {
            this.currency = currency;
        }
    }

    /**
     * Lead-related metrics (LEAD-01 to LEAD-08)
     */
    public static class LeadMetrics {
        private long created;           // LEAD-01
        private long converted;         // LEAD-02
        private long lost;              // LEAD-03
        private long closed;            // LEAD-04
        private BigDecimal conversionRate; // LEAD-05 (percentage, 0-100)
        private long open;              // LEAD-06 (snapshot)
        private Map<String, Long> byChannel; // LEAD-07
        private Map<String, Long> byType;    // LEAD-08

        public LeadMetrics() {}

        public long getCreated() {
            return created;
        }

        public void setCreated(long created) {
            this.created = created;
        }

        public long getConverted() {
            return converted;
        }

        public void setConverted(long converted) {
            this.converted = converted;
        }

        public long getLost() {
            return lost;
        }

        public void setLost(long lost) {
            this.lost = lost;
        }

        public long getClosed() {
            return closed;
        }

        public void setClosed(long closed) {
            this.closed = closed;
        }

        public BigDecimal getConversionRate() {
            return conversionRate;
        }

        public void setConversionRate(BigDecimal conversionRate) {
            this.conversionRate = conversionRate;
        }

        public long getOpen() {
            return open;
        }

        public void setOpen(long open) {
            this.open = open;
        }

        public Map<String, Long> getByChannel() {
            return byChannel;
        }

        public void setByChannel(Map<String, Long> byChannel) {
            this.byChannel = byChannel;
        }

        public Map<String, Long> getByType() {
            return byType;
        }

        public void setByType(Map<String, Long> byType) {
            this.byType = byType;
        }
    }

    /**
     * Inbox-related metrics (INBOX-01 to INBOX-06, SLA-01, SLA-02)
     */
    public static class InboxMetrics {
        private long activeConversations;      // INBOX-01 (snapshot)
        private long totalUnreadMessages;      // INBOX-02 (snapshot)
        private long conversationsStarted;     // INBOX-03
        private long inboundMessages;          // INBOX-04
        private long outboundMessages;         // INBOX-05
        private Map<String, Long> conversationsByChannel; // INBOX-06
        private BigDecimal avgFirstResponseMinutes; // SLA-01
        private long conversationsWithoutResponse;  // SLA-02 (snapshot)

        public InboxMetrics() {}

        public long getActiveConversations() {
            return activeConversations;
        }

        public void setActiveConversations(long activeConversations) {
            this.activeConversations = activeConversations;
        }

        public long getTotalUnreadMessages() {
            return totalUnreadMessages;
        }

        public void setTotalUnreadMessages(long totalUnreadMessages) {
            this.totalUnreadMessages = totalUnreadMessages;
        }

        public long getConversationsStarted() {
            return conversationsStarted;
        }

        public void setConversationsStarted(long conversationsStarted) {
            this.conversationsStarted = conversationsStarted;
        }

        public long getInboundMessages() {
            return inboundMessages;
        }

        public void setInboundMessages(long inboundMessages) {
            this.inboundMessages = inboundMessages;
        }

        public long getOutboundMessages() {
            return outboundMessages;
        }

        public void setOutboundMessages(long outboundMessages) {
            this.outboundMessages = outboundMessages;
        }

        public Map<String, Long> getConversationsByChannel() {
            return conversationsByChannel;
        }

        public void setConversationsByChannel(Map<String, Long> conversationsByChannel) {
            this.conversationsByChannel = conversationsByChannel;
        }

        public BigDecimal getAvgFirstResponseMinutes() {
            return avgFirstResponseMinutes;
        }

        public void setAvgFirstResponseMinutes(BigDecimal avgFirstResponseMinutes) {
            this.avgFirstResponseMinutes = avgFirstResponseMinutes;
        }

        public long getConversationsWithoutResponse() {
            return conversationsWithoutResponse;
        }

        public void setConversationsWithoutResponse(long conversationsWithoutResponse) {
            this.conversationsWithoutResponse = conversationsWithoutResponse;
        }
    }
}
