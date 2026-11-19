package com.studentmanagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studentmanagement.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * End-to-end tests through the HTTP layer, service and JPA against an in-memory database.
 */
@SpringBootTest
@AutoConfigureMockMvc
class StudentControllerIntegrationTest {

    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper mapper;
    @Autowired private StudentRepository repository;

    @BeforeEach
    void clean() {
        repository.deleteAll();
    }

    private Map<String, Object> student(String name, String email, String gender, String dept, double marks) {
        Map<String, Object> m = new HashMap<>();
        m.put("fullName", name);
        m.put("email", email);
        m.put("phone", "+919876543210");
        m.put("gender", gender);
        m.put("dateOfBirth", "2003-05-14");
        m.put("department", dept);
        m.put("course", "B.Tech");
        m.put("yearOfStudy", 2);
        m.put("semester", "3rd");
        m.put("address", "12 Campus Road");
        m.put("marks", marks);
        return m;
    }

    private long create(Map<String, Object> body) throws Exception {
        MvcResult r = mvc.perform(post("/api/students").contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andReturn();
        return mapper.readTree(r.getResponse().getContentAsString()).path("data").path("id").asLong();
    }

    @Test
    void fullCrudLifecycle() throws Exception {
        // Create
        long id = create(student("Asha Rao", "asha@uni.edu", "Female", "Computer Science", 91));

        // Read
        mvc.perform(get("/api/students/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.studentCode").value("STU-0001"))
                .andExpect(jsonPath("$.data.grade").value("O"))
                .andExpect(jsonPath("$.data.result").value("Excellent"))
                .andExpect(jsonPath("$.data.createdAt").exists());

        // Update — marks change must recalculate grade; student code stays the same
        Map<String, Object> update = student("Asha R. Rao", "asha@uni.edu", "Female", "Computer Science", 35);
        update.put("studentCode", "STU-9999");
        mvc.perform(put("/api/students/{id}", id).contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fullName").value("Asha R. Rao"))
                .andExpect(jsonPath("$.data.studentCode").value("STU-0001"))
                .andExpect(jsonPath("$.data.grade").value("F"))
                .andExpect(jsonPath("$.data.result").value("Fail"));

        // Delete
        mvc.perform(delete("/api/students/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Student deleted successfully"));
        mvc.perform(get("/api/students/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void duplicateEmailReturnsConflict() throws Exception {
        create(student("A One", "dup@uni.edu", "Male", "Physics", 50));
        mvc.perform(post("/api/students").contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(student("A Two", "DUP@uni.edu", "Male", "Physics", 60))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString("already exists")));
    }

    @Test
    void invalidPayloadReturnsFieldErrors() throws Exception {
        Map<String, Object> bad = student("", "not-an-email", "Unknown", "Physics", 120);
        bad.put("phone", "12ab");
        mvc.perform(post("/api/students").contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(bad)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.data.fullName").exists())
                .andExpect(jsonPath("$.data.email").exists())
                .andExpect(jsonPath("$.data.gender").exists())
                .andExpect(jsonPath("$.data.phone").exists())
                .andExpect(jsonPath("$.data.marks").value("Marks cannot exceed 100"));
    }

    @Test
    void negativeMarksRejected() throws Exception {
        mvc.perform(post("/api/students").contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(student("Neg Marks", "neg@uni.edu", "Male", "Civil", -5))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data.marks").value("Marks cannot be less than 0"));
    }

    @Test
    void malformedJsonReturnsBadRequest() throws Exception {
        mvc.perform(post("/api/students").contentType(MediaType.APPLICATION_JSON).content("{bad json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void searchFilterSortAndPaginate() throws Exception {
        create(student("Ravi Kumar", "ravi@uni.edu", "Male", "Computer Science", 95));
        create(student("Meera Nair", "meera@uni.edu", "Female", "Electronics", 72));
        create(student("Kiran Das", "kiran@uni.edu", "Male", "Computer Science", 38));
        create(student("Priya Shah", "priya@uni.edu", "Female", "Mechanical", 81));

        // Keyword search by name (case-insensitive)
        mvc.perform(get("/api/students/search").param("keyword", "meera"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].fullName").value("Meera Nair"));

        // Search by student ID
        mvc.perform(get("/api/students/search").param("keyword", "STU-0003"))
                .andExpect(jsonPath("$.data.content[0].fullName").value("Kiran Das"));

        // Department filter
        mvc.perform(get("/api/students/filter").param("department", "Computer Science"))
                .andExpect(jsonPath("$.data.totalElements").value(2));

        // Grade filter combined with department
        mvc.perform(get("/api/students/filter").param("department", "Computer Science").param("grade", "F"))
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].fullName").value("Kiran Das"));

        // Keyword + department combined
        mvc.perform(get("/api/students/filter").param("keyword", "ravi").param("department", "Electronics"))
                .andExpect(jsonPath("$.data.totalElements").value(0));

        // Sorting by marks descending
        mvc.perform(get("/api/students").param("sortBy", "marks").param("sortDir", "desc"))
                .andExpect(jsonPath("$.data.content[0].marks").value(95.0))
                .andExpect(jsonPath("$.data.content[3].marks").value(38.0));

        // Pagination
        mvc.perform(get("/api/students").param("page", "1").param("size", "3").param("sortBy", "fullName").param("sortDir", "asc"))
                .andExpect(jsonPath("$.data.totalElements").value(4))
                .andExpect(jsonPath("$.data.totalPages").value(2))
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].fullName").value("Ravi Kumar"));
    }

    @Test
    void invalidSortFieldReturnsBadRequest() throws Exception {
        mvc.perform(get("/api/students").param("sortBy", "nonexistent"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Invalid sort field")));
    }

    @Test
    void statsReflectDatabaseContents() throws Exception {
        create(student("S One", "s1@uni.edu", "Male", "Physics", 90));
        create(student("S Two", "s2@uni.edu", "Female", "Physics", 70));
        create(student("S Three", "s3@uni.edu", "Female", "Biology", 20));

        mvc.perform(get("/api/students/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalStudents").value(3))
                .andExpect(jsonPath("$.data.maleStudents").value(1))
                .andExpect(jsonPath("$.data.femaleStudents").value(2))
                .andExpect(jsonPath("$.data.averageMarks").value(60.0))
                .andExpect(jsonPath("$.data.passedStudents").value(2))
                .andExpect(jsonPath("$.data.failedStudents").value(1))
                .andExpect(jsonPath("$.data.passPercentage").value(66.67))
                .andExpect(jsonPath("$.data.gradeDistribution.O").value(1))
                .andExpect(jsonPath("$.data.gradeDistribution.F").value(1))
                .andExpect(jsonPath("$.data.departmentDistribution.Physics").value(2))
                .andExpect(jsonPath("$.data.enrollmentTrend").isMap());

        mvc.perform(get("/api/students/recent"))
                .andExpect(jsonPath("$.data", hasSize(3)));
    }

    @Test
    void unknownStudentReturns404() throws Exception {
        mvc.perform(put("/api/students/{id}", 12345).contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(student("Ghost", "ghost@uni.edu", "Male", "Arts", 50))))
                .andExpect(status().isNotFound());
        mvc.perform(delete("/api/students/{id}", 12345)).andExpect(status().isNotFound());
    }

    @Test
    void staticPagesAreServed() throws Exception {
        mvc.perform(get("/index.html")).andExpect(status().isOk());
        mvc.perform(get("/pages/students.html")).andExpect(status().isOk());
        mvc.perform(get("/pages/add-student.html")).andExpect(status().isOk());
        mvc.perform(get("/pages/edit-student.html")).andExpect(status().isOk());
        mvc.perform(get("/pages/student-details.html")).andExpect(status().isOk());
        mvc.perform(get("/pages/analytics.html")).andExpect(status().isOk());
        mvc.perform(get("/pages/settings.html")).andExpect(status().isOk());
    }
}
