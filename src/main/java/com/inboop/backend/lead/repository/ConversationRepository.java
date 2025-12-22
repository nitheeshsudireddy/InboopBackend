package com.inboop.backend.lead.repository;

import com.inboop.backend.lead.entity.Conversation;
import com.inboop.backend.lead.enums.ChannelType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    Optional<Conversation> findByInstagramConversationId(String instagramConversationId);

    List<Conversation> findByBusinessId(Long businessId);

    /**
     * Find an existing conversation between a business and a customer.
     * Used to find or create conversations when processing webhook messages.
     */
    Optional<Conversation> findByBusinessIdAndInstagramScopedUserId(Long businessId, String instagramScopedUserId);

    /**
     * Find all conversations for a specific Instagram Business Account.
     * Used for data deletion - we traverse Business -> Conversations.
     */
    @Query("SELECT c FROM Conversation c WHERE c.business.instagramBusinessAccountId = :instagramBusinessAccountId")
    List<Conversation> findAllByBusinessInstagramBusinessAccountId(@Param("instagramBusinessAccountId") String instagramBusinessAccountId);

    /**
     * Get IDs of all conversations for a business.
     * Used for efficient bulk deletion of messages.
     */
    @Query("SELECT c.id FROM Conversation c WHERE c.business.instagramBusinessAccountId = :instagramBusinessAccountId")
    List<Long> findIdsByBusinessInstagramBusinessAccountId(@Param("instagramBusinessAccountId") String instagramBusinessAccountId);

    /**
     * Delete all conversations for a specific Instagram Business Account.
     *
     * META APP REVIEW NOTE:
     * Conversations contain customer PII (handle, name, profile picture) and
     * must be deleted when the business requests data deletion.
     */
    @Modifying
    @Query("DELETE FROM Conversation c WHERE c.business.instagramBusinessAccountId = :instagramBusinessAccountId")
    int deleteByBusinessInstagramBusinessAccountId(@Param("instagramBusinessAccountId") String instagramBusinessAccountId);

    // ==================== Analytics Queries ====================

    /**
     * INBOX-01: Count active conversations (snapshot).
     */
    @Query("SELECT COUNT(c) FROM Conversation c WHERE c.deletedAt IS NULL " +
           "AND c.isActive = true " +
           "AND (:businessId IS NULL OR c.business.id = :businessId)")
    long countActiveConversations(@Param("businessId") Long businessId);

    /**
     * INBOX-02: Sum of unread messages across active conversations (snapshot).
     */
    @Query("SELECT COALESCE(SUM(c.unreadCount), 0) FROM Conversation c WHERE c.deletedAt IS NULL " +
           "AND c.isActive = true " +
           "AND (:businessId IS NULL OR c.business.id = :businessId)")
    long sumUnreadMessages(@Param("businessId") Long businessId);

    /**
     * INBOX-03: Count conversations started within period.
     */
    @Query("SELECT COUNT(c) FROM Conversation c WHERE c.deletedAt IS NULL " +
           "AND (:businessId IS NULL OR c.business.id = :businessId) " +
           "AND (:channel IS NULL OR c.channel = :channel) " +
           "AND c.startedAt >= :periodStart AND c.startedAt < :periodEnd")
    long countConversationsStarted(
            @Param("businessId") Long businessId,
            @Param("channel") ChannelType channel,
            @Param("periodStart") LocalDateTime periodStart,
            @Param("periodEnd") LocalDateTime periodEnd);

    /**
     * INBOX-06: Count conversations by channel within period.
     */
    @Query("SELECT c.channel, COUNT(c) FROM Conversation c WHERE c.deletedAt IS NULL " +
           "AND (:businessId IS NULL OR c.business.id = :businessId) " +
           "AND c.startedAt >= :periodStart AND c.startedAt < :periodEnd " +
           "GROUP BY c.channel")
    List<Object[]> countConversationsByChannel(
            @Param("businessId") Long businessId,
            @Param("periodStart") LocalDateTime periodStart,
            @Param("periodEnd") LocalDateTime periodEnd);

    /**
     * SLA-02: Count active conversations with unread messages but no outbound response.
     */
    @Query("SELECT COUNT(DISTINCT c.id) FROM Conversation c " +
           "WHERE c.deletedAt IS NULL " +
           "AND c.isActive = true " +
           "AND c.unreadCount > 0 " +
           "AND (:businessId IS NULL OR c.business.id = :businessId) " +
           "AND NOT EXISTS (SELECT 1 FROM Message m WHERE m.conversation.id = c.id " +
           "AND (m.direction = 'OUTBOUND' OR m.isFromCustomer = false))")
    long countConversationsWithoutResponse(@Param("businessId") Long businessId);
}
