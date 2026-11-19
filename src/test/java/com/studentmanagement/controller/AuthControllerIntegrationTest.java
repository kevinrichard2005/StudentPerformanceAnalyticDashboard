package com.studentmanagement.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studentmanagement.repository.StudentRepository;
import com.studentmanagement.repository.UserAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * End-to-end tests for Student, Teacher, and Admin registration, login, and role-based access control.
 */
@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerIntegrationTest {

    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper mapper;
    @Autowired private UserAccountRepository userRepository;
    @Autowired private StudentRepository studentRepository;

    @BeforeEach
    void clean() {
        userRepository.deleteAll();
        studentRepository.deleteAll();
    }

    private String registerUser(String name, String email, String password, String role, String department) throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("fullName", name);
        body.put("email", email);
        body.put("password", password);
        body.put("role", role);
        body.put("department", department);
        body.put("course", "B.Tech CS");
        body.put("semester", "3rd");
        body.put("gender", "Female");

        MvcResult res = mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andExpect(jsonPath("$.data.user.role").value(role))
                .andReturn();

        JsonNode data = mapper.readTree(res.getResponse().getContentAsString()).path("data");
        return data.path("token").asText();
    }

    private Map<String, Object> sampleStudentPayload(String name, String email) {
        Map<String, Object> m = new HashMap<>();
        m.put("fullName", name);
        m.put("email", email);
        m.put("phone", "+919876543210");
        m.put("gender", "Male");
        m.put("department", "Computer Science");
        m.put("course", "B.Tech");
        m.put("yearOfStudy", 2);
        m.put("semester", "3rd");
        m.put("marks", 85.0);
        return m;
    }

    @Test
    void studentRegistrationAutoCreatesAcademicProfileAndEnforcesReadOnlyAccess() throws Exception {
        String studentToken = registerUser("Neha Verma", "neha@uni.edu", "Secret123", "STUDENT", "Computer Science");

        // /api/auth/me includes linked studentCode & studentRecordId
        mvc.perform(get("/api/auth/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("neha@uni.edu"))
                .andExpect(jsonPath("$.data.role").value("STUDENT"))
                .andExpect(jsonPath("$.data.studentCode").value("STU-0001"))
                .andExpect(jsonPath("$.data.studentRecordId").isNumber());

        // /api/auth/my-student-profile returns the linked StudentDTO
        MvcResult profileRes = mvc.perform(get("/api/auth/my-student-profile")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fullName").value("Neha Verma"))
                .andExpect(jsonPath("$.data.grade").value("A"))
                .andReturn();
        long studentId = mapper.readTree(profileRes.getResponse().getContentAsString()).path("data").path("id").asLong();

        // Student can view student records
        mvc.perform(get("/api/students").header(HttpHeaders.AUTHORIZATION, "Bearer " + studentToken))
                .andExpect(status().isOk());

        // Student CANNOT create, update, or delete student records (403 Forbidden)
        mvc.perform(post("/api/students")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(sampleStudentPayload("Other", "other@uni.edu"))))
                .andExpect(status().isForbidden());

        mvc.perform(put("/api/students/{id}", studentId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(sampleStudentPayload("Neha Hack", "neha@uni.edu"))))
                .andExpect(status().isForbidden());

        mvc.perform(delete("/api/students/{id}", studentId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + studentToken))
                .andExpect(status().isForbidden());

        // Student CANNOT list all user accounts
        mvc.perform(get("/api/auth/users").header(HttpHeaders.AUTHORIZATION, "Bearer " + studentToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void teacherCanCreateAndUpdateStudentsButCannotDelete() throws Exception {
        String teacherToken = registerUser("Prof. Sharma", "sharma@uni.edu", "Teacher123", "TEACHER", "Electronics");

        // Teacher CAN create a student
        MvcResult createRes = mvc.perform(post("/api/students")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + teacherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(sampleStudentPayload("Arjun Rao", "arjun@uni.edu"))))
                .andExpect(status().isCreated())
                .andReturn();
        long studentId = mapper.readTree(createRes.getResponse().getContentAsString()).path("data").path("id").asLong();

        // Teacher CAN update marks/details
        Map<String, Object> updated = sampleStudentPayload("Arjun Rao", "arjun@uni.edu");
        updated.put("marks", 94.0);
        mvc.perform(put("/api/students/{id}", studentId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + teacherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.grade").value("O"));

        // Teacher CANNOT delete a student (403 Forbidden)
        mvc.perform(delete("/api/students/{id}", studentId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + teacherToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message", containsString("Only administrators")));

        // Teacher CANNOT view all user accounts
        mvc.perform(get("/api/auth/users").header(HttpHeaders.AUTHORIZATION, "Bearer " + teacherToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminHasFullAccessToUsersAndStudentDeletion() throws Exception {
        registerUser("Student One", "s1@uni.edu", "Pass1234", "STUDENT", "Computer Science");
        String adminToken = registerUser("Principal Admin", "admin@uni.edu", "Admin1234", "ADMIN", "Administration");

        // Admin can list all registered users
        mvc.perform(get("/api/auth/users").header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(2)));

        // Admin can delete student records
        long studentId = studentRepository.findAll().get(0).getId();
        mvc.perform(delete("/api/students/{id}", studentId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    @Test
    void loginValidatesCredentialsAndRoleSelection() throws Exception {
        registerUser("Prof. Anita", "anita@uni.edu", "CorrectPass1", "TEACHER", "Mathematics");

        // Valid login with matching role
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of(
                                "email", "ANITA@uni.edu",
                                "password", "CorrectPass1",
                                "role", "TEACHER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andExpect(jsonPath("$.data.user.role").value("TEACHER"));

        // Wrong role selected on login tab -> 403 Forbidden
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of(
                                "email", "anita@uni.edu",
                                "password", "CorrectPass1",
                                "role", "ADMIN"))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message", containsString("TEACHER")));

        // Wrong password -> 401 Unauthorized
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of(
                                "email", "anita@uni.edu",
                                "password", "WrongPassword"))))
                .andExpect(status().isUnauthorized());

        // Duplicate registration -> 409 Conflict
        mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of(
                                "fullName", "Duplicate",
                                "email", "anita@uni.edu",
                                "password", "CorrectPass1",
                                "role", "TEACHER"))))
                .andExpect(status().isConflict());
    }

    @Test
    void loginAndRegisterStaticPagesAreServed() throws Exception {
        mvc.perform(get("/pages/login.html")).andExpect(status().isOk());
        mvc.perform(get("/pages/register.html")).andExpect(status().isOk());
    }
}
