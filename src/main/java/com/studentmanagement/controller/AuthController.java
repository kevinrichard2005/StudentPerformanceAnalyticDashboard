package com.studentmanagement.controller;

import com.studentmanagement.dto.*;
import com.studentmanagement.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST endpoints for authentication, registration, and role profile inspection.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponseDTO>> register(@Valid @RequestBody RegisterRequestDTO request) {
        AuthResponseDTO response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Account registered successfully", response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponseDTO>> login(@Valid @RequestBody LoginRequestDTO request) {
        AuthResponseDTO response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileDTO>> getCurrentUser(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        return ResponseEntity.ok(ApiResponse.success(authService.getCurrentUser(authHeader)));
    }

    @GetMapping("/my-student-profile")
    public ResponseEntity<ApiResponse<StudentDTO>> getMyStudentProfile(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        return ResponseEntity.ok(ApiResponse.success(authService.getMyStudentProfile(authHeader)));
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserProfileDTO>>> getAllUsers(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        return ResponseEntity.ok(ApiResponse.success(authService.getAllUsers(authHeader)));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully", null));
    }
}
