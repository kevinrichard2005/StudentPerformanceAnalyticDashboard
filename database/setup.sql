-- ============================================
-- Student Management System — Database Setup
-- ============================================

-- Create database
CREATE DATABASE IF NOT EXISTS student_management_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE student_management_db;

-- Create students table
CREATE TABLE IF NOT EXISTS students (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_code VARCHAR(20) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL,
    phone VARCHAR(20),
    gender VARCHAR(10),
    date_of_birth DATE,
    department VARCHAR(100),
    course VARCHAR(100),
    year_of_study INT,
    semester VARCHAR(20),
    address VARCHAR(500),
    marks DOUBLE NOT NULL DEFAULT 0,
    grade VARCHAR(5),
    result VARCHAR(20),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT uk_student_code UNIQUE (student_code),
    CONSTRAINT uk_student_email UNIQUE (email),
    CONSTRAINT chk_marks_range CHECK (marks >= 0 AND marks <= 100),
    INDEX idx_department (department),
    INDEX idx_grade (grade),
    INDEX idx_gender (gender),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert sample data for testing (idempotent)
INSERT IGNORE INTO students (student_code, full_name, email, phone, gender, date_of_birth, department, course, year_of_study, semester, address, marks, grade, result) VALUES
('STU-0001', 'Alice Johnson', 'alice.johnson@university.edu', '+1234567890', 'Female', '2002-03-15', 'Computer Science', 'B.Tech CS', 2, '3rd', '123 University Ave', 92, 'O', 'Excellent'),
('STU-0002', 'Bob Smith', 'bob.smith@university.edu', '+1234567891', 'Male', '2001-07-22', 'Electronics', 'B.Tech ECE', 3, '5th', '456 College St', 78, 'A', 'Very Good'),
('STU-0003', 'Carol Williams', 'carol.w@university.edu', '+1234567892', 'Female', '2003-01-10', 'Computer Science', 'B.Tech CS', 1, '2nd', '789 Campus Rd', 85, 'A+', 'Outstanding'),
('STU-0004', 'David Brown', 'david.b@university.edu', '+1234567893', 'Male', '2002-11-05', 'Mechanical', 'B.Tech ME', 2, '4th', '321 Engineer Blvd', 45, 'C', 'Pass'),
('STU-0005', 'Eva Martinez', 'eva.m@university.edu', '+1234567894', 'Female', '2001-09-18', 'Mathematics', 'B.Sc Math', 3, '6th', '654 Science Park', 67, 'B+', 'Good'),
('STU-0006', 'Frank Wilson', 'frank.w@university.edu', '+1234567895', 'Male', '2003-04-25', 'Physics', 'B.Sc Physics', 1, '1st', '987 Lab Lane', 35, 'F', 'Fail'),
('STU-0007', 'Grace Lee', 'grace.l@university.edu', '+1234567896', 'Female', '2002-06-30', 'Computer Science', 'B.Tech CS', 2, '3rd', '147 Digital Dr', 55, 'B', 'Average'),
('STU-0008', 'Henry Davis', 'henry.d@university.edu', '+1234567897', 'Male', '2001-12-14', 'Civil', 'B.Tech Civil', 4, '7th', '258 Structure Ave', 73, 'A', 'Very Good'),
('STU-0009', 'Ivy Chen', 'ivy.c@university.edu', '+1234567898', 'Female', '2003-02-28', 'Business', 'BBA', 1, '2nd', '369 Commerce Sq', 88, 'A+', 'Outstanding'),
('STU-0010', 'Jack Thompson', 'jack.t@university.edu', '+1234567899', 'Male', '2002-08-07', 'Electronics', 'B.Tech ECE', 3, '5th', '741 Circuit Rd', 62, 'B+', 'Good');


-- Create users table for Student, Teacher, and Admin authentication
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    department VARCHAR(100),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT uk_user_email UNIQUE (email),
    INDEX idx_user_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
