package com.routeresq.auth;

import com.routeresq.auth.config.JwtProperties;
import com.routeresq.auth.jwt.JwtService;
import com.routeresq.user.model.User;
import com.routeresq.user.model.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwtService;
    private JwtProperties jwtProperties;

    @BeforeEach
    void setUp() {
        jwtProperties = new JwtProperties(
                "super-secret-key-that-is-at-least-256-bits-long-for-hmac-sha-256-security-routeresq-2026",
                900000L,
                604800000L
        );
        jwtService = new JwtService(jwtProperties);
    }

    @Test
    @DisplayName("Generate & Validate Valid JWT Token")
    void testGenerateAndValidateToken() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .email("dispatcher@routeresq.io")
                .role(UserRole.DISPATCHER)
                .build();
        user.setId(userId);

        String token = jwtService.generateToken(user);

        assertThat(token).isNotBlank();
        assertThat(jwtService.validateToken(token)).isTrue();
        assertThat(jwtService.getUserIdFromToken(token)).isEqualTo(userId);
        assertThat(jwtService.getEmailFromToken(token)).isEqualTo("dispatcher@routeresq.io");
        assertThat(jwtService.getRoleFromToken(token)).isEqualTo(UserRole.DISPATCHER);
    }

    @Test
    @DisplayName("Reject Tampered or Invalid JWT Token")
    void testInvalidToken() {
        String invalidToken = "eyJhbGciOiJIUzI1NiJ9.invalid.payload";
        assertThat(jwtService.validateToken(invalidToken)).isFalse();
    }

    @Test
    @DisplayName("Reject Expired JWT Token")
    void testExpiredToken() {
        JwtProperties expiredProps = new JwtProperties(
                "super-secret-key-that-is-at-least-256-bits-long-for-hmac-sha-256-security-routeresq-2026",
                -1000L, // Already expired 1 sec ago
                -1000L
        );
        JwtService expiredJwtService = new JwtService(expiredProps);

        User user = User.builder()
                .email("admin@routeresq.io")
                .role(UserRole.ADMIN)
                .build();
        user.setId(UUID.randomUUID());

        String token = expiredJwtService.generateToken(user);
        assertThat(expiredJwtService.validateToken(token)).isFalse();
    }
}
