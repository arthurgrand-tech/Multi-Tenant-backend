package com.ArthurGrand.module.employee.entity;

import com.ArthurGrand.common.enums.EmployeeStatus;
import com.ArthurGrand.module.department.entity.Department;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Employee {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 255, unique = true)
    private String employeeId;

    @Column(length = 255, unique = true,nullable = false)
    private String firstname;

    @Column(length = 255)
    private String lastname;

    @Column(length = 255, unique = true, nullable = false)
    private String emailId;

    @Column(nullable = false)
    private String password;

    @Column(length = 255)
    private String contactNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmployeeStatus employeeStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    @JsonBackReference
    private Department department;
}