package com.ArthurGrand.common.component;

import com.ArthurGrand.admin.tenants.context.TenantContext;
import com.ArthurGrand.admin.tenants.entity.Tenant;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;


@Component
public class TenantStatusValidator {
    public boolean validate(Tenant tenant, HttpServletResponse response) throws IOException {
        switch (tenant.getStatus()) {
            case ACTIVE:
                TenantContext.setCurrentTenant(tenant.getDatabaseName());
                return true;

            case INACTIVE:
                respond(response, "Tenant is inactive. Please contact master admin for approval.", HttpServletResponse.SC_FORBIDDEN);
                return false;

            case EXPIRED:
                respond(response, "Tenant subscription has expired. Please renew your plan.", HttpServletResponse.SC_PAYMENT_REQUIRED);
                return false;

            default:
                respond(response, "Unknown tenant status", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                return false;
        }
    }

    private void respond(HttpServletResponse response, String message, int status) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\": \"" + message + "\"}");
    }

}
