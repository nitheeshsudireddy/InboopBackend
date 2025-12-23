package com.inboop.backend.rbac;

import com.inboop.backend.auth.entity.User;
import com.inboop.backend.workspace.entity.Workspace;
import com.inboop.backend.workspace.entity.WorkspaceMember;
import com.inboop.backend.workspace.enums.WorkspaceRole;
import com.inboop.backend.workspace.repository.WorkspaceMemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RbacService Tests")
class RbacServiceTest {

    @Mock
    private WorkspaceMemberRepository memberRepository;

    @InjectMocks
    private RbacService rbacService;

    private User adminUser;
    private User editorUser;
    private User viewerUser;
    private Workspace workspace;

    @BeforeEach
    void setUp() {
        adminUser = new User();
        adminUser.setId(1L);
        adminUser.setName("Admin User");
        adminUser.setEmail("admin@example.com");

        editorUser = new User();
        editorUser.setId(2L);
        editorUser.setName("Editor User");
        editorUser.setEmail("editor@example.com");

        viewerUser = new User();
        viewerUser.setId(3L);
        viewerUser.setName("Viewer User");
        viewerUser.setEmail("viewer@example.com");

        workspace = new Workspace();
        workspace.setId(1L);
        workspace.setName("Test Workspace");
    }

    private WorkspaceMember createMember(User user, WorkspaceRole role) {
        WorkspaceMember member = new WorkspaceMember();
        member.setId(user.getId() * 10);
        member.setWorkspace(workspace);
        member.setUser(user);
        member.setRole(role);
        return member;
    }

    @Nested
    @DisplayName("Permission Checks")
    class PermissionChecks {

        @Test
        @DisplayName("Admin has all permissions")
        void adminHasAllPermissions() {
            WorkspaceMember adminMember = createMember(adminUser, WorkspaceRole.ADMIN);
            when(memberRepository.findByWorkspaceIdAndUserId(1L, 1L)).thenReturn(Optional.of(adminMember));

            assertTrue(rbacService.hasPermission(adminUser, 1L, Permission.CONVERSATION_READ));
            assertTrue(rbacService.hasPermission(adminUser, 1L, Permission.CONVERSATION_SEND));
            assertTrue(rbacService.hasPermission(adminUser, 1L, Permission.LEAD_READ));
            assertTrue(rbacService.hasPermission(adminUser, 1L, Permission.LEAD_WRITE));
            assertTrue(rbacService.hasPermission(adminUser, 1L, Permission.ORDER_READ));
            assertTrue(rbacService.hasPermission(adminUser, 1L, Permission.ORDER_WRITE));
            assertTrue(rbacService.hasPermission(adminUser, 1L, Permission.TEAM_MANAGE));
        }

        @Test
        @DisplayName("Editor has read/write permissions but not team manage")
        void editorHasReadWritePermissions() {
            WorkspaceMember editorMember = createMember(editorUser, WorkspaceRole.EDITOR);
            when(memberRepository.findByWorkspaceIdAndUserId(1L, 2L)).thenReturn(Optional.of(editorMember));

            assertTrue(rbacService.hasPermission(editorUser, 1L, Permission.CONVERSATION_READ));
            assertTrue(rbacService.hasPermission(editorUser, 1L, Permission.CONVERSATION_SEND));
            assertTrue(rbacService.hasPermission(editorUser, 1L, Permission.LEAD_READ));
            assertTrue(rbacService.hasPermission(editorUser, 1L, Permission.LEAD_WRITE));
            assertTrue(rbacService.hasPermission(editorUser, 1L, Permission.ORDER_READ));
            assertTrue(rbacService.hasPermission(editorUser, 1L, Permission.ORDER_WRITE));
            assertFalse(rbacService.hasPermission(editorUser, 1L, Permission.TEAM_MANAGE));
        }

        @Test
        @DisplayName("Viewer has read-only permissions")
        void viewerHasReadOnlyPermissions() {
            WorkspaceMember viewerMember = createMember(viewerUser, WorkspaceRole.VIEWER);
            when(memberRepository.findByWorkspaceIdAndUserId(1L, 3L)).thenReturn(Optional.of(viewerMember));

            assertTrue(rbacService.hasPermission(viewerUser, 1L, Permission.CONVERSATION_READ));
            assertFalse(rbacService.hasPermission(viewerUser, 1L, Permission.CONVERSATION_SEND));
            assertTrue(rbacService.hasPermission(viewerUser, 1L, Permission.LEAD_READ));
            assertFalse(rbacService.hasPermission(viewerUser, 1L, Permission.LEAD_WRITE));
            assertTrue(rbacService.hasPermission(viewerUser, 1L, Permission.ORDER_READ));
            assertFalse(rbacService.hasPermission(viewerUser, 1L, Permission.ORDER_WRITE));
            assertFalse(rbacService.hasPermission(viewerUser, 1L, Permission.TEAM_MANAGE));
        }

        @Test
        @DisplayName("Non-member has no permissions")
        void nonMemberHasNoPermissions() {
            when(memberRepository.findByWorkspaceIdAndUserId(1L, 3L)).thenReturn(Optional.empty());

            assertFalse(rbacService.hasPermission(viewerUser, 1L, Permission.CONVERSATION_READ));
            assertFalse(rbacService.hasPermission(viewerUser, 1L, Permission.LEAD_READ));
            assertFalse(rbacService.hasPermission(viewerUser, 1L, Permission.ORDER_READ));
        }
    }

    @Nested
    @DisplayName("Viewer Cannot Write Tests")
    class ViewerCannotWriteTests {

        @Test
        @DisplayName("Viewer cannot send messages")
        void viewerCannotSendMessage() {
            WorkspaceMember viewerMember = createMember(viewerUser, WorkspaceRole.VIEWER);
            when(memberRepository.findByWorkspaceIdAndUserId(1L, 3L)).thenReturn(Optional.of(viewerMember));

            RbacException exception = assertThrows(
                    RbacException.class,
                    () -> rbacService.assertCanSendMessage(viewerUser, 1L)
            );

            assertEquals("FORBIDDEN", exception.getCode());
            assertTrue(exception.getMessage().contains("Viewers cannot perform this action"));
        }

        @Test
        @DisplayName("Viewer cannot create lead")
        void viewerCannotCreateLead() {
            WorkspaceMember viewerMember = createMember(viewerUser, WorkspaceRole.VIEWER);
            when(memberRepository.findByWorkspaceIdAndUserId(1L, 3L)).thenReturn(Optional.of(viewerMember));

            RbacException exception = assertThrows(
                    RbacException.class,
                    () -> rbacService.assertCanWriteLead(viewerUser, 1L)
            );

            assertEquals("FORBIDDEN", exception.getCode());
        }

        @Test
        @DisplayName("Viewer cannot create order")
        void viewerCannotCreateOrder() {
            WorkspaceMember viewerMember = createMember(viewerUser, WorkspaceRole.VIEWER);
            when(memberRepository.findByWorkspaceIdAndUserId(1L, 3L)).thenReturn(Optional.of(viewerMember));

            RbacException exception = assertThrows(
                    RbacException.class,
                    () -> rbacService.assertCanWriteOrder(viewerUser, 1L)
            );

            assertEquals("FORBIDDEN", exception.getCode());
        }

        @Test
        @DisplayName("Viewer cannot assign leads")
        void viewerCannotAssignLead() {
            WorkspaceMember viewerMember = createMember(viewerUser, WorkspaceRole.VIEWER);
            when(memberRepository.findByWorkspaceIdAndUserId(1L, 3L)).thenReturn(Optional.of(viewerMember));

            RbacException exception = assertThrows(
                    RbacException.class,
                    () -> rbacService.assertCanAssignLead(viewerUser, 1L)
            );

            assertEquals("FORBIDDEN", exception.getCode());
        }
    }

    @Nested
    @DisplayName("Editor Can Write Tests")
    class EditorCanWriteTests {

        @Test
        @DisplayName("Editor can send messages")
        void editorCanSendMessage() {
            WorkspaceMember editorMember = createMember(editorUser, WorkspaceRole.EDITOR);
            when(memberRepository.findByWorkspaceIdAndUserId(1L, 2L)).thenReturn(Optional.of(editorMember));

            assertDoesNotThrow(() -> rbacService.assertCanSendMessage(editorUser, 1L));
        }

        @Test
        @DisplayName("Editor can create lead")
        void editorCanCreateLead() {
            WorkspaceMember editorMember = createMember(editorUser, WorkspaceRole.EDITOR);
            when(memberRepository.findByWorkspaceIdAndUserId(1L, 2L)).thenReturn(Optional.of(editorMember));

            assertDoesNotThrow(() -> rbacService.assertCanWriteLead(editorUser, 1L));
        }

        @Test
        @DisplayName("Editor can create order")
        void editorCanCreateOrder() {
            WorkspaceMember editorMember = createMember(editorUser, WorkspaceRole.EDITOR);
            when(memberRepository.findByWorkspaceIdAndUserId(1L, 2L)).thenReturn(Optional.of(editorMember));

            assertDoesNotThrow(() -> rbacService.assertCanWriteOrder(editorUser, 1L));
        }

        @Test
        @DisplayName("Editor can assign orders")
        void editorCanAssignOrder() {
            WorkspaceMember editorMember = createMember(editorUser, WorkspaceRole.EDITOR);
            when(memberRepository.findByWorkspaceIdAndUserId(1L, 2L)).thenReturn(Optional.of(editorMember));

            assertDoesNotThrow(() -> rbacService.assertCanAssignOrder(editorUser, 1L));
        }
    }

    @Nested
    @DisplayName("Team Management Tests")
    class TeamManagementTests {

        @Test
        @DisplayName("Admin can manage team")
        void adminCanManageTeam() {
            WorkspaceMember adminMember = createMember(adminUser, WorkspaceRole.ADMIN);
            when(memberRepository.findByWorkspaceIdAndUserId(1L, 1L)).thenReturn(Optional.of(adminMember));

            assertDoesNotThrow(() -> rbacService.assertCanManageTeam(adminUser, 1L));
        }

        @Test
        @DisplayName("Editor cannot manage team")
        void editorCannotManageTeam() {
            WorkspaceMember editorMember = createMember(editorUser, WorkspaceRole.EDITOR);
            when(memberRepository.findByWorkspaceIdAndUserId(1L, 2L)).thenReturn(Optional.of(editorMember));

            RbacException exception = assertThrows(
                    RbacException.class,
                    () -> rbacService.assertCanManageTeam(editorUser, 1L)
            );

            assertEquals("FORBIDDEN", exception.getCode());
            assertEquals(Permission.TEAM_MANAGE, exception.getRequiredPermission());
            assertTrue(exception.getMessage().contains("Only admins can manage team members"));
        }

        @Test
        @DisplayName("Viewer cannot manage team")
        void viewerCannotManageTeam() {
            WorkspaceMember viewerMember = createMember(viewerUser, WorkspaceRole.VIEWER);
            when(memberRepository.findByWorkspaceIdAndUserId(1L, 3L)).thenReturn(Optional.of(viewerMember));

            RbacException exception = assertThrows(
                    RbacException.class,
                    () -> rbacService.assertCanManageTeam(viewerUser, 1L)
            );

            assertEquals("FORBIDDEN", exception.getCode());
        }
    }

    @Nested
    @DisplayName("User Workspace Tests")
    class UserWorkspaceTests {

        @Test
        @DisplayName("Get user workspace ID returns first workspace")
        void getUserWorkspaceIdReturnsFirst() {
            WorkspaceMember member = createMember(adminUser, WorkspaceRole.ADMIN);
            when(memberRepository.findByUserId(1L)).thenReturn(List.of(member));

            Long workspaceId = rbacService.getUserWorkspaceId(adminUser);

            assertEquals(1L, workspaceId);
        }

        @Test
        @DisplayName("Get user workspace ID returns null when no workspaces")
        void getUserWorkspaceIdReturnsNullWhenNoWorkspaces() {
            when(memberRepository.findByUserId(1L)).thenReturn(List.of());

            Long workspaceId = rbacService.getUserWorkspaceId(adminUser);

            assertNull(workspaceId);
        }

        @Test
        @DisplayName("Get user workspace ID returns null for null user")
        void getUserWorkspaceIdReturnsNullForNullUser() {
            Long workspaceId = rbacService.getUserWorkspaceId(null);

            assertNull(workspaceId);
        }
    }
}
