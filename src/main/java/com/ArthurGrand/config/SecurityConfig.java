package com.ArthurGrand.config;

import com.ArthurGrand.security.CustomUserDetailsService;
import com.ArthurGrand.security.JwtAuthFilter;
import com.ArthurGrand.security.TenantValidationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthFilter filter;
    private final CustomUserDetailsService customUserDetailsService;
    private final TenantValidationFilter tenantValidationFilter;
    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter filter,
                          CustomUserDetailsService customUserDetailsService,
                          TenantValidationFilter tenantValidationFilter,
                          JwtAuthFilter jwtAuthFilter){
        this.filter=filter;
        this.customUserDetailsService=customUserDetailsService;
        this.tenantValidationFilter=tenantValidationFilter;
        this.jwtAuthFilter=jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)  // Then add JWT filter
                .addFilterBefore(tenantValidationFilter, UsernamePasswordAuthenticationFilter.class)  // Add tenant validation filter first
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/v1/auth/login","/api/v1/tenants/**","/api/v1/employee/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",        // <- required
                                "/api-docs/**",
                                "/swagger-resources/**",
                                "/webjars/**"// <- if using custom path
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authProvider())
                .addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public DaoAuthenticationProvider authProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(customUserDetailsService);
        authProvider.setPasswordEncoder(new BCryptPasswordEncoder());
        return authProvider;
    }

    // Expose AuthenticationManager as a Spring Bean
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    // You can add your password encoder here too
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}