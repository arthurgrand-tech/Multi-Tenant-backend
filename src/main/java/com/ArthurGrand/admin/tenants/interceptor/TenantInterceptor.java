//package com.ArthurGrand.admin.tenants.interceptor;
//
//import com.ArthurGrand.admin.tenants.context.TenantContext;
//import com.ArthurGrand.admin.tenants.resolver.TenantResolver;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//import org.springframework.web.servlet.HandlerInterceptor;
//
//@Component
//public class TenantInterceptor implements HandlerInterceptor {
//
//    @Autowired
//    private TenantResolver tenantResolver;
//
//    @Override
//    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
//        String tenantId = tenantResolver.resolveTenantId(request);
//
//        if (request.getRequestURI().startsWith("/admin")) {
//            TenantContext.setCurrentTenant("master");
//            return true;
//        }
//
//        if (tenantId == null) {
//            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
//            return false;
//        }
//
//        TenantContext.setCurrentTenant(tenantId);
//        return true;
//    }
//
//    @Override
//    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
//                                Object handler, Exception ex) {
//        TenantContext.clear();
//    }
//}
