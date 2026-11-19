package com.studentmanagement.dto;

import com.studentmanagement.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class RegisterRequestDTO {

    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(regexp = "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$", message = "Invalid email format")
    @Size(max = 150, message = "Email must not exceed 150 characters")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 100, message = "Password must be at least 6 characters")
    private String password;

    @NotNull(message = "Role is required (STUDENT, TEACHER, or ADMIN)")
    private Role role;

    @Size(max = 100, message = "Department must not exceed 100 characters")
    private String department;

    @Size(max = 100, message = "Course must not exceed 100 characters")
    private String course;

    @Size(max = 20, message = "Semester must not exceed 20 characters")
    private String semester;

    @Size(max = 10, message = "Gender must not exceed 10 characters")
    private String gender;

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getCourse() { return course; }
    public void setCourse(String course) { this.course = course; }

    public String getSemester() { return semester; }
    public void setSemester(String semester) { this.semester = semester; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
}
