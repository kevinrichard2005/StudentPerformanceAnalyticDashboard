package com.studentmanagement.dto;

import com.studentmanagement.entity.Role;
import java.time.LocalDateTime;

public class UserProfileDTO {

    private Long id;
    private String fullName;
    private String email;
    private Role role;
    private String department;
    private Long studentRecordId;
    private String studentCode;
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public Long getStudentRecordId() { return studentRecordId; }
    public void setStudentRecordId(Long studentRecordId) { this.studentRecordId = studentRecordId; }

    public String getStudentCode() { return studentCode; }
    public void setStudentCode(String studentCode) { this.studentCode = studentCode; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
