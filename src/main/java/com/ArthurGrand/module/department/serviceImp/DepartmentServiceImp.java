package com.ArthurGrand.module.department.serviceImp;

import com.ArthurGrand.common.exception.DepartmentNotFoundException;
import com.ArthurGrand.dto.*;
import com.ArthurGrand.module.department.entity.Department;
import com.ArthurGrand.module.department.repository.DepartmentRepository;
import com.ArthurGrand.module.department.service.DepartmentService;
import com.ArthurGrand.module.employee.entity.Employee;
import com.ArthurGrand.module.employee.repository.EmployeeRepository;
import com.itextpdf.text.pdf.qrcode.Mode;
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
    public int createDepartment(DepartmentDto departmentDto) {
        Set<Employee> employeeSet = new HashSet<>();
        for (Integer empId : departmentDto.getEmployees()) {
            employeeRepo.findById(empId).ifPresent(employeeSet::add);
        }
        Department department = new Department();
        department.setDepartmentName(departmentDto.getDepartmentName());
        department.setDepartmentLead(departmentDto.getDepartmentLead());
        department.setDeleted(false);
        Department savedDepartment = departmentRepo.save(department);

        for (Employee employee : employeeSet) {
            employee.setDepartment(savedDepartment);
        }
        employeeRepo.saveAll(employeeSet);

        return savedDepartment.getId();
    }

    @Override
    public DepartmentViewDto getDepartment(int id) {
        Optional<Department> deptOpt = departmentRepo.findById(id);

        if (deptOpt.isEmpty()) {
            return null;
        }
        Department department = deptOpt.get();
        DepartmentViewDto dto = new DepartmentViewDto();

        List<Employee> employeeList = employeeRepo.findByDepartmentIdAndIsDeleteFalse(deptOpt.get().getId());
        Set<EmployeeViewDto> employeeDtoSet = employeeList.stream()
                .map(emp -> {
                    EmployeeViewDto empDto = new EmployeeViewDto();
                    modelMapper.map(emp, empDto);
                    return empDto;
                })
                .collect(Collectors.toSet());

        dto.setDepartmentId(department.getId());
        dto.setDepartmentName(department.getDepartmentName());
        dto.setDepartmentLead(department.getDepartmentLead());
        dto.setEmployees(employeeDtoSet);
        return dto;
    }

    @Override
    public Page<DepartmentViewDto> getAllDepartments(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Department> departmentPage = departmentRepo.findAll(pageable);

        return departmentPage.map(dept -> {
            DepartmentViewDto dto = new DepartmentViewDto();
            dto.setDepartmentId(dept.getId());
            dto.setDepartmentName(dept.getDepartmentName());
            dto.setDepartmentLead(dept.getDepartmentLead());

            List<Employee> employeeList = employeeRepo.findByDepartmentIdAndIsDeleteFalse(dept.getId());

            Set<EmployeeViewDto> employeeDtoSet = employeeList.stream()
                    .map(emp -> {
                        EmployeeViewDto empDto = new EmployeeViewDto();
                        modelMapper.map(emp, empDto);
                        return empDto;
                    })
                    .collect(Collectors.toSet());

            dto.setEmployees(employeeDtoSet);
            return dto;
        });
    }

    @Override
    public DepartmentViewDto updateDepartment(int id, DepartmentViewDto dto) {
        Optional<Department> opt = departmentRepo.findById(id);
        if (opt.isEmpty()) return null;

        Department dept = opt.get();
        dept.setDepartmentName(dto.getDepartmentName());
        dept.setDepartmentLead(dto.getDepartmentLead());
        departmentRepo.save(dept);

        DepartmentViewDto updatedDto = new DepartmentViewDto();
        updatedDto.setDepartmentId(dept.getId());
        updatedDto.setDepartmentName(dept.getDepartmentName());
        updatedDto.setDepartmentLead(dept.getDepartmentLead());
        updatedDto.setEmployees(dto.getEmployees()); // Optional: reload from DB

        return updatedDto;
    }

    @Override
    public boolean deleteDepartment(int id) {
        if (!departmentRepo.existsById(id)) {
            return false;
        }
        departmentRepo.deleteById(id);
        return true;
    }

    @Override
    @Transactional
    public void exportDepartmentDataToEmail(String recipientEmail, String format) throws MessagingException {
        List<Department> departments = departmentRepo.findAll();
        List<DepartmentViewDto> departmentDTOs = new ArrayList<>();

        for (Department department : departments) {
            Set<EmployeeViewDto> employeeDTOs = department.getEmployees().stream()
                    .map(employee -> {
                        EmployeeViewDto dto = new EmployeeViewDto();
                        modelMapper.map(employee, dto);
                        return dto;
                    })
                    .collect(Collectors.toSet());

            DepartmentViewDto departmentDTO = new DepartmentViewDto();
            modelMapper.map(department, departmentDTO);
            departmentDTO.setEmployees(employeeDTOs);

            departmentDTOs.add(departmentDTO);
        }

        if (format.equalsIgnoreCase("csv")) {
            emailDepartmentService.sendEmailWithCSV(recipientEmail, departments);
        } else if (format.equalsIgnoreCase("pdf")) {
            emailDepartmentService.sendEmailWithPDF(recipientEmail, departments);
        } else {
            throw new IllegalArgumentException("Unsupported export format: " + format);
        }
    }

    @Override
    @Transactional
    public Page<DepartmentSimplifyDto> getDepartmentsSimplified(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Department> departmentPage = departmentRepo.findDepartmentsWithEmployees(pageable);

        return departmentPage.map(department -> {
            DepartmentSimplifyDto dto = new DepartmentSimplifyDto();
            dto.setDepartmentId(department.getId());
            dto.setDepartmentName(department.getDepartmentName());
            dto.setDepartmentLead(department.getDepartmentLead());

            dto.setEmployeeNames(department.getEmployees() != null ?
                    department.getEmployees().stream()
                            .filter(Objects::nonNull)
                            .map(employee -> {
                                Map<String, Object> employeeMap = new HashMap<>();
                                employeeMap.put("employeeId", employee.getEmployeeId());
                                employeeMap.put("firstname", employee.getFirstName());
                                employeeMap.put("lastname", employee.getLastName());
                                return employeeMap;
                            })
                            .collect(Collectors.toSet()) :
                    new HashSet<>());

            return dto;
        });
    }

    @Override
    public Map<String, Object> getDashboardData() {
        Map<String, Object> dashboardData = new HashMap<>();
        dashboardData.put("totalDepartments", departmentRepo.count());
        dashboardData.put("distinctDepartmentNames", departmentRepo.countDistinctDepartmentNames());
        dashboardData.put("distinctDepartmentLeads", departmentRepo.countDistinctDepartmentLeads());
        dashboardData.put("employeeCounts", departmentRepo.countByEmployeeSize());
        dashboardData.put("departmentSummary", departmentRepo.getDepartmentSummary());
        return dashboardData;
    }
}