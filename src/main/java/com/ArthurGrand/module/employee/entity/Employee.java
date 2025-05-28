package com.ArthurGrand.module.employee.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Employee {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer employeeId;

    @Column(name = "first_name" , length = 255, nullable = false)
    private String firstname;

    @Column(name = "last_name" , length = 255)
    private String lastname;

    @Column(name = "email_id" , length = 255, unique = true, nullable = false)
    private String emailid;

    @Column(nullable = false)
    private String password;


}
