package com.studentmanagement.config;

import com.studentmanagement.dto.StudentDTO;
import com.studentmanagement.entity.Role;
import com.studentmanagement.repository.StudentRepository;
import com.studentmanagement.service.AuthService;
import com.studentmanagement.service.StudentService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Seeds default Admin, Teacher, and Student accounts (plus sample student records
 * when the database is empty) so all three roles can be tested immediately.
 */
@Component
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true", matchIfMissing = true)
public class DataInitializer implements CommandLineRunner {

    private final AuthService authService;
    private final StudentService studentService;
    private final StudentRepository studentRepository;

    public DataInitializer(AuthService authService,
                           StudentService studentService,
                           StudentRepository studentRepository) {
        this.authService = authService;
        this.studentService = studentService;
        this.studentRepository = studentRepository;
    }

    @Override
    public void run(String... args) {
        if (studentRepository.count() == 0) {
            seedSampleStudents();
        }

        // Default role accounts for immediate testing
        authService.ensureDefaultAccount(
                "System Administrator", "admin@university.edu", "Admin@123", Role.ADMIN, "Administration");
        authService.ensureDefaultAccount(
                "Prof. Robert Langdon", "teacher@university.edu", "Teacher@123", Role.TEACHER, "Computer Science");
        authService.ensureDefaultAccount(
                "Alice Johnson", "alice.johnson@university.edu", "Student@123", Role.STUDENT, "Computer Science");
    }

    private void seedSampleStudents() {
        createSample("Alice Johnson", "alice.johnson@university.edu", "+1234567890", "Female",
                "2002-03-15", "Computer Science", "B.Tech CS", 2, "3rd", "123 University Ave", 92);
        createSample("Bob Smith", "bob.smith@university.edu", "+1234567891", "Male",
                "2001-07-22", "Electronics", "B.Tech ECE", 3, "5th", "456 College St", 78);
        createSample("Carol Williams", "carol.w@university.edu", "+1234567892", "Female",
                "2003-01-10", "Computer Science", "B.Tech CS", 1, "2nd", "789 Campus Rd", 85);
        createSample("David Brown", "david.b@university.edu", "+1234567893", "Male",
                "2002-11-05", "Mechanical", "B.Tech ME", 2, "4th", "321 Engineer Blvd", 45);
        createSample("Eva Martinez", "eva.m@university.edu", "+1234567894", "Female",
                "2001-09-18", "Mathematics", "B.Sc Math", 3, "6th", "654 Science Park", 67);
    }

    private void createSample(String name, String email, String phone, String gender,
                              String dob, String dept, String course, int year,
                              String sem, String addr, double marks) {
        StudentDTO dto = new StudentDTO();
        dto.setFullName(name);
        dto.setEmail(email);
        dto.setPhone(phone);
        dto.setGender(gender);
        dto.setDateOfBirth(LocalDate.parse(dob));
        dto.setDepartment(dept);
        dto.setCourse(course);
        dto.setYearOfStudy(year);
        dto.setSemester(sem);
        dto.setAddress(addr);
        dto.setMarks(marks);
        studentService.createStudent(dto);
    }
}
