package com.inboop.backend.business.repository;

import com.inboop.backend.business.entity.Business;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BusinessRepository extends JpaRepository<Business, Long> {

    Optional<Business> findByInstagramBusinessAccountId(String instagramBusinessAccountId);

    Optional<Business> findByFacebookPageId(String facebookPageId);

    List<Business> findByOwnerId(Long ownerId);

    List<Business> findByFacebookUserId(String facebookUserId);

    /**
     * Find all businesses associated with an Instagram Business Account ID.
     * Used for Meta data deletion - we need to find all businesses to delete their data.
     */
    @Query("SELECT b FROM Business b WHERE b.instagramBusinessAccountId = :instagramBusinessAccountId")
    List<Business> findAllByInstagramBusinessAccountId(@Param("instagramBusinessAccountId") String instagramBusinessAccountId);

    /**
     * Anonymize business data for GDPR compliance.
     * Sets access tokens to null and marks business as inactive.
     *
     * META APP REVIEW NOTE:
     * Instead of hard deleting businesses (which would break referential integrity),
     * we anonymize the data by removing PII (access tokens) and deactivating.
     */
    @Modifying
    @Query("UPDATE Business b SET b.accessToken = null, b.tokenExpiresAt = null, " +
           "b.instagramUsername = 'DELETED', b.isActive = false, b.webhookVerified = false " +
           "WHERE b.instagramBusinessAccountId = :instagramBusinessAccountId")
    int anonymizeByInstagramBusinessAccountId(@Param("instagramBusinessAccountId") String instagramBusinessAccountId);
}
