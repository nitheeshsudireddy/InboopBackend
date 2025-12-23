package com.inboop.backend.rbac;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a user lacks permission to perform an action.
 * Returns 403 Forbidden with a structured error response.
 */
public class RbacException extends RuntimeException {

    private static final String DEFAULT_CODE = "FORBIDDEN";
    private static final String DEFAULT_MESSAGE = "You don't have permission to perform this action.";
    private static final HttpStatus STATUS = HttpStatus.FORBIDDEN;

    private final String code;
    private final Permission requiredPermission;

    public RbacException() {
        super(DEFAULT_MESSAGE);
        this.code = DEFAULT_CODE;
        this.requiredPermission = null;
    }

    public RbacException(String message) {
        super(message);
        this.code = DEFAULT_CODE;
        this.requiredPermission = null;
    }

    public RbacException(Permission requiredPermission) {
        super(DEFAULT_MESSAGE);
        this.code = DEFAULT_CODE;
        this.requiredPermission = requiredPermission;
    }

    public RbacException(Permission requiredPermission, String message) {
        super(message);
        this.code = DEFAULT_CODE;
        this.requiredPermission = requiredPermission;
    }

    public String getCode() {
        return code;
    }

    public HttpStatus getStatus() {
        return STATUS;
    }

    public Permission getRequiredPermission() {
        return requiredPermission;
    }

    /**
     * Factory method for forbidden access.
     */
    public static RbacException forbidden() {
        return new RbacException();
    }

    /**
     * Factory method for forbidden access with specific permission.
     */
    public static RbacException forbidden(Permission permission) {
        return new RbacException(permission);
    }

    /**
     * Factory method for forbidden access with custom message.
     */
    public static RbacException forbidden(String message) {
        return new RbacException(message);
    }

    /**
     * Factory method for viewer trying to perform write action.
     */
    public static RbacException viewerCannotWrite() {
        return new RbacException("Viewers cannot perform this action. Contact an admin to upgrade your role.");
    }

    /**
     * Factory method for non-admin trying to manage team.
     */
    public static RbacException teamManageRequired() {
        return new RbacException(Permission.TEAM_MANAGE, "Only admins can manage team members.");
    }
}
