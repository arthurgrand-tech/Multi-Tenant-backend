-- Create Department table
CREATE TABLE IF NOT EXISTS Department (
    department_id INT PRIMARY KEY AUTO_INCREMENT,
    department_name VARCHAR(255),
    department_lead VARCHAR(255),
    is_deleted BOOLEAN DEFAULT FALSE
);

-- Create Employee table
CREATE TABLE IF NOT EXISTS employee (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employees_id VARCHAR(255),
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255),
    email_id VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    contact_number BIGINT,
    employee_status VARCHAR(50),
    department_id INT,
    CONSTRAINT fk_employee_department
        FOREIGN KEY (department_id)
        REFERENCES Department(department_id)
        ON DELETE SET NULL
        ON UPDATE CASCADE
);
