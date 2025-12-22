package com.inboop.backend.lead.repository;

import com.inboop.backend.lead.entity.Message;
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
public interface MessageRepository extends JpaRepository<Message, Long> {

    Optional<Message> findByInstagramMessageId(String instagramMessageId);

    List<Message> findByConversationId(Long conversationId);

    /**
     * Find all messages for conversations belonging to a specific Instagram Business Account.
     */
    @Query("SELECT m FROM Message m WHERE m.conversation.business.instagramBusinessAccountId = :instagramBusinessAccountId")
    List<Message> findAllByBusinessInstagramBusinessAccountId(@Param("instagramBusinessAccountId") String instagramBusinessAccountId);

    /**
     * Count messages for a business.
     * Used for reporting how many records were deleted.
     */
    @Query("SELECT COUNT(m) FROM Message m WHERE m.conversation.business.instagramBusinessAccountId = :instagramBusinessAccountId")
    long countByBusinessInstagramBusinessAccountId(@Param("instagramBusinessAccountId") String instagramBusinessAccountId);

    /**
     * Delete all messages for conversations belonging to a specific Instagram Business Account.
     *
     * META APP REVIEW NOTE:
     * Messages contain the actual DM content (original_text, translated_text) which is
     * the most sensitive PII. These MUST be deleted when the business requests data deletion.
     * Attachment URLs (which may point to user-uploaded content) are also deleted.
     */
    @Modifying
    @Query("DELETE FROM Message m WHERE m.conversation.id IN " +
           "(SELECT c.id FROM Conversation c WHERE c.business.instagramBusinessAccountId = :instagramBusinessAccountId)")
    int deleteByBusinessInstagramBusinessAccountId(@Param("instagramBusinessAccountId") String instagramBusinessAccountId);

    /**
     * Delete messages by conversation IDs.
     * Used when we already have the conversation IDs (more efficient).
     */
    @Modifying
    @Query("DELETE FROM Message m WHERE m.conversation.id IN :conversationIds")
    int deleteByConversationIds(@Param("conversationIds") List<Long> conversationIds);

    // ==================== Analytics Queries ====================

    /**
     * INBOX-04: Count inbound messages within period.
     */
    @Query("SELECT COUNT(m) FROM Message m " +
           "JOIN m.conversation c " +
           "WHERE c.deletedAt IS NULL " +
           "AND (:businessId IS NULL OR c.business.id = :businessId) " +
           "AND (m.direction = 'INBOUND' OR m.isFromCustomer = true) " +
           "AND m.sentAt >= :periodStart AND m.sentAt < :periodEnd")
    long countInboundMessages(
            @Param("businessId") Long businessId,
            @Param("periodStart") LocalDateTime periodStart,
            @Param("periodEnd") LocalDateTime periodEnd);

    /**
     * INBOX-05: Count outbound messages within period.
     */
    @Query("SELECT COUNT(m) FROM Message m " +
           "JOIN m.conversation c " +
           "WHERE c.deletedAt IS NULL " +
           "AND (:businessId IS NULL OR c.business.id = :businessId) " +
           "AND (m.direction = 'OUTBOUND' OR m.isFromCustomer = false) " +
           "AND m.sentAt >= :periodStart AND m.sentAt < :periodEnd")
    long countOutboundMessages(
            @Param("businessId") Long businessId,
            @Param("periodStart") LocalDateTime periodStart,
            @Param("periodEnd") LocalDateTime periodEnd);

    /**
     * SLA-01: Calculate average first response time in seconds.
     * Returns the average time difference between first inbound and first outbound message per conversation.
     */
    @Query(value = "SELECT AVG(EXTRACT(EPOCH FROM (first_out.sent_at - first_in.sent_at))) " +
           "FROM conversations c " +
           "JOIN ( " +
           "  SELECT conversation_id, MIN(sent_at) as sent_at " +
           "  FROM messages " +
           "  WHERE (direction = 'INBOUND' OR is_from_customer = true) " +
           "  GROUP BY conversation_id " +
           ") first_in ON c.id = first_in.conversation_id " +
           "JOIN ( " +
           "  SELECT conversation_id, MIN(sent_at) as sent_at " +
           "  FROM messages " +
           "  WHERE (direction = 'OUTBOUND' OR is_from_customer = false) " +
           "  GROUP BY conversation_id " +
           ") first_out ON c.id = first_out.conversation_id " +
           "WHERE c.deleted_at IS NULL " +
           "AND (:businessId IS NULL OR c.business_id = :businessId) " +
           "AND c.started_at >= :periodStart AND c.started_at < :periodEnd " +
           "AND first_out.sent_at > first_in.sent_at",
           nativeQuery = true)
    Double avgFirstResponseTimeSeconds(
            @Param("businessId") Long businessId,
            @Param("periodStart") LocalDateTime periodStart,
            @Param("periodEnd") LocalDateTime periodEnd);
}
