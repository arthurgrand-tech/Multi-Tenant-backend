package com.ArthurGrand.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CacheConfig {
    @Bean
    public Caffeine<Object, Object> caffeineConfig() {
        return Caffeine.newBuilder()
                //.expireAfterWrite(30, TimeUnit.MINUTES) // TTL of 30 minutes
                .maximumSize(500)                      // Max 500 tenants in cache
                .recordStats();                        // Optional: for metrics
    }

    @Bean
    public CacheManager cacheManager(Caffeine<Object, Object> caffeine) {
        CaffeineCacheManager manager = new CaffeineCacheManager("tenants");
        manager.setCaffeine(caffeine);
        return manager;
    }
}
