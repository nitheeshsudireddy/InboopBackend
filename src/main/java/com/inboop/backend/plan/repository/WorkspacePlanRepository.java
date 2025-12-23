package com.inboop.backend.plan.repository;

import com.inboop.backend.plan.entity.WorkspacePlan;
import com.inboop.backend.plan.enums.Plan;
import com.inboop.backend.plan.enums.PlanStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface WorkspacePlanRepository extends JpaRepository<WorkspacePlan, Long> {

    /**
     * Find the plan for a workspace.
     */
    Optional<WorkspacePlan> findByWorkspaceId(Long workspaceId);

    /**
     * Find all plans expiring before a given date.
     */
    List<WorkspacePlan> findByExpiresAtBeforeAndPlanStatus(LocalDateTime date, PlanStatus status);

    /**
     * Find all workspaces on a specific plan.
     */
    List<WorkspacePlan> findByPlan(Plan plan);

    /**
     * Check if a workspace has an active plan.
     */
    @Query("SELECT CASE WHEN COUNT(wp) > 0 THEN true ELSE false END FROM WorkspacePlan wp " +
           "WHERE wp.workspace.id = :workspaceId " +
           "AND wp.planStatus = 'ACTIVE' " +
           "AND (wp.expiresAt IS NULL OR wp.expiresAt > CURRENT_TIMESTAMP)")
    boolean hasActivePlan(@Param("workspaceId") Long workspaceId);
}
