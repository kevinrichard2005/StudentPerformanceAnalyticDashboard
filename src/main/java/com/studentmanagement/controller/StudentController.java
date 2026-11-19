package com.studentmanagement.controller;

import com.studentmanagement.dto.ApiResponse;
import com.studentmanagement.dto.StudentDTO;
import com.studentmanagement.dto.StudentStatsDTO;
import com.studentmanagement.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST endpoints for student management. Contains no business logic — delegates to {@link StudentService}.
 */
@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<StudentDTO>> createStudent(@Valid @RequestBody StudentDTO studentDTO) {
        StudentDTO created = studentService.createStudent(studentDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Student created successfully", created));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<StudentDTO>>> getAllStudents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        return ResponseEntity.ok(ApiResponse.success(studentService.getAllStudents(page, size, sortBy, sortDir)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<StudentDTO>> getStudentById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(studentService.getStudentById(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<StudentDTO>> updateStudent(@PathVariable Long id,
                                                                 @Valid @RequestBody StudentDTO studentDTO) {
        return ResponseEntity.ok(ApiResponse.success("Student updated successfully",
                studentService.updateStudent(id, studentDTO)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteStudent(@PathVariable Long id) {
        studentService.deleteStudent(id);
        return ResponseEntity.ok(ApiResponse.success("Student deleted successfully", null));
    }

    /** Global keyword search (name, email, student ID, department, course). */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<StudentDTO>>> searchStudents(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "fullName") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        return ResponseEntity.ok(ApiResponse.success(
                studentService.searchStudents(keyword, page, size, sortBy, sortDir)));
    }

    /** Combined keyword, department and grade filtering with sorting and pagination. */
    @GetMapping("/filter")
    public ResponseEntity<ApiResponse<Page<StudentDTO>>> filterStudents(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String grade,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        return ResponseEntity.ok(ApiResponse.success(
                studentService.filterStudents(keyword, department, grade, page, size, sortBy, sortDir)));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<StudentStatsDTO>> getStudentStats() {
        return ResponseEntity.ok(ApiResponse.success(studentService.getStudentStats()));
    }

    @GetMapping("/recent")
    public ResponseEntity<ApiResponse<List<StudentDTO>>> getRecentStudents() {
        return ResponseEntity.ok(ApiResponse.success(studentService.getRecentStudents()));
    }
}
