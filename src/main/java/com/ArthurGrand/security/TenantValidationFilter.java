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
    public TenantValidationFilter(
                                  TenantStatusValidator tenantStatusValidator,
                                  TenantCacheService tenantCacheService,
                                  JwtUtil jwtUtil) {
        this.tenantStatusValidator = tenantStatusValidator;
        this.tenantCacheService=tenantCacheService;
        this.jwtUtil=jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        System.out.println("🔍 Processing request: " + path);

        // Skip tenant validation for Swagger and tenant creation
        if (path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs")
                || path.equals("/swagger-ui.html")
                || path.startsWith("/api/v1/tenants/createTenant")) {
            System.out.println("⏭️  Skipping tenant validation for path: " + path);
            filterChain.doFilter(request, response);
            return;
        }

        String userType = request.getHeader("X-User-Type");
        String tenantId = request.getHeader("X-Tenant-ID");

        System.out.println("🔍 User-Type: " + userType);
        System.out.println("🔍 Tenant-ID: " + tenantId);

        if (userType == null || userType.isEmpty()) {
            System.out.println("❌ X-User-Type header is missing");
            handleException(response, "X-User-Type header is missing", HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        // Master user — allow request but do NOT set tenant context
        if ("MASTER".equalsIgnoreCase(userType)) {
            System.out.println("👑 Master user detected - no tenant context will be set");
            filterChain.doFilter(request, response);
            return;
        }

        // Tenant user — tenant ID is required
        if (tenantId == null || tenantId.isEmpty()) {
            System.out.println("❌ Tenant ID is missing for non-master user");
            handleException(response, "Tenant ID is missing for non-master user", HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        // 🚫 Block clients from accessing the master DB directly
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

        // ✅ Set tenant context using database name
        System.out.println("✅ Setting tenant context to: " + tenant.getDatabaseName());
        TenantContext.setCurrentTenant(tenant.getDatabaseName());

        // 🔐 Extract employeeId from JWT
        Integer employeeId = null;
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7); // remove "Bearer "
            try {
                Claims claims = jwtUtil.parseToken(token);
                employeeId = claims.get("employeeId", Integer.class);
                System.out.println("👤 Extracted employeeId from token: " + employeeId);
            } catch (Exception e) {
                System.out.println("⚠️ Failed to parse JWT token: " + e.getMessage());
            }
        }

        UserSessionDto sessionDto = new UserSessionDto();
        sessionDto.setTenantId(tenant.getTenantId());
        sessionDto.setDomain(tenant.getDomain());
        sessionDto.setTimezone(tenant.getTimezone());
        sessionDto.setEmployeeId(employeeId);

        TenantContext.setUserSession(sessionDto);
        try {
            System.out.println("🔄 Proceeding with request for tenant: " + tenant.getDatabaseName());
            filterChain.doFilter(request, response);
        } finally {
            System.out.println("🧹 Clearing tenant context for: " + tenant.getDatabaseName());
            TenantContext.clear(); // Always clear after the request
        }
    }

    private void handleException(HttpServletResponse response, String message, int status) throws IOException {
        System.out.println("💥 Exception: " + message + " (Status: " + status + ")");
        response.setStatus(status);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\": \"" + message + "\"}");
    }
}