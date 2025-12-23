package com.inboop.backend.rbac;

import com.inboop.backend.auth.entity.User;
import com.inboop.backend.workspace.entity.Workspace;
import com.inboop.backend.workspace.entity.WorkspaceMember;
import com.inboop.backend.workspace.enums.WorkspaceRole;
import com.inboop.backend.workspace.repository.WorkspaceMemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Central service for Role-Based Access Control (RBAC) enforcement.
 * Single source of truth for permission checks across the application.
 */
@Service
@Transactional(readOnly = true)
public class RbacService {

    private final WorkspaceMemberRepository memberRepository;

    public RbacService(WorkspaceMemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    /**
     * Check if a user has a specific permission in a workspace.
     *
     * @param user The user to check
     * @param workspaceId The workspace context
     * @param permission The permission to check
     * @return true if the user has the permission, false otherwise
     */
    public boolean hasPermission(User user, Long workspaceId, Permission permission) {
        if (user == null || workspaceId == null || permission == null) {
            return false;
        }

        Optional<WorkspaceMember> memberOpt = memberRepository.findByWorkspaceIdAndUserId(workspaceId, user.getId());
        if (memberOpt.isEmpty()) {
            return false;
        }

        WorkspaceRole role = memberOpt.get().getRole();
        return role.hasPermission(permission);
    }

    /**
     * Get the user's role in a workspace.
     *
     * @param user The user
     * @param workspaceId The workspace
     * @return The user's role, or empty if not a member
     */
    public Optional<WorkspaceRole> getUserRole(User user, Long workspaceId) {
        if (user == null || workspaceId == null) {
            return Optional.empty();
        }

        return memberRepository.findByWorkspaceIdAndUserId(workspaceId, user.getId())
                .map(WorkspaceMember::getRole);
    }

    /**
     * Assert that a user has a specific permission, or throw RbacException.
     *
     * @param user The user to check
     * @param workspaceId The workspace context
     * @param permission The required permission
     * @throws RbacException if the user lacks the permission
     */
    public void assertPermission(User user, Long workspaceId, Permission permission) {
        if (!hasPermission(user, workspaceId, permission)) {
            throw RbacException.forbidden(permission);
        }
    }

    /**
     * Assert that a user can send messages (not a VIEWER).
     */
    public void assertCanSendMessage(User user, Long workspaceId) {
        if (!hasPermission(user, workspaceId, Permission.CONVERSATION_SEND)) {
            throw RbacException.viewerCannotWrite();
        }
    }

    /**
     * Assert that a user can write leads (not a VIEWER).
     */
    public void assertCanWriteLead(User user, Long workspaceId) {
        if (!hasPermission(user, workspaceId, Permission.LEAD_WRITE)) {
            throw RbacException.viewerCannotWrite();
        }
    }

    /**
     * Assert that a user can assign leads (not a VIEWER).
     */
    public void assertCanAssignLead(User user, Long workspaceId) {
        if (!hasPermission(user, workspaceId, Permission.LEAD_ASSIGN)) {
            throw RbacException.viewerCannotWrite();
        }
    }

    /**
     * Assert that a user can write orders (not a VIEWER).
     */
    public void assertCanWriteOrder(User user, Long workspaceId) {
        if (!hasPermission(user, workspaceId, Permission.ORDER_WRITE)) {
            throw RbacException.viewerCannotWrite();
        }
    }

    /**
     * Assert that a user can assign orders (not a VIEWER).
     */
    public void assertCanAssignOrder(User user, Long workspaceId) {
        if (!hasPermission(user, workspaceId, Permission.ORDER_ASSIGN)) {
            throw RbacException.viewerCannotWrite();
        }
    }

    /**
     * Assert that a user can manage team (must be ADMIN).
     */
    public void assertCanManageTeam(User user, Long workspaceId) {
        if (!hasPermission(user, workspaceId, Permission.TEAM_MANAGE)) {
            throw RbacException.teamManageRequired();
        }
    }

    /**
     * Check if user is a member of the workspace.
     */
    public boolean isMember(User user, Long workspaceId) {
        if (user == null || workspaceId == null) {
            return false;
        }
        return memberRepository.existsByWorkspaceIdAndUserId(workspaceId, user.getId());
    }

    /**
     * Check if user is an admin in the workspace.
     */
    public boolean isAdmin(User user, Long workspaceId) {
        return hasPermission(user, workspaceId, Permission.TEAM_MANAGE);
    }

    /**
     * Check if user can read in the workspace (any role).
     */
    public boolean canRead(User user, Long workspaceId) {
        return isMember(user, workspaceId);
    }

    /**
     * Check if user can write in the workspace (EDITOR or ADMIN).
     */
    public boolean canWrite(User user, Long workspaceId) {
        return hasPermission(user, workspaceId, Permission.LEAD_WRITE);
    }

    /**
     * Get the user's primary workspace ID.
     * For now, returns the first workspace the user is a member of.
     *
     * @param user The user
     * @return The workspace ID, or null if user has no workspace
     */
    public Long getUserWorkspaceId(User user) {
        if (user == null) {
            return null;
        }
        List<WorkspaceMember> memberships = memberRepository.findByUserId(user.getId());
        if (memberships.isEmpty()) {
            return null;
        }
        // Return first workspace (primary)
        return memberships.get(0).getWorkspace().getId();
    }

    /**
     * Get the user's primary workspace.
     * For now, returns the first workspace the user is a member of.
     *
     * @param user The user
     * @return The workspace, or empty if user has no workspace
     */
    public Optional<Workspace> getUserWorkspace(User user) {
        if (user == null) {
            return Optional.empty();
        }
        List<WorkspaceMember> memberships = memberRepository.findByUserId(user.getId());
        if (memberships.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(memberships.get(0).getWorkspace());
    }
}
