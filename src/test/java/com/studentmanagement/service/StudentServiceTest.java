package com.studentmanagement.service;

import com.studentmanagement.dto.StudentDTO;
import com.studentmanagement.entity.Student;
import com.studentmanagement.exception.DuplicateResourceException;
import com.studentmanagement.exception.InvalidRequestException;
import com.studentmanagement.exception.ResourceNotFoundException;
import com.studentmanagement.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock
    private StudentRepository repository;

    @InjectMocks
    private StudentService service;

    private StudentDTO dto;

    @BeforeEach
    void setUp() {
        dto = new StudentDTO();
        dto.setFullName("Jane Doe");
        dto.setEmail("Jane.Doe@Example.com ");
        dto.setGender("Female");
        dto.setDepartment("Computer Science");
        dto.setCourse("B.Tech CS");
        dto.setMarks(84.0);
    }

    @Test
    void createStudent_generatesCodeNormalizesEmailAndCalculatesGrade() {
        when(repository.existsByEmailIgnoreCase("jane.doe@example.com")).thenReturn(false);
        when(repository.findMaxStudentCode()).thenReturn("STU-0041");
        when(repository.save(any(Student.class))).thenAnswer(inv -> {
            Student s = inv.getArgument(0);
            s.setId(1L);
            return s;
        });

        StudentDTO created = service.createStudent(dto);

        assertThat(created.getStudentCode()).isEqualTo("STU-0042");
        assertThat(created.getEmail()).isEqualTo("jane.doe@example.com");
        assertThat(created.getGrade()).isEqualTo("A+");
        assertThat(created.getResult()).isEqualTo("Outstanding");
    }

    @Test
    void createStudent_firstStudentGetsCode0001() {
        when(repository.existsByEmailIgnoreCase(any())).thenReturn(false);
        when(repository.findMaxStudentCode()).thenReturn(null);
        when(repository.save(any(Student.class))).thenAnswer(inv -> inv.getArgument(0));

        assertThat(service.createStudent(dto).getStudentCode()).isEqualTo("STU-0001");
    }

    @Test
    void createStudent_rejectsDuplicateEmail() {
        when(repository.existsByEmailIgnoreCase("jane.doe@example.com")).thenReturn(true);

        assertThatThrownBy(() -> service.createStudent(dto)).isInstanceOf(DuplicateResourceException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void updateStudent_preservesStudentCodeAndRecalculatesGrade() {
        Student existing = new Student();
        existing.setId(5L);
        existing.setStudentCode("STU-0005");
        existing.setMarks(30.0);
        existing.setGrade("F");
        when(repository.findById(5L)).thenReturn(Optional.of(existing));
        when(repository.findByEmailIgnoreCase(any())).thenReturn(Optional.empty());
        when(repository.save(any(Student.class))).thenAnswer(inv -> inv.getArgument(0));

        dto.setStudentCode("HACKED-999");
        dto.setMarks(55.0);
        StudentDTO updated = service.updateStudent(5L, dto);

        ArgumentCaptor<Student> captor = ArgumentCaptor.forClass(Student.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getStudentCode()).isEqualTo("STU-0005");
        assertThat(updated.getGrade()).isEqualTo("B");
        assertThat(updated.getResult()).isEqualTo("Average");
    }

    @Test
    void updateStudent_rejectsEmailOwnedByAnotherStudent() {
        Student existing = new Student();
        existing.setId(5L);
        Student other = new Student();
        other.setId(9L);
        when(repository.findById(5L)).thenReturn(Optional.of(existing));
        when(repository.findByEmailIgnoreCase("jane.doe@example.com")).thenReturn(Optional.of(other));

        assertThatThrownBy(() -> service.updateStudent(5L, dto)).isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void getAndDelete_throwNotFoundForUnknownId() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getStudentById(99L)).isInstanceOf(ResourceNotFoundException.class);
        assertThatThrownBy(() -> service.deleteStudent(99L)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void rejectsUnknownSortFieldAndBadPageSize() {
        assertThatThrownBy(() -> service.getAllStudents(0, 10, "password", "asc"))
                .isInstanceOf(InvalidRequestException.class);
        assertThatThrownBy(() -> service.getAllStudents(0, 1000, "fullName", "asc"))
                .isInstanceOf(InvalidRequestException.class);
    }
}
