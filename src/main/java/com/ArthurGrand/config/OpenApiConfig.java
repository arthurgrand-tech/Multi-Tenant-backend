package com.ArthurGrand.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    private static final String SECURITY_SCHEME_NAME = "bearerAuth";
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                // Add security requirement globally for all headers
                .addSecurityItem(new SecurityRequirement()
                        .addList("bearerAuth")
                        .addList("X-Tenant-ID")
                        .addList("X-User-Type"))
                .components(new Components()
                        // Bearer token scheme
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .name("Authorization")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT"))
                        // X-Tenant-ID as API key header
                        .addSecuritySchemes("X-Tenant-ID", new SecurityScheme()
                                .name("X-Tenant-ID")
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER))
                        // X-User-Type as API key header
                        .addSecuritySchemes("X-User-Type", new SecurityScheme()
                                .name("X-User-Type")
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER))
                );
    }
}