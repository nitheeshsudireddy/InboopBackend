package com.inboop.backend.lead.repository;

import com.inboop.backend.lead.entity.Lead;
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
public interface LeadRepository extends JpaRepository<Lead, Long> {

    List<Lead> findByBusinessId(Long businessId);

    /**
     * Find an existing lead for a customer in a business.
     * Used to find or create leads when processing webhook messages.
     */
    Optional<Lead> findByBusinessIdAndInstagramUserId(Long businessId, String instagramUserId);

    List<Lead> findByConversationId(Long conversationId);

    /**
     * Find all leads for a specific Instagram Business Account.
     */
    @Query("SELECT l FROM Lead l WHERE l.business.instagramBusinessAccountId = :instagramBusinessAccountId")
    List<Lead> findAllByBusinessInstagramBusinessAccountId(@Param("instagramBusinessAccountId") String instagramBusinessAccountId);

    /**
     * Get IDs of all leads for a business.
     * Used for efficient bulk deletion of lead_labels.
     */
    @Query("SELECT l.id FROM Lead l WHERE l.business.instagramBusinessAccountId = :instagramBusinessAccountId")
    List<Long> findIdsByBusinessInstagramBusinessAccountId(@Param("instagramBusinessAccountId") String instagramBusinessAccountId);

    /**
     * Delete all leads for a specific Instagram Business Account.
     *
     * META APP REVIEW NOTE:
     * Leads contain customer PII (Instagram username, customer name, profile picture)
     * and must be deleted when the business requests data deletion.
     * The lead_labels collection table entries are deleted via cascade.
     */
    @Modifying
    @Query("DELETE FROM Lead l WHERE l.business.instagramBusinessAccountId = :instagramBusinessAccountId")
    int deleteByBusinessInstagramBusinessAccountId(@Param("instagramBusinessAccountId") String instagramBusinessAccountId);

    // ==================== Analytics Queries ====================

    /**
     * LEAD-01: Count leads created within period.
     */
    @Query("SELECT COUNT(l) FROM Lead l WHERE l.deletedAt IS NULL " +
           "AND (:businessId IS NULL OR l.business.id = :businessId) " +
           "AND (:channel IS NULL OR l.channel = :channel) " +
           "AND l.createdAt >= :periodStart AND l.createdAt < :periodEnd")
    long countLeadsCreated(
            @Param("businessId") Long businessId,
            @Param("channel") ChannelType channel,
            @Param("periodStart") LocalDateTime periodStart,
            @Param("periodEnd") LocalDateTime periodEnd);

    /**
     * LEAD-02: Count leads converted within period.
     */
    @Query("SELECT COUNT(l) FROM Lead l WHERE l.deletedAt IS NULL " +
           "AND l.status = 'CONVERTED' " +
           "AND (:businessId IS NULL OR l.business.id = :businessId) " +
           "AND (:channel IS NULL OR l.channel = :channel) " +
           "AND l.convertedAt >= :periodStart AND l.convertedAt < :periodEnd")
    long countLeadsConverted(
            @Param("businessId") Long businessId,
            @Param("channel") ChannelType channel,
            @Param("periodStart") LocalDateTime periodStart,
            @Param("periodEnd") LocalDateTime periodEnd);

    /**
     * LEAD-03: Count leads lost within period (uses updatedAt as proxy).
     */
    @Query("SELECT COUNT(l) FROM Lead l WHERE l.deletedAt IS NULL " +
           "AND l.status = 'LOST' " +
           "AND (:businessId IS NULL OR l.business.id = :businessId) " +
           "AND (:channel IS NULL OR l.channel = :channel) " +
           "AND l.updatedAt >= :periodStart AND l.updatedAt < :periodEnd")
    long countLeadsLost(
            @Param("businessId") Long businessId,
            @Param("channel") ChannelType channel,
            @Param("periodStart") LocalDateTime periodStart,
            @Param("periodEnd") LocalDateTime periodEnd);

    /**
     * LEAD-04: Count leads closed within period (uses updatedAt as proxy).
     */
    @Query("SELECT COUNT(l) FROM Lead l WHERE l.deletedAt IS NULL " +
           "AND l.status = 'CLOSED' " +
           "AND (:businessId IS NULL OR l.business.id = :businessId) " +
           "AND (:channel IS NULL OR l.channel = :channel) " +
           "AND l.updatedAt >= :periodStart AND l.updatedAt < :periodEnd")
    long countLeadsClosed(
            @Param("businessId") Long businessId,
            @Param("channel") ChannelType channel,
            @Param("periodStart") LocalDateTime periodStart,
            @Param("periodEnd") LocalDateTime periodEnd);

    /**
     * LEAD-06: Count open leads (snapshot).
     */
    @Query("SELECT COUNT(l) FROM Lead l WHERE l.deletedAt IS NULL " +
           "AND l.status = 'NEW' " +
           "AND (:businessId IS NULL OR l.business.id = :businessId)")
    long countOpenLeads(@Param("businessId") Long businessId);

    /**
     * LEAD-07: Count leads by channel within period.
     */
    @Query("SELECT l.channel, COUNT(l) FROM Lead l WHERE l.deletedAt IS NULL " +
           "AND (:businessId IS NULL OR l.business.id = :businessId) " +
           "AND l.createdAt >= :periodStart AND l.createdAt < :periodEnd " +
           "GROUP BY l.channel")
    List<Object[]> countLeadsByChannel(
            @Param("businessId") Long businessId,
            @Param("periodStart") LocalDateTime periodStart,
            @Param("periodEnd") LocalDateTime periodEnd);

    /**
     * LEAD-08: Count leads by type within period.
     */
    @Query("SELECT l.type, COUNT(l) FROM Lead l WHERE l.deletedAt IS NULL " +
           "AND (:businessId IS NULL OR l.business.id = :businessId) " +
           "AND l.createdAt >= :periodStart AND l.createdAt < :periodEnd " +
           "GROUP BY l.type")
    List<Object[]> countLeadsByType(
            @Param("businessId") Long businessId,
            @Param("periodStart") LocalDateTime periodStart,
            @Param("periodEnd") LocalDateTime periodEnd);
}
