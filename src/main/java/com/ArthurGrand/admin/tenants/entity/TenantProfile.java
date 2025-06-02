package com.ArthurGrand.admin.tenants.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class TenantProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "tenant_id", nullable = false, unique = true)
    private Tenant tenant;

    private String contactPerson;
    private String email;
    private String phoneNumber;
    private String address;
    private String website;
}
