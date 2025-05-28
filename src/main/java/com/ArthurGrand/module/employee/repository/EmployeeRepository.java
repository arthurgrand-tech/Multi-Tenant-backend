package com.ArthurGrand.module.employee.repository;

import com.ArthurGrand.module.employee.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee,Integer> {
    boolean existsByEmailid(String emailid);
    Optional<Employee> findByEmailid(String email);
}
