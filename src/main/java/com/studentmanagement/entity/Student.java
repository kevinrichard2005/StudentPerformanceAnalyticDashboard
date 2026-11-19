package com.studentmanagement.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "students",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_student_code", columnNames = "student_code"),
        @UniqueConstraint(name = "uk_student_email", columnNames = "email")
    },
    indexes = {
        @Index(name = "idx_department", columnList = "department"),
        @Index(name = "idx_grade", columnList = "grade"),
        @Index(name = "idx_gender", columnList = "gender"),
        @Index(name = "idx_created_at", columnList = "created_at")
    })
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_code", nullable = false, length = 20, updatable = false)
    private String studentCode;

    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Column(nullable = false, length = 150)
    private String email;

    @Pattern(regexp = "^[+]?[0-9]{7,15}$", message = "Invalid phone number")
    @Column(length = 20)
    private String phone;

    @NotBlank(message = "Gender is required")
    @Column(length = 10)
    private String gender;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @NotBlank(message = "Department is required")
    @Column(length = 100)
    private String department;

    @NotBlank(message = "Course is required")
    @Column(length = 100)
    private String course;

    @Column(name = "year_of_study")
    private Integer yearOfStudy;

    @Column(length = 20)
    private String semester;

    @Column(length = 500)
    private String address;

    @DecimalMin(value = "0.0", message = "Marks cannot be less than 0")
    @DecimalMax(value = "100.0", message = "Marks cannot exceed 100")
    @Column(nullable = false)
    private Double marks;

    @Column(length = 5)
    private String grade;

    @Column(length = 20)
    private String result;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now().withNano(0);
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now().withNano(0);
    }

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
