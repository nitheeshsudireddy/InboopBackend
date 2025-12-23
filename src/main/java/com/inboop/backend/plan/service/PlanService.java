package com.inboop.backend.plan.service;

import com.inboop.backend.plan.entity.WorkspacePlan;
import com.inboop.backend.plan.enums.Feature;
import com.inboop.backend.plan.enums.Plan;
import com.inboop.backend.plan.enums.PlanStatus;
import com.inboop.backend.plan.exception.PlanLimitException;
import com.inboop.backend.plan.repository.WorkspacePlanRepository;
import com.inboop.backend.workspace.entity.Workspace;
import com.inboop.backend.workspace.repository.WorkspaceMemberRepository;
import com.inboop.backend.workspace.repository.WorkspaceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Central service for plan-based feature gating and limit enforcement.
 * All plan checks should go through this service.
 */
@Service
@Transactional(readOnly = true)
public class PlanService {

    private final WorkspacePlanRepository planRepository;
    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository memberRepository;

    public PlanService(
            WorkspacePlanRepository planRepository,
            WorkspaceRepository workspaceRepository,
            WorkspaceMemberRepository memberRepository) {
        this.planRepository = planRepository;
        this.workspaceRepository = workspaceRepository;
        this.memberRepository = memberRepository;
    }

    // ========== Plan Retrieval ==========

    /**
     * Get the current plan for a workspace.
     * Returns FREE if no plan record exists.
     */
    public Plan getPlan(Long workspaceId) {
        return planRepository.findByWorkspaceId(workspaceId)
                .map(WorkspacePlan::getPlan)
                .orElse(Plan.FREE);
    }

    /**
     * Get the full plan details for a workspace.
     */
    public WorkspacePlan getWorkspacePlan(Long workspaceId) {
        return planRepository.findByWorkspaceId(workspaceId)
                .orElseGet(() -> createDefaultPlan(workspaceId));
    }

    /**
     * Check if workspace has an active plan.
     */
    public boolean isActive(Long workspaceId) {
        return planRepository.findByWorkspaceId(workspaceId)
                .map(WorkspacePlan::isActive)
                .orElse(true); // Default to active for FREE plan
    }

    // ========== User Limits ==========

    /**
     * Get the maximum users allowed for a workspace's plan.
     */
    public int maxUsersAllowed(Long workspaceId) {
        return getPlan(workspaceId).getMaxUsers();
    }

    /**
     * Get the maximum users allowed for a workspace.
     */
    public int maxUsersAllowed(Workspace workspace) {
        return maxUsersAllowed(workspace.getId());
    }

    /**
     * Check if the workspace can invite more users.
     */
    public boolean canInviteUser(Long workspaceId) {
        Plan plan = getPlan(workspaceId);
        long currentCount = memberRepository.countByWorkspaceId(workspaceId);
        return currentCount < plan.getMaxUsers();
    }

    /**
     * Check if workspace can invite more users.
     */
    public boolean canInviteUser(Workspace workspace) {
        return canInviteUser(workspace.getId());
    }

    /**
     * Assert that a workspace can invite another user.
     * Throws PlanLimitException if limit is reached.
     */
    public void assertCanInviteUser(Long workspaceId) {
        Plan plan = getPlan(workspaceId);
        long currentCount = memberRepository.countByWorkspaceId(workspaceId);

        if (currentCount >= plan.getMaxUsers()) {
            throw PlanLimitException.userLimitReached(plan, plan.getMaxUsers());
        }
    }

    /**
     * Assert that a workspace can invite another user.
     */
    public void assertCanInviteUser(Workspace workspace) {
        assertCanInviteUser(workspace.getId());
    }

    // ========== Feature Gating ==========

    /**
     * Check if a feature is enabled for a workspace.
     */
    public boolean isFeatureEnabled(Long workspaceId, Feature feature) {
        Plan plan = getPlan(workspaceId);
        Plan requiredPlan = feature.getMinimumPlan();

        // Compare plan ordinals (FREE < PRO < ENTERPRISE)
        return plan.ordinal() >= requiredPlan.ordinal();
    }

    /**
     * Check if a feature is enabled for a workspace.
     */
    public boolean isFeatureEnabled(Workspace workspace, Feature feature) {
        return isFeatureEnabled(workspace.getId(), feature);
    }

    /**
     * Assert that a feature is enabled for a workspace.
     * Throws PlanLimitException if feature is not available.
     */
    public void assertFeatureEnabled(Long workspaceId, Feature feature) {
        Plan plan = getPlan(workspaceId);
        Plan requiredPlan = feature.getMinimumPlan();

        if (plan.ordinal() < requiredPlan.ordinal()) {
            throw PlanLimitException.featureNotAvailable(feature, plan, requiredPlan);
        }
    }

    /**
     * Assert that a feature is enabled for a workspace.
     */
    public void assertFeatureEnabled(Workspace workspace, Feature feature) {
        assertFeatureEnabled(workspace.getId(), feature);
    }

    // ========== Plan Status ==========

    /**
     * Assert that the plan is active (not expired or suspended).
     */
    public void assertPlanActive(Long workspaceId) {
        WorkspacePlan workspacePlan = planRepository.findByWorkspaceId(workspaceId).orElse(null);

        if (workspacePlan == null) {
            return; // No plan record means FREE tier, always active
        }

        if (workspacePlan.getPlanStatus() == PlanStatus.SUSPENDED) {
            throw PlanLimitException.planSuspended(workspacePlan.getPlan());
        }

        if (workspacePlan.getPlanStatus() == PlanStatus.EXPIRED) {
            throw PlanLimitException.planExpired(workspacePlan.getPlan());
        }

        if (workspacePlan.getExpiresAt() != null &&
            workspacePlan.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw PlanLimitException.planExpired(workspacePlan.getPlan());
        }
    }

    // ========== Plan Info ==========

    /**
     * Get available seats (max users - current users).
     */
    public int getAvailableSeats(Long workspaceId) {
        int maxUsers = maxUsersAllowed(workspaceId);
        long currentCount = memberRepository.countByWorkspaceId(workspaceId);
        return Math.max(0, maxUsers - (int) currentCount);
    }

    /**
     * Get current seat usage info.
     */
    public SeatInfo getSeatInfo(Long workspaceId) {
        int maxUsers = maxUsersAllowed(workspaceId);
        long currentCount = memberRepository.countByWorkspaceId(workspaceId);
        return new SeatInfo(
            (int) currentCount,
            maxUsers,
            Math.max(0, maxUsers - (int) currentCount)
        );
    }

    // ========== Plan Management (Internal) ==========

    /**
     * Create a default plan for a workspace (used for new workspaces).
     */
    @Transactional
    public WorkspacePlan createDefaultPlan(Long workspaceId) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new IllegalArgumentException("Workspace not found: " + workspaceId));

        WorkspacePlan plan = new WorkspacePlan();
        plan.setWorkspace(workspace);
        plan.setPlan(Plan.FREE);
        plan.setPlanStatus(PlanStatus.ACTIVE);
        plan.setStartedAt(LocalDateTime.now());

        return planRepository.save(plan);
    }

    /**
     * Simple record for seat usage info.
     */
    public record SeatInfo(int used, int max, int available) {}
}
