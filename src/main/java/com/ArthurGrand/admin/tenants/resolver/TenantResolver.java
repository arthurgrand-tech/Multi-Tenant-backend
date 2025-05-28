package com.ArthurGrand.admin.tenants.resolver;

import com.ArthurGrand.admin.tenants.entity.Tenant;
import com.ArthurGrand.admin.tenants.repository.TenantRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class TenantResolver {

    private final TenantRepository tenantRepository;
    public TenantResolver(TenantRepository tenantRepository){
        this.tenantRepository=tenantRepository;
    }
    public String resolveTenantId(HttpServletRequest request) {
        // Extract domain from request
        String domain = extractDomain(request);

        // Find tenant by domain
        Optional<Tenant> tenant = tenantRepository.findByDomain(domain);

        return tenant.map(Tenant::getDatabaseName).orElse(null);
    }

    private String extractDomain(HttpServletRequest request) {
        // Extract domain from URL path: /v2/{domain}/timesheet
        String path = request.getRequestURI();
        String[] parts = path.split("/");

        if (parts.length >= 3 && "v2".equals(parts[1])) {
            return parts[2];
        }

        return null;
    }
}