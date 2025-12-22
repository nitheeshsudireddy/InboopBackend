package com.inboop.backend.analytics.service;

import com.inboop.backend.analytics.dto.AnalyticsOverviewResponse;
import com.inboop.backend.analytics.dto.AnalyticsOverviewResponse.*;
import com.inboop.backend.lead.enums.ChannelType;
import com.inboop.backend.lead.repository.ConversationRepository;
import com.inboop.backend.lead.repository.LeadRepository;
import com.inboop.backend.lead.repository.MessageRepository;
import com.inboop.backend.order.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for computing analytics metrics.
 * All metrics are computed at query time per ANALYTICS-MVP-CONTRACT.md
 */
@Service
@Transactional(readOnly = true)
public class AnalyticsService {

    private final OrderRepository orderRepository;
    private final LeadRepository leadRepository;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;

    public AnalyticsService(
            OrderRepository orderRepository,
            LeadRepository leadRepository,
            ConversationRepository conversationRepository,
            MessageRepository messageRepository) {
        this.orderRepository = orderRepository;
        this.leadRepository = leadRepository;
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
    }

    /**
     * Get analytics overview for a business within a time period.
     *
     * @param businessId The business ID (null for all businesses - demo mode)
     * @param channel Optional channel filter
     * @param periodStart Start of the period (inclusive)
     * @param periodEnd End of the period (exclusive)
     * @param currency Currency for revenue metrics (informational only in V1)
     * @return AnalyticsOverviewResponse with all metrics
     */
    public AnalyticsOverviewResponse getOverview(
            Long businessId,
            ChannelType channel,
            LocalDateTime periodStart,
            LocalDateTime periodEnd,
            String currency) {

        AnalyticsOverviewResponse response = new AnalyticsOverviewResponse();
        response.setPeriodStart(periodStart);
        response.setPeriodEnd(periodEnd);

        // Compute each metric category
        response.setOrders(computeOrderMetrics(businessId, channel, periodStart, periodEnd));
        response.setRevenue(computeRevenueMetrics(businessId, channel, periodStart, periodEnd, currency));
        response.setLeads(computeLeadMetrics(businessId, channel, periodStart, periodEnd));
        response.setInbox(computeInboxMetrics(businessId, channel, periodStart, periodEnd));

        return response;
    }

    /**
     * Compute order metrics (ORD-01 to ORD-06).
     */
    private OrderMetrics computeOrderMetrics(
            Long businessId,
            ChannelType channel,
            LocalDateTime periodStart,
            LocalDateTime periodEnd) {

        OrderMetrics metrics = new OrderMetrics();

        // ORD-01: Orders created
        metrics.setCreated(orderRepository.countOrdersCreated(businessId, channel, periodStart, periodEnd));

        // ORD-02: Orders delivered
        metrics.setDelivered(orderRepository.countOrdersDelivered(businessId, channel, periodStart, periodEnd));

        // ORD-03: Orders cancelled
        metrics.setCancelled(orderRepository.countOrdersCancelled(businessId, channel, periodStart, periodEnd));

        // ORD-04: Cancellation rate
        long terminalOrders = metrics.getDelivered() + metrics.getCancelled();
        if (terminalOrders > 0) {
            BigDecimal rate = BigDecimal.valueOf(metrics.getCancelled())
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(terminalOrders), 2, RoundingMode.HALF_UP);
            metrics.setCancellationRate(rate);
        } else {
            metrics.setCancellationRate(BigDecimal.ZERO);
        }

        // ORD-05: Orders by channel (only if no channel filter)
        if (channel == null) {
            metrics.setByChannel(toChannelMap(orderRepository.countOrdersByChannel(businessId, periodStart, periodEnd)));
        }

        // ORD-06: Orders by status (snapshot)
        metrics.setByStatus(toStatusMap(orderRepository.countOrdersByStatus(businessId)));

        return metrics;
    }

    /**
     * Compute revenue metrics (REV-01 to REV-04).
     */
    private RevenueMetrics computeRevenueMetrics(
            Long businessId,
            ChannelType channel,
            LocalDateTime periodStart,
            LocalDateTime periodEnd,
            String currency) {

        RevenueMetrics metrics = new RevenueMetrics();

        // REV-01: Gross revenue
        BigDecimal gross = orderRepository.sumGrossRevenue(businessId, channel, periodStart, periodEnd);
        metrics.setGross(gross != null ? gross : BigDecimal.ZERO);

        // REV-02: Refunded amount
        BigDecimal refunded = orderRepository.sumRefundedAmount(businessId, channel, periodStart, periodEnd);
        metrics.setRefunded(refunded != null ? refunded : BigDecimal.ZERO);

        // REV-03: Net revenue
        metrics.setNet(metrics.getGross().subtract(metrics.getRefunded()));

        // REV-04: Average order value
        BigDecimal aov = orderRepository.avgOrderValue(businessId, channel, periodStart, periodEnd);
        metrics.setAverageOrderValue(aov != null ? aov.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO);

        // Currency (informational)
        metrics.setCurrency(currency != null ? currency : "INR");

        return metrics;
    }

    /**
     * Compute lead metrics (LEAD-01 to LEAD-08).
     */
    private LeadMetrics computeLeadMetrics(
            Long businessId,
            ChannelType channel,
            LocalDateTime periodStart,
            LocalDateTime periodEnd) {

        LeadMetrics metrics = new LeadMetrics();

        // LEAD-01: Leads created
        metrics.setCreated(leadRepository.countLeadsCreated(businessId, channel, periodStart, periodEnd));

        // LEAD-02: Leads converted
        metrics.setConverted(leadRepository.countLeadsConverted(businessId, channel, periodStart, periodEnd));

        // LEAD-03: Leads lost
        metrics.setLost(leadRepository.countLeadsLost(businessId, channel, periodStart, periodEnd));

        // LEAD-04: Leads closed
        metrics.setClosed(leadRepository.countLeadsClosed(businessId, channel, periodStart, periodEnd));

        // LEAD-05: Conversion rate
        long terminalLeads = metrics.getConverted() + metrics.getLost() + metrics.getClosed();
        if (terminalLeads > 0) {
            BigDecimal rate = BigDecimal.valueOf(metrics.getConverted())
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(terminalLeads), 2, RoundingMode.HALF_UP);
            metrics.setConversionRate(rate);
        } else {
            metrics.setConversionRate(BigDecimal.ZERO);
        }

        // LEAD-06: Open leads (snapshot)
        metrics.setOpen(leadRepository.countOpenLeads(businessId));

        // LEAD-07: Leads by channel (only if no channel filter)
        if (channel == null) {
            metrics.setByChannel(toChannelMap(leadRepository.countLeadsByChannel(businessId, periodStart, periodEnd)));
        }

        // LEAD-08: Leads by type
        metrics.setByType(toStringMap(leadRepository.countLeadsByType(businessId, periodStart, periodEnd)));

        return metrics;
    }

    /**
     * Compute inbox metrics (INBOX-01 to INBOX-06, SLA-01, SLA-02).
     */
    private InboxMetrics computeInboxMetrics(
            Long businessId,
            ChannelType channel,
            LocalDateTime periodStart,
            LocalDateTime periodEnd) {

        InboxMetrics metrics = new InboxMetrics();

        // INBOX-01: Active conversations (snapshot)
        metrics.setActiveConversations(conversationRepository.countActiveConversations(businessId));

        // INBOX-02: Total unread messages (snapshot)
        metrics.setTotalUnreadMessages(conversationRepository.sumUnreadMessages(businessId));

        // INBOX-03: Conversations started
        metrics.setConversationsStarted(conversationRepository.countConversationsStarted(businessId, channel, periodStart, periodEnd));

        // INBOX-04: Inbound messages
        metrics.setInboundMessages(messageRepository.countInboundMessages(businessId, periodStart, periodEnd));

        // INBOX-05: Outbound messages
        metrics.setOutboundMessages(messageRepository.countOutboundMessages(businessId, periodStart, periodEnd));

        // INBOX-06: Conversations by channel (only if no channel filter)
        if (channel == null) {
            metrics.setConversationsByChannel(toChannelMap(conversationRepository.countConversationsByChannel(businessId, periodStart, periodEnd)));
        }

        // SLA-01: Average first response time (in minutes)
        Double avgSeconds = messageRepository.avgFirstResponseTimeSeconds(businessId, periodStart, periodEnd);
        if (avgSeconds != null && avgSeconds > 0) {
            BigDecimal minutes = BigDecimal.valueOf(avgSeconds)
                    .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
            metrics.setAvgFirstResponseMinutes(minutes);
        } else {
            metrics.setAvgFirstResponseMinutes(BigDecimal.ZERO);
        }

        // SLA-02: Conversations without response (snapshot)
        metrics.setConversationsWithoutResponse(conversationRepository.countConversationsWithoutResponse(businessId));

        return metrics;
    }

    /**
     * Convert query result to channel map.
     */
    private Map<String, Long> toChannelMap(List<Object[]> results) {
        Map<String, Long> map = new HashMap<>();
        // Initialize all channels to 0
        for (ChannelType ct : ChannelType.values()) {
            map.put(ct.name(), 0L);
        }
        // Populate with actual counts
        for (Object[] row : results) {
            if (row[0] != null) {
                String key = row[0] instanceof ChannelType ? ((ChannelType) row[0]).name() : row[0].toString();
                map.put(key, ((Number) row[1]).longValue());
            }
        }
        return map;
    }

    /**
     * Convert query result to status map (for order statuses).
     */
    private Map<String, Long> toStatusMap(List<Object[]> results) {
        Map<String, Long> map = new HashMap<>();
        for (Object[] row : results) {
            if (row[0] != null) {
                String key = row[0].toString();
                map.put(key, ((Number) row[1]).longValue());
            }
        }
        return map;
    }

    /**
     * Convert query result to generic string map.
     */
    private Map<String, Long> toStringMap(List<Object[]> results) {
        Map<String, Long> map = new HashMap<>();
        for (Object[] row : results) {
            if (row[0] != null) {
                String key = row[0].toString();
                map.put(key, ((Number) row[1]).longValue());
            }
        }
        return map;
    }
}
