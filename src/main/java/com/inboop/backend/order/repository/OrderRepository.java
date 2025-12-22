package com.inboop.backend.order.repository;

import com.inboop.backend.lead.enums.ChannelType;
import com.inboop.backend.order.entity.Order;
import com.inboop.backend.order.enums.OrderStatus;
import com.inboop.backend.order.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByBusinessId(Long businessId);

    List<Order> findByLeadId(Long leadId);

    /**
     * Find order by idempotency key for duplicate prevention.
     */
    Optional<Order> findByIdempotencyKey(String idempotencyKey);

    /**
     * Count orders for a conversation.
     */
    long countByConversationId(Long conversationId);

    /**
     * Find all orders for a specific Instagram Business Account.
     */
    @Query("SELECT o FROM Order o WHERE o.business.instagramBusinessAccountId = :instagramBusinessAccountId")
    List<Order> findAllByBusinessInstagramBusinessAccountId(@Param("instagramBusinessAccountId") String instagramBusinessAccountId);

    /**
     * Count orders for a business.
     */
    @Query("SELECT COUNT(o) FROM Order o WHERE o.business.instagramBusinessAccountId = :instagramBusinessAccountId")
    long countByBusinessInstagramBusinessAccountId(@Param("instagramBusinessAccountId") String instagramBusinessAccountId);

    /**
     * Anonymize order customer data for GDPR compliance.
     *
     * META APP REVIEW NOTE:
     * Orders have legal/financial significance and may need to be retained for accounting.
     * Instead of deleting orders, we anonymize customer PII:
     * - customer_name -> 'DELETED'
     * - customer_phone -> null
     * - customer_address -> null
     * - lead_id -> null (break link to deleted lead)
     *
     * This approach:
     * - Maintains order records for business reporting and accounting
     * - Removes all personally identifiable information
     * - Complies with data deletion requirements while preserving aggregated analytics
     */
    @Modifying
    @Query("UPDATE Order o SET o.customerName = 'DELETED', o.customerPhone = null, " +
           "o.customerAddress = null, o.lead = null " +
           "WHERE o.business.instagramBusinessAccountId = :instagramBusinessAccountId")
    int anonymizeByBusinessInstagramBusinessAccountId(@Param("instagramBusinessAccountId") String instagramBusinessAccountId);

    // ==================== Analytics Queries ====================

    /**
     * ORD-01: Count orders created within period.
     */
    @Query("SELECT COUNT(o) FROM Order o WHERE o.deletedAt IS NULL " +
           "AND (:businessId IS NULL OR o.business.id = :businessId) " +
           "AND (:channel IS NULL OR o.channel = :channel) " +
           "AND o.createdAt >= :periodStart AND o.createdAt < :periodEnd")
    long countOrdersCreated(
            @Param("businessId") Long businessId,
            @Param("channel") ChannelType channel,
            @Param("periodStart") LocalDateTime periodStart,
            @Param("periodEnd") LocalDateTime periodEnd);

    /**
     * ORD-02: Count orders delivered within period.
     */
    @Query("SELECT COUNT(o) FROM Order o WHERE o.deletedAt IS NULL " +
           "AND o.status = 'DELIVERED' " +
           "AND (:businessId IS NULL OR o.business.id = :businessId) " +
           "AND (:channel IS NULL OR o.channel = :channel) " +
           "AND o.deliveredAt >= :periodStart AND o.deliveredAt < :periodEnd")
    long countOrdersDelivered(
            @Param("businessId") Long businessId,
            @Param("channel") ChannelType channel,
            @Param("periodStart") LocalDateTime periodStart,
            @Param("periodEnd") LocalDateTime periodEnd);

    /**
     * ORD-03: Count orders cancelled within period.
     */
    @Query("SELECT COUNT(o) FROM Order o WHERE o.deletedAt IS NULL " +
           "AND o.status = 'CANCELLED' " +
           "AND (:businessId IS NULL OR o.business.id = :businessId) " +
           "AND (:channel IS NULL OR o.channel = :channel) " +
           "AND o.cancelledAt >= :periodStart AND o.cancelledAt < :periodEnd")
    long countOrdersCancelled(
            @Param("businessId") Long businessId,
            @Param("channel") ChannelType channel,
            @Param("periodStart") LocalDateTime periodStart,
            @Param("periodEnd") LocalDateTime periodEnd);

    /**
     * ORD-05: Count orders by channel within period.
     */
    @Query("SELECT o.channel, COUNT(o) FROM Order o WHERE o.deletedAt IS NULL " +
           "AND (:businessId IS NULL OR o.business.id = :businessId) " +
           "AND o.createdAt >= :periodStart AND o.createdAt < :periodEnd " +
           "GROUP BY o.channel")
    List<Object[]> countOrdersByChannel(
            @Param("businessId") Long businessId,
            @Param("periodStart") LocalDateTime periodStart,
            @Param("periodEnd") LocalDateTime periodEnd);

    /**
     * ORD-06: Count orders by status (snapshot).
     */
    @Query("SELECT o.status, COUNT(o) FROM Order o WHERE o.deletedAt IS NULL " +
           "AND (:businessId IS NULL OR o.business.id = :businessId) " +
           "GROUP BY o.status")
    List<Object[]> countOrdersByStatus(@Param("businessId") Long businessId);

    /**
     * REV-01: Sum gross revenue (paid orders) within period.
     */
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.deletedAt IS NULL " +
           "AND o.paymentStatus = 'PAID' " +
           "AND (:businessId IS NULL OR o.business.id = :businessId) " +
           "AND (:channel IS NULL OR o.channel = :channel) " +
           "AND o.paidAt >= :periodStart AND o.paidAt < :periodEnd")
    BigDecimal sumGrossRevenue(
            @Param("businessId") Long businessId,
            @Param("channel") ChannelType channel,
            @Param("periodStart") LocalDateTime periodStart,
            @Param("periodEnd") LocalDateTime periodEnd);

    /**
     * REV-02: Sum refunded amount within period.
     */
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.deletedAt IS NULL " +
           "AND o.paymentStatus = 'REFUNDED' " +
           "AND (:businessId IS NULL OR o.business.id = :businessId) " +
           "AND (:channel IS NULL OR o.channel = :channel) " +
           "AND o.updatedAt >= :periodStart AND o.updatedAt < :periodEnd")
    BigDecimal sumRefundedAmount(
            @Param("businessId") Long businessId,
            @Param("channel") ChannelType channel,
            @Param("periodStart") LocalDateTime periodStart,
            @Param("periodEnd") LocalDateTime periodEnd);

    /**
     * REV-04: Average order value for paid orders within period.
     */
    @Query("SELECT AVG(o.totalAmount) FROM Order o WHERE o.deletedAt IS NULL " +
           "AND o.paymentStatus = 'PAID' " +
           "AND (:businessId IS NULL OR o.business.id = :businessId) " +
           "AND (:channel IS NULL OR o.channel = :channel) " +
           "AND o.paidAt >= :periodStart AND o.paidAt < :periodEnd")
    BigDecimal avgOrderValue(
            @Param("businessId") Long businessId,
            @Param("channel") ChannelType channel,
            @Param("periodStart") LocalDateTime periodStart,
            @Param("periodEnd") LocalDateTime periodEnd);
}
