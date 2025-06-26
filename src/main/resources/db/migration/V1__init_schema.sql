-- Create Department table
CREATE TABLE IF NOT EXISTS department (
    id INT PRIMARY KEY AUTO_INCREMENT,
    department_name VARCHAR(255) NOT NULL UNIQUE,
    department_lead VARCHAR(255),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

-- Create Employee table
CREATE TABLE IF NOT EXISTS employee (
    id INT PRIMARY KEY AUTO_INCREMENT,
    employee_id VARCHAR(255) NOT NULL UNIQUE,
    first_name VARCHAR(255) NOT NULL UNIQUE,
    last_name VARCHAR(255),
    email_id VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    contact_number VARCHAR(255),
    employee_status VARCHAR(50) NOT NULL,
    timezone VARCHAR(255) NOT NULL,
    is_delete BOOLEAN NOT NULL DEFAULT FALSE,
    department_id INT,
    CONSTRAINT fk_employee_department
        FOREIGN KEY (department_id)
        REFERENCES department(id)
        ON DELETE SET NULL
        ON UPDATE CASCADE
);