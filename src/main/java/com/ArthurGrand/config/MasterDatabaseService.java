package com.ArthurGrand.config;

import com.ArthurGrand.module.employee.entity.Employee;
import com.ArthurGrand.module.employee.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.Optional;

@Service
public class MasterDatabaseService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostConstruct
    public void initializeMasterDatabase() {
        try {
            // Create master database if it doesn't exist
            createMasterDatabaseIfNotExists();

            // Create master admin user
            createMasterAdminIfNotExists();

            System.out.println("✅ Master database initialization completed successfully");
        } catch (Exception e) {
            System.err.println("❌ Failed to initialize master database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void createMasterDatabaseIfNotExists() {
        try {
            // Check if master database exists
            String sql = "SELECT SCHEMA_NAME FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME = 'master'";
            var result = jdbcTemplate.queryForList(sql, String.class);

            if (result.isEmpty()) {
                jdbcTemplate.execute("CREATE DATABASE master");
                System.out.println("✅ Master database created");
            } else {
                System.out.println("✅ Master database already exists");
            }

            // Use master database
            jdbcTemplate.execute("USE master");

            // Create employee table in master if not exists
            String createTableSql = """
                CREATE TABLE IF NOT EXISTS employee (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    first_name VARCHAR(255) NOT NULL,
                    last_name VARCHAR(255),
                    email_id VARCHAR(255) NOT NULL UNIQUE,
                    password VARCHAR(255) NOT NULL
                )
            """;
            jdbcTemplate.execute(createTableSql);

        } catch (Exception e) {
            System.err.println("❌ Error creating master database: " + e.getMessage());
            throw new RuntimeException("Failed to create master database", e);
        }
    }

    private void createMasterAdminIfNotExists() {
        try {
            Optional<Employee> existingAdmin = employeeRepository.findByEmailid("superadmin@gmail.com");

            if (existingAdmin.isEmpty()) {
                Employee masterAdmin = new Employee();
                masterAdmin.setFirstname("Super");
                masterAdmin.setLastname("Admin");
                masterAdmin.setEmailid("superadmin@gmail.com");
                masterAdmin.setPassword(passwordEncoder.encode("123"));

                employeeRepository.save(masterAdmin);
                System.out.println("✅ Master admin user created successfully");
            } else {
                System.out.println("✅ Master admin user already exists");
            }
        } catch (Exception e) {
            System.err.println("❌ Error creating master admin: " + e.getMessage());
            throw new RuntimeException("Failed to create master admin", e);
        }
    }
}