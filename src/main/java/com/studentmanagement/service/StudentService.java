package com.studentmanagement.service;

import com.studentmanagement.dto.StudentDTO;
import com.studentmanagement.dto.StudentStatsDTO;
import com.studentmanagement.entity.Student;
import com.studentmanagement.exception.DuplicateResourceException;
import com.studentmanagement.exception.InvalidRequestException;
import com.studentmanagement.exception.ResourceNotFoundException;
import com.studentmanagement.repository.StudentRepository;
import com.studentmanagement.util.GradeCalculator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Month;
import java.time.format.TextStyle;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Business logic for student management: CRUD, grading, search and statistics.
 */
@Service
@Transactional
public class StudentService {

    /** Whitelisted sortable fields — prevents arbitrary property access via the API. */
    private static final Set<String> SORTABLE_FIELDS = Set.of(
            "id", "studentCode", "fullName", "email", "department", "course",
            "semester", "marks", "grade", "createdAt", "updatedAt");

    private static final int MAX_PAGE_SIZE = 100;
    private static final String[] GRADE_ORDER = {"O", "A+", "A", "B+", "B", "C", "F"};

    private final StudentRepository studentRepository;

    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    // ===================== CRUD =====================

    public StudentDTO createStudent(StudentDTO dto) {
        String email = normalizeEmail(dto.getEmail());
        if (studentRepository.existsByEmailIgnoreCase(email)) {
            throw new DuplicateResourceException("A student with email '" + email + "' already exists");
        }

        Student student = new Student();
        applyDto(student, dto);
        student.setStudentCode(generateStudentCode());
        applyGrade(student);

        return mapToDTO(studentRepository.save(student));
    }

    @Transactional(readOnly = true)
    public Page<StudentDTO> getAllStudents(int page, int size, String sortBy, String sortDir) {
        return studentRepository.findAll(buildPageable(page, size, sortBy, sortDir)).map(this::mapToDTO);
    }

    @Transactional(readOnly = true)
    public StudentDTO getStudentById(Long id) {
        return mapToDTO(findOrThrow(id));
    }

    @Transactional(readOnly = true)
    public java.util.Optional<StudentDTO> findStudentByEmail(String email) {
        if (email == null || email.isBlank()) {
            return java.util.Optional.empty();
        }
        return studentRepository.findByEmailIgnoreCase(normalizeEmail(email)).map(this::mapToDTO);
    }

    public StudentDTO updateStudent(Long id, StudentDTO dto) {
        Student existing = findOrThrow(id);
        String email = normalizeEmail(dto.getEmail());

        studentRepository.findByEmailIgnoreCase(email).ifPresent(other -> {
            if (!other.getId().equals(id)) {
                throw new DuplicateResourceException("A student with email '" + email + "' already exists");
            }
        });

        // Student code is intentionally preserved during updates.
        applyDto(existing, dto);
        applyGrade(existing);

        return mapToDTO(studentRepository.save(existing));
    }

    public void deleteStudent(Long id) {
        Student existing = findOrThrow(id);
        studentRepository.delete(existing);
    }

    // ===================== Search & Filter =====================

    @Transactional(readOnly = true)
    public Page<StudentDTO> searchStudents(String keyword, int page, int size, String sortBy, String sortDir) {
        String kw = blankToNull(keyword);
        Pageable pageable = buildPageable(page, size, sortBy, sortDir);
        if (kw == null) {
            return studentRepository.findAll(pageable).map(this::mapToDTO);
        }
        return studentRepository.searchStudents(kw, pageable).map(this::mapToDTO);
    }

    @Transactional(readOnly = true)
    public Page<StudentDTO> filterStudents(String keyword, String department, String grade,
                                           int page, int size, String sortBy, String sortDir) {
        Pageable pageable = buildPageable(page, size, sortBy, sortDir);
        return studentRepository
                .filterStudents(blankToNull(keyword), blankToNull(department), blankToNull(grade), pageable)
                .map(this::mapToDTO);
    }

    @Transactional(readOnly = true)
    public List<StudentDTO> getRecentStudents() {
        return studentRepository.findTop5ByOrderByCreatedAtDesc().stream().map(this::mapToDTO).toList();
    }

    // ===================== Statistics =====================

    @Transactional(readOnly = true)
    public StudentStatsDTO getStudentStats() {
        StudentStatsDTO stats = new StudentStatsDTO();
        long total = studentRepository.count();
        long failed = studentRepository.countByGrade("F");
        long passed = total - failed;

        stats.setTotalStudents(total);
        stats.setMaleStudents(studentRepository.countByGender("Male"));
        stats.setFemaleStudents(studentRepository.countByGender("Female"));
        stats.setPassedStudents(passed);
        stats.setFailedStudents(failed);

        Double avg = studentRepository.getAverageMarks();
        stats.setAverageMarks(avg != null ? round2(avg) : 0.0);
        stats.setPassPercentage(total > 0 ? round2(passed * 100.0 / total) : 0.0);

        Map<String, Long> gradeMap = new LinkedHashMap<>();
        for (String g : GRADE_ORDER) {
            gradeMap.put(g, 0L);
        }
        studentRepository.getGradeDistribution()
                .forEach(row -> gradeMap.put((String) row[0], (Long) row[1]));
        stats.setGradeDistribution(gradeMap);

        stats.setDepartmentDistribution(toMap(studentRepository.getDepartmentDistribution()));
        stats.setGenderDistribution(toMap(studentRepository.getGenderDistribution()));

        Map<String, Long> enrollment = new LinkedHashMap<>();
        studentRepository.getMonthlyEnrollment().forEach(row -> {
            int year = ((Number) row[0]).intValue();
            int month = ((Number) row[1]).intValue();
            String label = Month.of(month).getDisplayName(TextStyle.SHORT, Locale.ENGLISH) + " " + year;
            enrollment.put(label, ((Number) row[2]).longValue());
        });
        stats.setEnrollmentTrend(enrollment);

        return stats;
    }

    // ===================== Helpers =====================

    /** Applies backend grade calculation — the authoritative source of grades. */
    private void applyGrade(Student student) {
        GradeCalculator.GradeResult r = GradeCalculator.evaluate(student.getMarks());
        student.setGrade(r.grade());
        student.setResult(r.result());
    }

    private void applyDto(Student s, StudentDTO dto) {
        s.setFullName(dto.getFullName().trim());
        s.setEmail(normalizeEmail(dto.getEmail()));
        s.setPhone(blankToNull(dto.getPhone()));
        s.setGender(dto.getGender());
        s.setDateOfBirth(dto.getDateOfBirth());
        s.setDepartment(dto.getDepartment());
        s.setCourse(dto.getCourse().trim());
        s.setYearOfStudy(dto.getYearOfStudy());
        s.setSemester(blankToNull(dto.getSemester()));
        s.setAddress(blankToNull(dto.getAddress()));
        s.setMarks(dto.getMarks());
    }

    private Student findOrThrow(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));
    }

    private Pageable buildPageable(int page, int size, String sortBy, String sortDir) {
        if (page < 0) {
            throw new InvalidRequestException("Page index must not be negative");
        }
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new InvalidRequestException("Page size must be between 1 and " + MAX_PAGE_SIZE);
        }
        String field = (sortBy == null || sortBy.isBlank()) ? "createdAt" : sortBy;
        if (!SORTABLE_FIELDS.contains(field)) {
            throw new InvalidRequestException("Invalid sort field: " + field);
        }
        Sort sort = "asc".equalsIgnoreCase(sortDir) ? Sort.by(field).ascending() : Sort.by(field).descending();
        // Secondary sort on id keeps pagination stable when primary values tie.
        return PageRequest.of(page, size, sort.and(Sort.by("id").ascending()));
    }

    private synchronized String generateStudentCode() {
        String maxCode = studentRepository.findMaxStudentCode();
        int next = 1;
        if (maxCode != null) {
            try {
                next = Integer.parseInt(maxCode.substring(4)) + 1;
            } catch (NumberFormatException | IndexOutOfBoundsException e) {
                next = (int) studentRepository.count() + 1;
            }
        }
        return String.format("STU-%04d", next);
    }

    private static Map<String, Long> toMap(List<Object[]> rows) {
        Map<String, Long> map = new LinkedHashMap<>();
        rows.forEach(row -> map.put(String.valueOf(row[0]), ((Number) row[1]).longValue()));
        return map;
    }

    private static String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    private static String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }

    private static double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private StudentDTO mapToDTO(Student s) {
        StudentDTO dto = new StudentDTO();
        dto.setId(s.getId());
        dto.setStudentCode(s.getStudentCode());
        dto.setFullName(s.getFullName());
        dto.setEmail(s.getEmail());
        dto.setPhone(s.getPhone());
        dto.setGender(s.getGender());
        dto.setDateOfBirth(s.getDateOfBirth());
        dto.setDepartment(s.getDepartment());
        dto.setCourse(s.getCourse());
        dto.setYearOfStudy(s.getYearOfStudy());
        dto.setSemester(s.getSemester());
        dto.setAddress(s.getAddress());
        dto.setMarks(s.getMarks());
        dto.setGrade(s.getGrade());
        dto.setResult(s.getResult());
        dto.setCreatedAt(s.getCreatedAt());
        dto.setUpdatedAt(s.getUpdatedAt());
        return dto;
    }
}
