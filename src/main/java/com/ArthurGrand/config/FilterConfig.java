package com.ArthurGrand.config;

import com.ArthurGrand.security.TenantValidationFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {
    @Bean
    public FilterRegistrationBean<TenantValidationFilter> tenantFilter(TenantValidationFilter filter) {
        FilterRegistrationBean<TenantValidationFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(filter);
        registrationBean.setOrder(1); // Make sure it runs early
        registrationBean.addUrlPatterns("/*");
        return registrationBean;
    }
}
