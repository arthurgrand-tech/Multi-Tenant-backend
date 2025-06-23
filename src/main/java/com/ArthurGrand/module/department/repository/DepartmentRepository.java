package com.ArthurGrand.module.department.repository;

import com.ArthurGrand.module.department.entity.Department;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Integer> {

    @Query("SELECT DISTINCT d FROM Department d LEFT JOIN FETCH d.departmentLeads WHERE d.isDeleted = false")
    Page<Department> findDepartmentsWithEmployees(Pageable pageable);
    @Query("SELECT new map(d.departmentId as id, d.departmentName as name, d.departmentLead as lead, SIZE(d.departmentLeads) as employeeCount) " +
            "FROM Department d")
    List<Map<String, Object>> getDepartmentSummary();
    @Query("SELECT new map(SIZE(d.departmentLeads) as employeeCount, COUNT(d) as departmentCount) " +
            "FROM Department d GROUP BY SIZE(d.departmentLeads)")
    List<Map<String, Object>> countByEmployeeSize();
    @Query("SELECT COUNT(DISTINCT d.departmentName) FROM Department d")
    Long countDistinctDepartmentNames();
    @Query("SELECT COUNT(DISTINCT d.departmentLead) FROM Department d")
    Long countDistinctDepartmentLeads();
}