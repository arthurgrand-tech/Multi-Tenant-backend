package com.ArthurGrand.module.employee.entity;

import com.ArthurGrand.common.enums.EmployeeStatus;
import com.ArthurGrand.module.department.entity.Department;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Employee {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "employees_id" , length = 255)
    private String employeeId;

    @Column(name = "first_name" , length = 255, nullable = false)
    private String firstname;

    @Column(name = "last_name" , length = 255)
    private String lastname;

    @Column(name = "email_id" , length = 255, unique = true, nullable = false)
    private String emailId;

    @Column(nullable = false)
    private String password;

    @Column(name = "contact_number" , length = 255)
    private Long contactNumber;

    private EmployeeStatus employeeStatus;

    @ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinColumn(name="department_id", nullable=true)
    private Department department;

}