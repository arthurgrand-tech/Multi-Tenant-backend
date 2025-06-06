package com.ArthurGrand.admin.tenants.entity;

import com.ArthurGrand.common.enums.TenantStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Entity
@Data
public class Tenant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(unique = true, nullable = false)
    private String domain;

    @Column(unique = true, nullable = false)
    private String companyName;

    @Column(unique = true, nullable = false)
    private String databaseName;

    // Indicates if tenant is using their own DB server (true) or provider's shared server (false)
    @Column(nullable = false)
    private boolean usesCustomDb = false;

    // Optional fields if tenant uses their own DB server
    private String dbHost;       // e.g., 192.168.1.10
    private Integer dbPort;      // e.g., 3306
    private String dbUsername;
    private String dbPassword;

    @Column(unique = true, nullable = false)
    private String adminEmail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TenantStatus status = TenantStatus.INACTIVE;

    @Column(nullable = false)
    private String timezone;  // e.g., "Asia/Kolkata"

    @Column(nullable = false)
    private String country;   // e.g., "IN"

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @PrePersist
    public void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = Instant.now();
    }

    /**
     * Constructs the JDBC URL dynamically based on tenant DB source.
     */
    public String getJdbcUrl(String providerHost, int providerPort) {
        String host = usesCustomDb ? dbHost : providerHost;
        int port = usesCustomDb ? dbPort : providerPort;
        return "jdbc:mysql://" + host + ":" + port + "/" + databaseName + "?useSSL=false&serverTimezone=UTC";
    }
    public String getJdbcUrlFromBaseUrl(String baseUrl) {
        // baseUrl is like: jdbc:mysql://localhost:3306/master
        // Replace DB name with tenant DB name
        return baseUrl.replaceFirst("/[^/?]+", "/" + this.databaseName);
    }
}