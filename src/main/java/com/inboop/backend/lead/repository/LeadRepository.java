package com.inboop.backend.lead.repository;

import com.inboop.backend.lead.entity.Lead;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
    @Query("SELECT l FROM Lead l WHERE l.business.instagramBusinessId = :instagramBusinessId")
    List<Lead> findAllByBusinessInstagramBusinessId(@Param("instagramBusinessId") String instagramBusinessId);

    /**
     * Get IDs of all leads for a business.
     * Used for efficient bulk deletion of lead_labels.
     */
    @Query("SELECT l.id FROM Lead l WHERE l.business.instagramBusinessId = :instagramBusinessId")
    List<Long> findIdsByBusinessInstagramBusinessId(@Param("instagramBusinessId") String instagramBusinessId);

    /**
     * Delete all leads for a specific Instagram Business Account.
     *
     * META APP REVIEW NOTE:
     * Leads contain customer PII (Instagram username, customer name, profile picture)
     * and must be deleted when the business requests data deletion.
     * The lead_labels collection table entries are deleted via cascade.
     */
    @Modifying
    @Query("DELETE FROM Lead l WHERE l.business.instagramBusinessId = :instagramBusinessId")
    int deleteByBusinessInstagramBusinessId(@Param("instagramBusinessId") String instagramBusinessId);
}
