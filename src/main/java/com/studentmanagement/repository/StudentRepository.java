package com.studentmanagement.repository;

import com.studentmanagement.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    Optional<Student> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    /** Global keyword search across ID, name, email, department and course. */
    @Query("SELECT s FROM Student s WHERE " +
           "LOWER(s.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(s.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(s.studentCode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(s.department) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(s.course) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Student> searchStudents(@Param("keyword") String keyword, Pageable pageable);

    /** Combined keyword + department + grade filter. Null parameters are ignored. */
    @Query("SELECT s FROM Student s WHERE " +
           "(:department IS NULL OR s.department = :department) AND " +
           "(:grade IS NULL OR s.grade = :grade) AND " +
           "(:keyword IS NULL OR " +
           "  LOWER(s.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "  LOWER(s.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "  LOWER(s.studentCode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "  LOWER(s.course) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Student> filterStudents(@Param("keyword") String keyword,
                                 @Param("department") String department,
                                 @Param("grade") String grade,
                                 Pageable pageable);

    long countByGender(String gender);

    long countByGrade(String grade);

    long countByGradeNot(String grade);

    @Query("SELECT AVG(s.marks) FROM Student s")
    Double getAverageMarks();

    @Query("SELECT s.grade, COUNT(s) FROM Student s GROUP BY s.grade")
    List<Object[]> getGradeDistribution();

    @Query("SELECT s.department, COUNT(s) FROM Student s GROUP BY s.department ORDER BY COUNT(s) DESC")
    List<Object[]> getDepartmentDistribution();

    @Query("SELECT s.gender, COUNT(s) FROM Student s GROUP BY s.gender")
    List<Object[]> getGenderDistribution();

    @Query("SELECT YEAR(s.createdAt), MONTH(s.createdAt), COUNT(s) FROM Student s " +
           "GROUP BY YEAR(s.createdAt), MONTH(s.createdAt) " +
           "ORDER BY YEAR(s.createdAt), MONTH(s.createdAt)")
    List<Object[]> getMonthlyEnrollment();

    List<Student> findTop5ByOrderByCreatedAtDesc();

    @Query("SELECT MAX(s.studentCode) FROM Student s WHERE s.studentCode LIKE 'STU-%'")
    String findMaxStudentCode();
}
