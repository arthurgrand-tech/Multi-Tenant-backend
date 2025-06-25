package com.ArthurGrand.security;

import com.ArthurGrand.admin.dto.UserSessionDto;
import com.ArthurGrand.admin.tenants.context.TenantContext;
import com.ArthurGrand.admin.tenants.entity.Tenant;
import com.ArthurGrand.admin.tenants.serviceImp.TenantCacheService;
import com.ArthurGrand.common.component.JwtUtil;
import com.ArthurGrand.common.component.TenantStatusValidator;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Component
public class TenantValidationFilter extends OncePerRequestFilter {

    private final TenantStatusValidator tenantStatusValidator;
    private final TenantCacheService tenantCacheService;
    private final JwtUtil jwtUtil;

    public TenantValidationFilter(TenantStatusValidator tenantStatusValidator,
                                  TenantCacheService tenantCacheService,
                                  JwtUtil jwtUtil) {
        this.tenantStatusValidator = tenantStatusValidator;
        this.tenantCacheService = tenantCacheService;
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        System.out.println("🔍 Processing request: " + path);

        // Skip tenant validation for public endpoints
        if (shouldSkipValidation(path)) {
            System.out.println("⏭️ Skipping tenant validation for path: " + path);
            // Set master context for public endpoints
            TenantContext.setCurrentTenant("master");
            try {
                filterChain.doFilter(request, response);
            } finally {
                TenantContext.clear();
            }
            return;
        }

        String userType = request.getHeader("X-User-Type");
        String tenantId = request.getHeader("X-Tenant-ID");

        System.out.println("🔍 User-Type: " + userType);
        System.out.println("🔍 Tenant-ID: " + tenantId);

        if (userType == null || userType.isEmpty()) {
            System.out.println("❌ X-User-Type header is missing");
            handleException(response, "X-User-Type header is required", HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        // Master user - use master database
        if ("MASTER".equalsIgnoreCase(userType)) {
            System.out.println("👑 Master user detected - using master database");
            TenantContext.setCurrentTenant("master");

            // Extract employee ID from JWT for master user
            Integer employeeId = extractEmployeeIdFromJWT(request);
            if (employeeId != null) {
                UserSessionDto sessionDto = new UserSessionDto();
                sessionDto.setEmployeeId(employeeId);
                sessionDto.setTenantId(0); // Master tenant ID
                sessionDto.setDomain("master");
                sessionDto.setTimezone("UTC");
                TenantContext.setUserSession(sessionDto);
            }

            try {
                filterChain.doFilter(request, response);
            } finally {
                TenantContext.clear();
            }
            return;
        }

        // Tenant user - tenant ID is required
        if (tenantId == null || tenantId.isEmpty()) {
            System.out.println("❌ Tenant ID is missing for tenant user");
            handleException(response, "X-Tenant-ID header is required for tenant users", HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        // Block direct access to master database via tenant header
        if ("master".equalsIgnoreCase(tenantId)) {
            System.out.println("🚫 Blocked attempt to access master database via tenant header");
            handleException(response, "Access to master database via tenant header is forbidden", HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        System.out.println("🔍 Looking for tenant with domain: " + tenantId);
        Optional<Tenant> optionalTenant = tenantCacheService.getTenantByDomain(tenantId);
        System.out.println("🔍 Tenant found: " + optionalTenant.isPresent());

        if (optionalTenant.isEmpty()) {
            System.out.println("❌ No tenant found for domain: " + tenantId);
            handleException(response, "Tenant not found", HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        Tenant tenant = optionalTenant.get();
        System.out.println("🔍 Found tenant: " + tenant.getCompanyName() + " (ID: " + tenant.getTenantId() + ")");
        System.out.println("🔍 Tenant database name: " + tenant.getDatabaseName());
        System.out.println("🔍 Tenant status: " + tenant.getStatus());

        if (!tenantStatusValidator.validate(tenant, response)) {
            System.out.println("❌ Tenant validation failed for: " + tenant.getDatabaseName());
            return;
        }

        // Set tenant context
        System.out.println("✅ Setting tenant context to: " + tenant.getDatabaseName());
        TenantContext.setCurrentTenant(tenant.getDatabaseName());

        // Extract employeeId from JWT
        Integer employeeId = extractEmployeeIdFromJWT(request);

        UserSessionDto sessionDto = new UserSessionDto();
        sessionDto.setTenantId(tenant.getTenantId());
        sessionDto.setDomain(tenant.getDomain());
        sessionDto.setTimezone(tenant.getTimezone());
        if (employeeId != null) {
            sessionDto.setEmployeeId(employeeId);
        }

        TenantContext.setUserSession(sessionDto);

        // Debug logging for payment endpoints
        if (path.startsWith("/api/v1/payment")) {
            System.out.println("💳 Payment endpoint - Tenant context: " + TenantContext.getCurrentTenant());
            System.out.println("💳 User session: " + TenantContext.getUserSession());
        }

        try {
            System.out.println("🔄 Proceeding with request for tenant: " + tenant.getDatabaseName());
            filterChain.doFilter(request, response);
        } finally {
            System.out.println("🧹 Clearing tenant context for: " + tenant.getDatabaseName());
            TenantContext.clear();
        }
    }

    private boolean shouldSkipValidation(String path) {
        return path.startsWith("/swagger-ui") ||
                path.startsWith("/v3/api-docs") ||
                path.equals("/swagger-ui.html") ||
                path.startsWith("/api/v1/tenants/createTenant") ||
                path.startsWith("/api/v1/tenants/") && path.endsWith("/activate") ||
                path.equals("/api/v1/payment/webhook") ||
                path.equals("/api/v1/auth/login") ||
                path.startsWith("/webjars/") ||
                path.startsWith("/swagger-resources/");
    }

    private Integer extractEmployeeIdFromJWT(HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                Claims claims = jwtUtil.parseToken(token);
                Integer employeeId = claims.get("employeeId", Integer.class);
                System.out.println("👤 Extracted employeeId from token: " + employeeId);
                return employeeId;
            }
        } catch (Exception e) {
            System.out.println("⚠️ Failed to parse JWT token: " + e.getMessage());
        }
        return null;
    }

    private void handleException(HttpServletResponse response, String message, int status) throws IOException {
        System.out.println("💥 Exception: " + message + " (Status: " + status + ")");
        response.setStatus(status);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\": \"" + message + "\"}");
    }
}