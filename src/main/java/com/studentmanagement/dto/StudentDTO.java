package com.studentmanagement.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class StudentDTO {

    private Long id;
    private String studentCode;

    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(regexp = "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$", message = "Invalid email format")
    @Size(max = 150, message = "Email must not exceed 150 characters")
    private String email;

    @Pattern(regexp = "^$|^[+]?[0-9]{7,15}$", message = "Invalid phone number (7-15 digits, optional +)")
    private String phone;

    @NotBlank(message = "Gender is required")
    @Pattern(regexp = "Male|Female|Other", message = "Gender must be Male, Female or Other")
    private String gender;

    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    @NotBlank(message = "Department is required")
    @Size(max = 100, message = "Department must not exceed 100 characters")
    private String department;

    @NotBlank(message = "Course is required")
    @Size(max = 100, message = "Course must not exceed 100 characters")
    private String course;

    @Min(value = 1, message = "Year of study must be between 1 and 6")
    @Max(value = 6, message = "Year of study must be between 1 and 6")
    private Integer yearOfStudy;

    @Size(max = 20, message = "Semester must not exceed 20 characters")
    private String semester;

    @Size(max = 500, message = "Address must not exceed 500 characters")
    private String address;

    @NotNull(message = "Marks are required")
    @DecimalMin(value = "0.0", message = "Marks cannot be less than 0")
    @DecimalMax(value = "100.0", message = "Marks cannot exceed 100")
    private Double marks;

    private String grade;
    private String result;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getStudentCode() { return studentCode; }
    public void setStudentCode(String studentCode) { this.studentCode = studentCode; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getCourse() { return course; }
    public void setCourse(String course) { this.course = course; }
    public Integer getYearOfStudy() { return yearOfStudy; }
    public void setYearOfStudy(Integer yearOfStudy) { this.yearOfStudy = yearOfStudy; }
    public String getSemester() { return semester; }
    public void setSemester(String semester) { this.semester = semester; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public Double getMarks() { return marks; }
    public void setMarks(Double marks) { this.marks = marks; }
    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }
    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
