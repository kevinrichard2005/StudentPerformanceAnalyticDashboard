package com.studentmanagement.service;

import com.studentmanagement.dto.*;
import com.studentmanagement.entity.Role;
import com.studentmanagement.entity.UserAccount;
import com.studentmanagement.exception.DuplicateResourceException;
import com.studentmanagement.exception.ForbiddenException;
import com.studentmanagement.exception.ResourceNotFoundException;
import com.studentmanagement.exception.UnauthorizedException;
import com.studentmanagement.repository.UserAccountRepository;
import com.studentmanagement.util.PasswordUtil;
import com.studentmanagement.util.TokenUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

/**
 * Handles registration, authentication, and role-based profile lookup for
 * Student, Teacher, and Admin accounts.
 */
@Service
@Transactional
public class AuthService {

    private final UserAccountRepository userRepository;
    private final StudentService studentService;
    private final TokenUtil tokenUtil;

    public AuthService(UserAccountRepository userRepository,
                       StudentService studentService,
                       TokenUtil tokenUtil) {
        this.userRepository = userRepository;
        this.studentService = studentService;
        this.tokenUtil = tokenUtil;
    }

    public AuthResponseDTO register(RegisterRequestDTO req) {
        String email = normalizeEmail(req.getEmail());
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new DuplicateResourceException("An account with email '" + email + "' already exists");
        }

        String department = (req.getDepartment() != null && !req.getDepartment().isBlank())
                ? req.getDepartment().trim()
                : "Computer Science";

        UserAccount user = new UserAccount();
        user.setFullName(req.getFullName().trim());
        user.setEmail(email);
        user.setPasswordHash(PasswordUtil.hash(req.getPassword()));
        user.setRole(req.getRole());
        user.setDepartment(department);

        UserAccount saved = userRepository.save(user);

        // If registering as a STUDENT and no student record exists yet for this email, create one automatically
        if (saved.getRole() == Role.STUDENT && studentService.findStudentByEmail(email).isEmpty()) {
            StudentDTO studentDto = new StudentDTO();
            studentDto.setFullName(saved.getFullName());
            studentDto.setEmail(email);
            studentDto.setGender(validGender(req.getGender()));
            studentDto.setDepartment(department);
            studentDto.setCourse(req.getCourse() != null && !req.getCourse().isBlank() ? req.getCourse().trim() : "B.Tech");
            studentDto.setSemester(req.getSemester() != null && !req.getSemester().isBlank() ? req.getSemester().trim() : "1st");
            studentDto.setYearOfStudy(1);
            studentDto.setMarks(75.0);
            studentService.createStudent(studentDto);
        }

        String token = tokenUtil.generateToken(saved.getId(), saved.getEmail(), saved.getRole());
        return new AuthResponseDTO(token, toProfileDTO(saved));
    }

    @Transactional(readOnly = true)
    public AuthResponseDTO login(LoginRequestDTO req) {
        String email = normalizeEmail(req.getEmail());
        UserAccount user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (!PasswordUtil.verify(req.getPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        if (req.getRole() != null && req.getRole() != user.getRole()) {
            throw new ForbiddenException(
                    "This account is registered as " + user.getRole() + ". Please switch to the " + user.getRole() + " role tab.");
        }

        String token = tokenUtil.generateToken(user.getId(), user.getEmail(), user.getRole());
        return new AuthResponseDTO(token, toProfileDTO(user));
    }

    @Transactional(readOnly = true)
    public UserProfileDTO getCurrentUser(String authHeader) {
        TokenUtil.TokenPayload payload = tokenUtil.verifyToken(authHeader);
        UserAccount user = userRepository.findById(payload.userId())
                .orElseThrow(() -> new UnauthorizedException("User account no longer exists"));
        return toProfileDTO(user);
    }

    @Transactional(readOnly = true)
    public StudentDTO getMyStudentProfile(String authHeader) {
        TokenUtil.TokenPayload payload = tokenUtil.verifyToken(authHeader);
        return studentService.findStudentByEmail(payload.email())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No student academic record linked to email: " + payload.email()));
    }

    @Transactional(readOnly = true)
    public List<UserProfileDTO> getAllUsers(String authHeader) {
        TokenUtil.TokenPayload payload = tokenUtil.verifyToken(authHeader);
        if (payload.role() != Role.ADMIN) {
            throw new ForbiddenException("Only administrators can view all registered user accounts");
        }
        return userRepository.findAllByOrderByCreatedAtDesc().stream().map(this::toProfileDTO).toList();
    }

    public void ensureDefaultAccount(String fullName, String email, String rawPassword, Role role, String department) {
        String normalized = normalizeEmail(email);
        if (userRepository.existsByEmailIgnoreCase(normalized)) {
            return;
        }
        UserAccount user = new UserAccount();
        user.setFullName(fullName);
        user.setEmail(normalized);
        user.setPasswordHash(PasswordUtil.hash(rawPassword));
        user.setRole(role);
        user.setDepartment(department);
        userRepository.save(user);
    }

    private UserProfileDTO toProfileDTO(UserAccount user) {
        UserProfileDTO dto = new UserProfileDTO();
        dto.setId(user.getId());
        dto.setFullName(user.getFullName());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        dto.setDepartment(user.getDepartment());
        dto.setCreatedAt(user.getCreatedAt());

        if (user.getRole() == Role.STUDENT) {
            studentService.findStudentByEmail(user.getEmail()).ifPresent(s -> {
                dto.setStudentRecordId(s.getId());
                dto.setStudentCode(s.getStudentCode());
            });
        }
        return dto;
    }

    private static String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    private static String validGender(String gender) {
        if ("Male".equalsIgnoreCase(gender)) return "Male";
        if ("Female".equalsIgnoreCase(gender)) return "Female";
        return "Other";
    }
}
