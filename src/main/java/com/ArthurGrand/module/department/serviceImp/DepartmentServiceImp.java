package com.ArthurGrand.module.department.serviceImp;

import com.ArthurGrand.common.exception.DepartmentNotFoundException;
import com.ArthurGrand.dto.DepartmentDto;
import com.ArthurGrand.dto.DepartmentSimplifyDto;
import com.ArthurGrand.dto.DropDownDto;
import com.ArthurGrand.module.department.entity.Department;
import com.ArthurGrand.module.department.repository.DepartmentRepository;
import com.ArthurGrand.module.department.service.DepartmentService;
import com.ArthurGrand.module.employee.entity.Employee;
import com.ArthurGrand.module.employee.repository.EmployeeRepository;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DepartmentServiceImp implements DepartmentService {

    private final DepartmentRepository departmentRepo;
    private final EmployeeRepository employeeRepo;
    private final ModelMapper modelMapper;
    private final EmailDepartmentService emailDepartmentService;

    public DepartmentServiceImp(DepartmentRepository departmentRepo,
                                EmployeeRepository employeeRepo,
                                ModelMapper modelMapper,
                                EmailDepartmentService emailDepartmentService) {
        this.departmentRepo = departmentRepo;
        this.employeeRepo = employeeRepo;
        this.modelMapper = modelMapper;
        this.emailDepartmentService = emailDepartmentService;
    }

    @Override
    @Transactional
    public String addDepartment(DepartmentDto departmentDTO) {
        Set<Employee> employeeSet = new HashSet<>();

        for (DropDownDto access : departmentDTO.getResources()) {
            if (access.getId() != null) {
                Optional<Employee> optionalEmployee = employeeRepo.findById(access.getId());
                optionalEmployee.ifPresent(employeeSet::add);
            }
        }

        Department department = new Department();
        modelMapper.map(departmentDTO, department);

        for (Employee employee : employeeSet) {
            employee.setDepartment(department);
        }

        department.setDepartmentLeads(employeeSet);
        departmentRepo.save(department);

        return department.getDepartmentName();
    }

    @Override
    @Transactional
    public List<DepartmentDto> getAllDepartment() {
        List<Department> departments = departmentRepo.findAll();
        List<DepartmentDto> departmentDTOList = new ArrayList<>();

        for (Department dept : departments) {
            Set<DropDownDto> employeeDto = dept.getDepartmentLeads().stream()
                    .map(emp -> new DropDownDto(emp.getId(), emp.getFirstname(), emp.getFirstname()))
                    .collect(Collectors.toSet());

            DepartmentDto departmentDTO = new DepartmentDto(
                    dept.getDepartmentId(),
                    dept.getDepartmentName(),
                    dept.getDepartmentLead(),
                    employeeDto
            );

            departmentDTOList.add(departmentDTO);
        }

        return departmentDTOList;
    }

    @Override
    @Transactional
    public Integer updateDepartment(DepartmentDto departmentDTO) {
        if (!departmentRepo.existsById(departmentDTO.getDepartmentId())) {
            throw new DepartmentNotFoundException("Department with ID " + departmentDTO.getDepartmentId() + " not found.");
        }

        Department department = departmentRepo.getReferenceById(departmentDTO.getDepartmentId());

        // Remove existing employees from this department
        List<Employee> existingEmployees = employeeRepo.findByDepartment(department)
                .orElse(Collections.emptyList());

        for (Employee emp : existingEmployees) {
            emp.setDepartment(null);
        }

        employeeRepo.saveAll(existingEmployees);

        // Update department fields
        department.setDepartmentName(departmentDTO.getDepartmentName());
        department.setDepartmentLead(departmentDTO.getDepartmentLead());

        // Add new employees to the department
        Set<Employee> updatedEmployees = new HashSet<>();
        for (DropDownDto dropdownDTO : departmentDTO.getResources()) {
            if (dropdownDTO.getId() != null) {
                Employee employee = employeeRepo.getReferenceById(dropdownDTO.getId());
                employee.setDepartment(department);
                updatedEmployees.add(employee);
            }
        }

        department.setDepartmentLeads(updatedEmployees);
        departmentRepo.save(department);

        return department.getDepartmentId();
    }

    @Override
    @Transactional
    public boolean deleteDepartment(int id) {
        if (!departmentRepo.existsById(id)) {
            throw new DepartmentNotFoundException("Department with ID " + id + " not found.");
        }

        departmentRepo.deleteById(id);
        return true;
    }


    @Override
    @Transactional
    public void exportDepartmentDataToEmail(String recipientEmail, String format) throws MessagingException {
        List<Department> departments = departmentRepo.findAll();
        List<DepartmentDto> departmentDTOS = new ArrayList<>();
        for (Department department : departments) {
            Set<DropDownDto> employeeDTOs = department.getDepartmentLeads().stream()
                    .map(employee -> {
                        DropDownDto dropdownDTO = new DropDownDto();
                        dropdownDTO.setId(employee.getId()); // Assuming dropdownDTO has setId method
                        dropdownDTO.setValue(employee.getFirstname()); // Assuming dropdownDTO has setValue method
                        dropdownDTO.setLabel(employee.getFirstname()); // Assuming dropdownDTO has setLabel method
                        return dropdownDTO;
                    })
                    .collect(Collectors.toSet());

            DepartmentDto departmentDTO = new DepartmentDto(
                    department.getDepartmentId(),
                    department.getDepartmentName(),
                    department.getDepartmentLead(),
                    employeeDTOs // Add employee DTOs to the DepartmentDTO constructor
            );
            departmentDTOS.add(departmentDTO);
        }

        if (format.equalsIgnoreCase("csv")) {
            // Assuming you have a method to send email with CSV attachment
            emailDepartmentService.sendEmailWithCSV(recipientEmail, departments);
        } else if (format.equalsIgnoreCase("pdf")) {
            // Assuming you have a method to send email with PDF attachment
            emailDepartmentService.sendEmailWithPDF(recipientEmail, departments);
        }
    }

    public Map<String, Object> getDashboardData() {
        Map<String, Object> dashboardData = new HashMap<>();

        // Get total department count
        long totalDepartments = departmentRepo.count();
        dashboardData.put("totalDepartments", totalDepartments);

        // Get count of distinct department names
        long distinctDepartmentNames = departmentRepo.countDistinctDepartmentNames();
        dashboardData.put("distinctDepartmentNames", distinctDepartmentNames);

        // Get count of distinct department leads
        long distinctDepartmentLeads = departmentRepo.countDistinctDepartmentLeads();
        dashboardData.put("distinctDepartmentLeads", distinctDepartmentLeads);

        // Get count by employee size
        List<Map<String, Object>> employeeCounts = departmentRepo.countByEmployeeSize();
        dashboardData.put("employeeCounts", employeeCounts);

        // Get department summary
        List<Map<String, Object>> departmentSummary = departmentRepo.getDepartmentSummary();
        dashboardData.put("departmentSummary", departmentSummary);

        return dashboardData;
    }

    @Override
    @Transactional
    public Page<DepartmentSimplifyDto> getDepartmentsSimplified(int page, int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Department> departmentPage = departmentRepo.findDepartmentsWithEmployees(pageable);

            return departmentPage.map(department -> {
                DepartmentSimplifyDto dto = new DepartmentSimplifyDto();
                dto.setDepartmentId(department.getDepartmentId());
                dto.setDepartmentName(department.getDepartmentName());
                dto.setDepartmentLead(department.getDepartmentLead());

                dto.setEmployeeNames(department.getDepartmentLeads() != null ?
                        department.getDepartmentLeads().stream()
                                .filter(Objects::nonNull)
                                .map(employee -> {
                                    Map<String, Object> employeeMap = new HashMap<>();
                                    employeeMap.put("employeeId", employee.getEmployeeId());
                                    employeeMap.put("firstname", employee.getFirstname());
                                    employeeMap.put("lastname", employee.getLastname());
                                    return employeeMap;
                                })
                                .collect(Collectors.toSet()) :
                        new HashSet<>());

                return dto;
            });
        } catch (Exception e) {
            throw e;
        }
    }
}
