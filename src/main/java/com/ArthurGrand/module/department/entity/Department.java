package com.ArthurGrand.module.department.entity;

import com.ArthurGrand.module.employee.entity.Employee;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Data
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 255,unique = true,nullable = false)
    private String departmentName;

    @Column(length = 255)
    private String departmentLead;

    @OneToMany(mappedBy = "department", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    private Set<Employee> employees = new HashSet<>();

    @Column(name = "is_deleted")
    private Boolean isDeleted = false;
}