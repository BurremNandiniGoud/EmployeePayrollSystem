# EmployeePayrollSystem
# Payroll Management System  

A *Java Swing* application with *MySQL* backend for managing employee payroll, leave, and HR tasks.  

### Features:  
 *Manager Portal* – Add/edit employees, process salaries, approve leaves  
 *Employee Portal* – View payslips, apply for leave, check payment history  
 *Database* – MySQL for secure data storage  
 *CSV Export* – Generate employee records in Excel  

### Tech Stack:  
- *Frontend*: Java Swing (GUI)  
- *Backend*: MySQL  
- *Security*: Role-based access (Manager/Employee)  

*Setup*:  
1. Import SQL script (payroll_system.sql)  
2. Configure JDBC connection(kindly use eclipse)  
3. Run the Java application  

*Default Logins*:  
 *Manager*: manager1 / manager123  
 ### Database Setup

Run the following SQL commands to create the required table: 
 
-- Create the database
CREATE DATABASE IF NOT EXISTS payroll_system;
USE payroll_system;
CREATE TABLE IF NOT EXISTS leave_applications (
    leave_id INT AUTO_INCREMENT PRIMARY KEY,
    employee_id VARCHAR(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    reason TEXT NOT NULL,
    status ENUM('pending', 'approved', 'rejected') DEFAULT 'pending',
    FOREIGN KEY (employee_id) REFERENCES employees(id)
);
-- Create users table for authentication
CREATE TABLE IF NOT EXISTS users (
    user_id VARCHAR(20) PRIMARY KEY,
    password VARCHAR(100) NOT NULL,
    role ENUM('manager', 'employee') NOT NULL,
    security_question VARCHAR(255),
    security_answer VARCHAR(255)
);

-- Create employees table
CREATE TABLE IF NOT EXISTS employees (
    id VARCHAR(20) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    basic_salary DECIMAL(10, 2) NOT NULL,
    email VARCHAR(100),
    phone VARCHAR(20),
    department VARCHAR(50)
);

-- Create salary_payments table
CREATE TABLE IF NOT EXISTS salary_payments (
    payment_id INT AUTO_INCREMENT PRIMARY KEY,
    employee_id VARCHAR(20) NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    payment_date DATE NOT NULL,
    status ENUM('paid', 'failed') NOT NULL,
    FOREIGN KEY (employee_id) REFERENCES employees(id)
);
-- Insert sample manager account (password: manager123)
INSERT INTO users (user_id, password, role, security_question, security_answer)
VALUES ('manager1', 'manager123', 'manager', 'What is your favorite color?', 'blue');
