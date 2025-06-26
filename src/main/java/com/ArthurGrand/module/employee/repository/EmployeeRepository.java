package com.ArthurGrand.module.employee.repository;

import com.ArthurGrand.module.department.entity.Department;
import com.ArthurGrand.module.employee.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee,Integer> {
    boolean existsByEmailId(String emailid);
    Optional<Employee> findByEmailId(String email);

    // Get all employees by department ID
    List<Employee> findByDepartmentId(Integer departmentId);
    Page<Employee> findByIsDeleteFalse(Pageable pageable);

    // Fetch only active (non-deleted) employees
    List<Employee> findByIsDeleteFalse();

    // Fetch non-deleted employees by department
    List<Employee> findByDepartmentIdAndIsDeleteFalse(Integer departmentId);


}
