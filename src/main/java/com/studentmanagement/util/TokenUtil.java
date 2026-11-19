package com.studentmanagement.util;

import com.studentmanagement.entity.Role;
import com.studentmanagement.exception.UnauthorizedException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;

/**
 * Stateless HMAC-SHA256 token signer and verifier for Student, Teacher, and Admin sessions.
 */
@Component
public class TokenUtil {

    private static final String HMAC_ALG = "HmacSHA256";
    private static final long EXPIRATION_SECONDS = 86400L; // 24 hours

    private final byte[] secretBytes;

    public TokenUtil(@Value("${app.auth.secret:StudentManagementSystemDefaultHmacSecretKey2026!}") String secret) {
        this.secretBytes = secret.getBytes(StandardCharsets.UTF_8);
    }

    public record TokenPayload(Long userId, String email, Role role, long expiresAtEpochSec) { }

    public String generateToken(Long userId, String email, Role role) {
        long exp = Instant.now().getEpochSecond() + EXPIRATION_SECONDS;
        String payload = userId + "|" + email + "|" + role.name() + "|" + exp;
        String encodedPayload = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(payload.getBytes(StandardCharsets.UTF_8));
        String signature = sign(encodedPayload);
        return encodedPayload + "." + signature;
    }

    public TokenPayload verifyToken(String token) {
        if (token == null || token.isBlank()) {
            throw new UnauthorizedException("Authentication token is missing");
        }
        String raw = token.startsWith("Bearer ") ? token.substring(7).trim() : token.trim();
        int dot = raw.indexOf('.');
        if (dot <= 0 || dot == raw.length() - 1) {
            throw new UnauthorizedException("Malformed authentication token");
        }
        String encodedPayload = raw.substring(0, dot);
        String signature = raw.substring(dot + 1);
        String expectedSig = sign(encodedPayload);
        if (!MessageDigest.isEqual(
                expectedSig.getBytes(StandardCharsets.UTF_8),
                signature.getBytes(StandardCharsets.UTF_8))) {
            throw new UnauthorizedException("Invalid authentication token signature");
        }

        try {
            String decoded = new String(Base64.getUrlDecoder().decode(encodedPayload), StandardCharsets.UTF_8);
            String[] parts = decoded.split("\\|");
            if (parts.length != 4) {
                throw new UnauthorizedException("Invalid token claims");
            }
            Long userId = Long.parseLong(parts[0]);
            String email = parts[1];
            Role role = Role.valueOf(parts[2]);
            long exp = Long.parseLong(parts[3]);
            if (Instant.now().getEpochSecond() > exp) {
                throw new UnauthorizedException("Session has expired. Please sign in again.");
            }
            return new TokenPayload(userId, email, role, exp);
        } catch (UnauthorizedException e) {
            throw e;
        } catch (Exception e) {
            throw new UnauthorizedException("Invalid authentication token");
        }
    }

    private String sign(String data) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALG);
            mac.init(new SecretKeySpec(secretBytes, HMAC_ALG));
            byte[] sig = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(sig);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to sign token", e);
        }
    }
}
