package com.ArthurGrand.module.employee.repository;

import com.ArthurGrand.module.department.entity.Department;
import com.ArthurGrand.module.employee.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee,Integer> {
    boolean existsByEmailId(String emailid);
    Optional<Employee> findByEmailId(String email);

    Optional<List<Employee>> findByDepartment(Department department);
    // Get all employees by department ID
    List<Employee> findByDepartmentId(Integer departmentId);

}
