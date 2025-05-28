package com.ArthurGrand.security;

import com.ArthurGrand.admin.tenants.context.TenantContext;
import com.ArthurGrand.admin.tenants.entity.Tenant;
import com.ArthurGrand.admin.tenants.repository.TenantRepository;
import com.ArthurGrand.common.enums.TenantStatus;
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

    private final TenantRepository tenantRepository;

    public TenantValidationFilter(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // Skip tenant validation for Swagger UI and API docs
        if (path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs") || path.equals("/swagger-ui.html")) {
            filterChain.doFilter(request, response);
            return;
        }

        String userType = request.getHeader("X-User-Type"); // e.g. MASTER or TENANT

        if (userType == null || userType.isEmpty()) {
            handleException(response, "X-User-Type header is missing", HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        if ("MASTER".equalsIgnoreCase(userType)) {
            // MASTER user — allow all requests without tenant check
            filterChain.doFilter(request, response);
            return;
        }

        // For other users (e.g. TENANT), tenant id is mandatory
        String tenantId = request.getHeader("X-Tenant-ID");
        if (tenantId == null || tenantId.isEmpty()) {
            handleException(response, "Tenant ID is missing for non-master user", HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        // Validate tenant id exists and is active
        Optional<Tenant> optionalTenant = tenantRepository.findByDomain(tenantId);

        if (optionalTenant.isEmpty()) {
            handleException(response, "Tenant not found", HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        Tenant tenant = optionalTenant.get();

        if (tenant.getStatus() != TenantStatus.ACTIVE) {
            handleException(response, "Tenant is not active", HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        // *** Set TenantContext using tenantId (string) — this must match MultiTenantDataSource key ***
        TenantContext.setCurrentTenant(tenant.getDatabaseName());

        System.out.println("TenantContext set to tenantId: " + tenantId);
        System.out.println("Tenant ID set in context: " + TenantContext.getCurrentTenant());

        try {
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear(); // Always clear the context to avoid leakage
        }
    }

    private void handleException(HttpServletResponse response, String message, int status) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\": \"" + message + "\"}");
    }
}
