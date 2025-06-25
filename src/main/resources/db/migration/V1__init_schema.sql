-- V1__init_master_schema.sql
-- This script initializes the master database schema

-- Create tenant table
CREATE TABLE IF NOT EXISTS tenant (
    tenant_id INT AUTO_INCREMENT PRIMARY KEY,
    domain VARCHAR(255) UNIQUE NOT NULL,
    company_name VARCHAR(255) UNIQUE NOT NULL,
    database_name VARCHAR(255) UNIQUE NOT NULL,
    uses_custom_db BOOLEAN NOT NULL DEFAULT FALSE,
    db_host VARCHAR(255),
    db_port INT,
    db_username VARCHAR(255),
    db_password VARCHAR(255),
    admin_email VARCHAR(255) UNIQUE NOT NULL,
    status ENUM('ACTIVE', 'INACTIVE', 'EXPIRED') NOT NULL DEFAULT 'INACTIVE',
    timezone VARCHAR(50) NOT NULL DEFAULT 'UTC',
    country VARCHAR(10) NOT NULL DEFAULT 'US',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_domain (domain),
    INDEX idx_status (status),
    INDEX idx_admin_email (admin_email)
);

-- Create tenant profile table
CREATE TABLE IF NOT EXISTS tenant_profile (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id INT NOT NULL,
    contact_person VARCHAR(255),
    email VARCHAR(255),
    phone_number VARCHAR(20),
    address TEXT,
    website VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (tenant_id) REFERENCES tenant(tenant_id) ON DELETE CASCADE,
    UNIQUE KEY unique_tenant_profile (tenant_id)
);

-- Create employee table for master admin users
CREATE TABLE IF NOT EXISTS employee (
    id INT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255),
    email_id VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_email (email_id)
);

-- Create payment related tables in master database
CREATE TABLE IF NOT EXISTS stripe_customers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id INT NOT NULL,
    stripe_customer_id VARCHAR(255) UNIQUE NOT NULL,
    email VARCHAR(255) NOT NULL,
    name VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_tenant_email (tenant_id, email),
    INDEX idx_stripe_customer (stripe_customer_id),
    FOREIGN KEY (tenant_id) REFERENCES tenant(tenant_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS subscriptions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id INT NOT NULL,
    stripe_subscription_id VARCHAR(255) UNIQUE NOT NULL,
    stripe_customer_id VARCHAR(255) NOT NULL,
    plan_id VARCHAR(255) NOT NULL,
    status ENUM('INCOMPLETE', 'INCOMPLETE_EXPIRED', 'TRIALING', 'ACTIVE', 'PAST_DUE', 'CANCELED', 'UNPAID') NOT NULL,
    current_period_start TIMESTAMP NOT NULL,
    current_period_end TIMESTAMP NOT NULL,
    cancel_at_period_end BOOLEAN DEFAULT FALSE,
    trial_end TIMESTAMP NULL,
    amount DECIMAL(10,2),
    currency VARCHAR(3) DEFAULT 'USD',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_tenant_status (tenant_id, status),
    INDEX idx_stripe_subscription (stripe_subscription_id),
    INDEX idx_status (status),
    FOREIGN KEY (tenant_id) REFERENCES tenant(tenant_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS payment_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id INT NOT NULL,
    stripe_payment_intent_id VARCHAR(255),
    stripe_invoice_id VARCHAR(255),
    subscription_id BIGINT,
    amount DECIMAL(10,2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'USD',
    status ENUM('SUCCEEDED', 'PENDING', 'FAILED', 'CANCELED', 'REFUNDED') NOT NULL,
    payment_method_type VARCHAR(50),
    failure_reason TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_tenant_created (tenant_id, created_at DESC),
    INDEX idx_subscription (subscription_id),
    INDEX idx_stripe_payment_intent (stripe_payment_intent_id),
    INDEX idx_status (status),
    FOREIGN KEY (tenant_id) REFERENCES tenant(tenant_id) ON DELETE CASCADE,
    FOREIGN KEY (subscription_id) REFERENCES subscriptions(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS webhook_events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    stripe_event_id VARCHAR(255) UNIQUE NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    processed BOOLEAN DEFAULT FALSE,
    retry_count INT DEFAULT 0,
    error_message TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP NULL,

    INDEX idx_processed (processed),
    INDEX idx_event_type (event_type),
    INDEX idx_stripe_event (stripe_event_id)
);