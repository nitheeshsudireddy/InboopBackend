package com.inboop.backend.workspace.service;

import com.inboop.backend.auth.entity.User;
import com.inboop.backend.auth.repository.UserRepository;
import com.inboop.backend.plan.enums.Plan;
import com.inboop.backend.plan.exception.PlanLimitException;
import com.inboop.backend.plan.service.PlanService;
import com.inboop.backend.rbac.RbacException;
import com.inboop.backend.rbac.RbacService;
import com.inboop.backend.workspace.dto.InviteUserRequest;
import com.inboop.backend.workspace.dto.UpdateMemberRoleRequest;
import com.inboop.backend.workspace.dto.WorkspaceMemberResponse;
import com.inboop.backend.workspace.entity.Workspace;
import com.inboop.backend.workspace.entity.WorkspaceMember;
import com.inboop.backend.workspace.enums.PlanType;
import com.inboop.backend.workspace.enums.WorkspaceRole;
import com.inboop.backend.workspace.exception.WorkspaceException;
import com.inboop.backend.workspace.repository.WorkspaceMemberRepository;
import com.inboop.backend.workspace.repository.WorkspaceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkspaceServiceTest {

    @Mock
    private WorkspaceRepository workspaceRepository;

    @Mock
    private WorkspaceMemberRepository memberRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PlanService planService;

    @Mock
    private RbacService rbacService;

    @InjectMocks
    private WorkspaceService workspaceService;

    private User adminUser;
    private User memberUser;
    private User viewerUser;
    private User newUser;
    private Workspace workspace;

    @BeforeEach
    void setUp() {
        adminUser = new User();
        adminUser.setId(1L);
        adminUser.setName("Admin User");
        adminUser.setEmail("admin@example.com");

        memberUser = new User();
        memberUser.setId(2L);
        memberUser.setName("Editor User");
        memberUser.setEmail("editor@example.com");

        viewerUser = new User();
        viewerUser.setId(4L);
        viewerUser.setName("Viewer User");
        viewerUser.setEmail("viewer@example.com");

        newUser = new User();
        newUser.setId(3L);
        newUser.setName("New User");
        newUser.setEmail("new@example.com");

        workspace = new Workspace();
        workspace.setId(1L);
        workspace.setName("Test Workspace");
        workspace.setPlan(PlanType.PRO);
        workspace.setOwner(adminUser);
    }

    @Nested
    @DisplayName("Invite User Tests")
    class InviteUserTests {

        @Test
        @DisplayName("Should reject invite when plan user limit reached (PRO = 5 users)")
        void shouldRejectInviteWhenPlanLimitReached() {
            // Given: Workspace has 5 members (Pro limit)
            when(workspaceRepository.findActiveById(1L)).thenReturn(Optional.of(workspace));
            doNothing().when(rbacService).assertCanManageTeam(adminUser, 1L);
            doThrow(PlanLimitException.userLimitReached(Plan.PRO, 5))
                .when(planService).assertCanInviteUser(1L);

            InviteUserRequest request = new InviteUserRequest("new@example.com", WorkspaceRole.EDITOR);

            // When/Then
            PlanLimitException exception = assertThrows(
                PlanLimitException.class,
                () -> workspaceService.inviteUser(1L, request, adminUser)
            );

            assertEquals("PLAN_LIMIT_REACHED", exception.getCode());
            assertTrue(exception.getMessage().contains("5 users"));
            assertEquals(HttpStatus.PAYMENT_REQUIRED, exception.getStatus());
            assertTrue(exception.isUpgradeSuggested());

            // Verify no member was saved
            verify(memberRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should reject invite when non-admin tries to invite (RBAC)")
        void shouldRejectInviteFromNonAdmin() {
            // Given: Non-admin user (Editor) tries to invite
            when(workspaceRepository.findActiveById(1L)).thenReturn(Optional.of(workspace));
            doThrow(RbacException.teamManageRequired())
                .when(rbacService).assertCanManageTeam(memberUser, 1L);

            InviteUserRequest request = new InviteUserRequest("new@example.com", WorkspaceRole.EDITOR);

            // When/Then
            RbacException exception = assertThrows(
                RbacException.class,
                () -> workspaceService.inviteUser(1L, request, memberUser)
            );

            assertEquals("FORBIDDEN", exception.getCode());
            assertTrue(exception.getMessage().contains("Only admins can manage team members"));

            // Verify no member was saved
            verify(memberRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should reject invite when viewer tries to invite (RBAC)")
        void shouldRejectInviteFromViewer() {
            // Given: Viewer user tries to invite
            when(workspaceRepository.findActiveById(1L)).thenReturn(Optional.of(workspace));
            doThrow(RbacException.teamManageRequired())
                .when(rbacService).assertCanManageTeam(viewerUser, 1L);

            InviteUserRequest request = new InviteUserRequest("new@example.com", WorkspaceRole.VIEWER);

            // When/Then
            RbacException exception = assertThrows(
                RbacException.class,
                () -> workspaceService.inviteUser(1L, request, viewerUser)
            );

            assertEquals("FORBIDDEN", exception.getCode());

            // Verify no member was saved
            verify(memberRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should allow invite when admin and under limit")
        void shouldAllowInviteWhenAdminAndUnderLimit() {
            // Given: Admin inviting, under limit
            when(workspaceRepository.findActiveById(1L)).thenReturn(Optional.of(workspace));
            doNothing().when(rbacService).assertCanManageTeam(adminUser, 1L);
            doNothing().when(planService).assertCanInviteUser(1L); // Under limit
            when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.of(newUser));
            when(memberRepository.existsByWorkspaceIdAndUserId(1L, newUser.getId())).thenReturn(false);
            when(memberRepository.save(any(WorkspaceMember.class))).thenAnswer(invocation -> {
                WorkspaceMember m = invocation.getArgument(0);
                m.setId(100L);
                return m;
            });

            InviteUserRequest request = new InviteUserRequest("new@example.com", WorkspaceRole.EDITOR);

            // When
            WorkspaceMemberResponse response = workspaceService.inviteUser(1L, request, adminUser);

            // Then
            assertNotNull(response);
            assertEquals("new@example.com", response.getUserEmail());
            assertEquals(WorkspaceRole.EDITOR, response.getRole());
            verify(memberRepository).save(any(WorkspaceMember.class));
        }
    }

    @Nested
    @DisplayName("Update Member Role Tests")
    class UpdateMemberRoleTests {

        @Test
        @DisplayName("Should prevent demoting last admin")
        void shouldPreventDemotingLastAdmin() {
            // Given: Admin member to demote, and they're the only admin
            WorkspaceMember adminMember = new WorkspaceMember();
            adminMember.setId(10L);
            adminMember.setWorkspace(workspace);
            adminMember.setUser(adminUser);
            adminMember.setRole(WorkspaceRole.ADMIN);

            when(workspaceRepository.findActiveById(1L)).thenReturn(Optional.of(workspace));
            doNothing().when(rbacService).assertCanManageTeam(adminUser, 1L);
            when(memberRepository.findById(10L)).thenReturn(Optional.of(adminMember));
            when(memberRepository.countByWorkspaceIdAndRole(1L, WorkspaceRole.ADMIN)).thenReturn(1L); // Only 1 admin

            UpdateMemberRoleRequest request = new UpdateMemberRoleRequest(WorkspaceRole.EDITOR);

            // When/Then
            WorkspaceException exception = assertThrows(
                WorkspaceException.class,
                () -> workspaceService.updateMemberRole(1L, 10L, request, adminUser)
            );

            assertEquals("MUST_HAVE_ADMIN", exception.getCode());
            assertEquals("Workspace must have at least one admin.", exception.getMessage());
            assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, exception.getStatus());

            // Verify role was not changed
            verify(memberRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should prevent demoting last admin to viewer")
        void shouldPreventDemotingLastAdminToViewer() {
            // Given: Admin member to demote to Viewer, and they're the only admin
            WorkspaceMember adminMember = new WorkspaceMember();
            adminMember.setId(10L);
            adminMember.setWorkspace(workspace);
            adminMember.setUser(adminUser);
            adminMember.setRole(WorkspaceRole.ADMIN);

            when(workspaceRepository.findActiveById(1L)).thenReturn(Optional.of(workspace));
            doNothing().when(rbacService).assertCanManageTeam(adminUser, 1L);
            when(memberRepository.findById(10L)).thenReturn(Optional.of(adminMember));
            when(memberRepository.countByWorkspaceIdAndRole(1L, WorkspaceRole.ADMIN)).thenReturn(1L);

            UpdateMemberRoleRequest request = new UpdateMemberRoleRequest(WorkspaceRole.VIEWER);

            // When/Then
            WorkspaceException exception = assertThrows(
                WorkspaceException.class,
                () -> workspaceService.updateMemberRole(1L, 10L, request, adminUser)
            );

            assertEquals("MUST_HAVE_ADMIN", exception.getCode());
            verify(memberRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should allow demoting admin when multiple admins exist")
        void shouldAllowDemotingWhenMultipleAdmins() {
            // Given: Admin member to demote, but there are 2 admins
            WorkspaceMember adminMember = new WorkspaceMember();
            adminMember.setId(10L);
            adminMember.setWorkspace(workspace);
            adminMember.setUser(memberUser);
            adminMember.setRole(WorkspaceRole.ADMIN);

            when(workspaceRepository.findActiveById(1L)).thenReturn(Optional.of(workspace));
            doNothing().when(rbacService).assertCanManageTeam(adminUser, 1L);
            when(memberRepository.findById(10L)).thenReturn(Optional.of(adminMember));
            when(memberRepository.countByWorkspaceIdAndRole(1L, WorkspaceRole.ADMIN)).thenReturn(2L); // 2 admins
            when(memberRepository.save(any(WorkspaceMember.class))).thenAnswer(invocation -> invocation.getArgument(0));

            UpdateMemberRoleRequest request = new UpdateMemberRoleRequest(WorkspaceRole.EDITOR);

            // When
            WorkspaceMemberResponse response = workspaceService.updateMemberRole(1L, 10L, request, adminUser);

            // Then
            assertEquals(WorkspaceRole.EDITOR, response.getRole());
            verify(memberRepository).save(adminMember);
        }

        @Test
        @DisplayName("Should reject role change by non-admin (RBAC)")
        void shouldRejectRoleChangeByNonAdmin() {
            // Given: Editor trying to change roles
            when(workspaceRepository.findActiveById(1L)).thenReturn(Optional.of(workspace));
            doThrow(RbacException.teamManageRequired())
                .when(rbacService).assertCanManageTeam(memberUser, 1L);

            UpdateMemberRoleRequest request = new UpdateMemberRoleRequest(WorkspaceRole.VIEWER);

            // When/Then
            RbacException exception = assertThrows(
                RbacException.class,
                () -> workspaceService.updateMemberRole(1L, 10L, request, memberUser)
            );

            assertEquals("FORBIDDEN", exception.getCode());
            verify(memberRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Remove Member Tests")
    class RemoveMemberTests {

        @Test
        @DisplayName("Should prevent removing last admin")
        void shouldPreventRemovingLastAdmin() {
            // Given: Only admin in workspace
            WorkspaceMember adminMember = new WorkspaceMember();
            adminMember.setId(10L);
            adminMember.setWorkspace(workspace);
            adminMember.setUser(adminUser);
            adminMember.setRole(WorkspaceRole.ADMIN);

            when(workspaceRepository.findActiveById(1L)).thenReturn(Optional.of(workspace));
            when(memberRepository.findById(10L)).thenReturn(Optional.of(adminMember));
            when(rbacService.isAdmin(adminUser, 1L)).thenReturn(true);
            when(memberRepository.countByWorkspaceIdAndRole(1L, WorkspaceRole.ADMIN)).thenReturn(1L);

            // When/Then
            WorkspaceException exception = assertThrows(
                WorkspaceException.class,
                () -> workspaceService.removeMember(1L, 10L, adminUser)
            );

            assertEquals("MUST_HAVE_ADMIN", exception.getCode());
            verify(memberRepository, never()).delete(any());
        }

        @Test
        @DisplayName("Should reject removal by non-admin (unless removing self)")
        void shouldRejectRemovalByNonAdmin() {
            // Given: Editor trying to remove another member
            WorkspaceMember targetMember = new WorkspaceMember();
            targetMember.setId(10L);
            targetMember.setWorkspace(workspace);
            targetMember.setUser(newUser);
            targetMember.setRole(WorkspaceRole.EDITOR);

            when(workspaceRepository.findActiveById(1L)).thenReturn(Optional.of(workspace));
            when(memberRepository.findById(10L)).thenReturn(Optional.of(targetMember));
            when(rbacService.isAdmin(memberUser, 1L)).thenReturn(false);

            // When/Then
            WorkspaceException exception = assertThrows(
                WorkspaceException.class,
                () -> workspaceService.removeMember(1L, 10L, memberUser)
            );

            assertEquals("ADMIN_REQUIRED", exception.getCode());
            verify(memberRepository, never()).delete(any());
        }

        @Test
        @DisplayName("Should reject removal by viewer (unless removing self)")
        void shouldRejectRemovalByViewer() {
            // Given: Viewer trying to remove another member
            WorkspaceMember targetMember = new WorkspaceMember();
            targetMember.setId(10L);
            targetMember.setWorkspace(workspace);
            targetMember.setUser(newUser);
            targetMember.setRole(WorkspaceRole.EDITOR);

            when(workspaceRepository.findActiveById(1L)).thenReturn(Optional.of(workspace));
            when(memberRepository.findById(10L)).thenReturn(Optional.of(targetMember));
            when(rbacService.isAdmin(viewerUser, 1L)).thenReturn(false);

            // When/Then
            WorkspaceException exception = assertThrows(
                WorkspaceException.class,
                () -> workspaceService.removeMember(1L, 10L, viewerUser)
            );

            assertEquals("ADMIN_REQUIRED", exception.getCode());
            verify(memberRepository, never()).delete(any());
        }

        @Test
        @DisplayName("Should allow user to remove themselves")
        void shouldAllowUserToRemoveSelf() {
            // Given: Editor removing themselves
            WorkspaceMember selfMember = new WorkspaceMember();
            selfMember.setId(10L);
            selfMember.setWorkspace(workspace);
            selfMember.setUser(memberUser);
            selfMember.setRole(WorkspaceRole.EDITOR);

            when(workspaceRepository.findActiveById(1L)).thenReturn(Optional.of(workspace));
            when(memberRepository.findById(10L)).thenReturn(Optional.of(selfMember));
            when(rbacService.isAdmin(memberUser, 1L)).thenReturn(false);
            doNothing().when(memberRepository).delete(selfMember);

            // When/Then - Should not throw
            assertDoesNotThrow(() -> workspaceService.removeMember(1L, 10L, memberUser));
            verify(memberRepository).delete(selfMember);
        }
    }
}
