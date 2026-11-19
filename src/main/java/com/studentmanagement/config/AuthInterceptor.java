package com.studentmanagement.config;

import com.studentmanagement.entity.Role;
import com.studentmanagement.exception.ForbiddenException;
import com.studentmanagement.exception.UnauthorizedException;
import com.studentmanagement.util.TokenUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Enforces role-based access control on /api/students/** endpoints:
 * - ADMIN: full access (GET, POST, PUT, DELETE)
 * - TEACHER: read, create, and update access (GET, POST, PUT); cannot DELETE
 * - STUDENT: read-only access (GET); cannot POST, PUT, or DELETE
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final TokenUtil tokenUtil;
    private final boolean requireAuth;

    public AuthInterceptor(TokenUtil tokenUtil,
                           @Value("${app.security.require-auth:false}") boolean requireAuth) {
        this.tokenUtil = tokenUtil;
        this.requireAuth = requireAuth;
    }

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request,
                             @NonNull HttpServletResponse response,
                             @NonNull Object handler) {
        String method = request.getMethod();
        if ("OPTIONS".equalsIgnoreCase(method)) {
            return true;
        }

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || authHeader.isBlank()) {
            if (requireAuth) {
                throw new UnauthorizedException("Authentication required. Please log in.");
            }
            return true;
        }

        TokenUtil.TokenPayload payload = tokenUtil.verifyToken(authHeader);
        Role role = payload.role();

        if ("DELETE".equalsIgnoreCase(method) && role != Role.ADMIN) {
            throw new ForbiddenException("Only administrators are permitted to delete student records");
        }

        if (("POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method))
                && role != Role.ADMIN && role != Role.TEACHER) {
            throw new ForbiddenException("Students have view-only access and cannot create or modify student records");
        }

        return true;
    }
}
