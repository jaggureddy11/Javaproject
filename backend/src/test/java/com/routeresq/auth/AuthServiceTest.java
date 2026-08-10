package com.routeresq.auth;

import com.routeresq.auth.config.JwtProperties;
import com.routeresq.auth.dto.LoginRequest;
import com.routeresq.auth.dto.LoginResponse;
import com.routeresq.auth.jwt.JwtService;
import com.routeresq.auth.service.AuthService;
import com.routeresq.user.model.User;
import com.routeresq.user.model.UserRole;
import com.routeresq.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthServiceTest {

    private AuthenticationManager authenticationManager;
    private UserRepository userRepository;
    private JwtService jwtService;
    private JwtProperties jwtProperties;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        authenticationManager = mock(AuthenticationManager.class);
        userRepository = mock(UserRepository.class);
        jwtProperties = new JwtProperties("super-secret-key-that-is-at-least-256-bits-long-for-hmac-sha-256-security-routeresq-2026", 900000L, 604800000L);
        jwtService = new JwtService(jwtProperties);
        authService = new AuthService(authenticationManager, userRepository, jwtService, jwtProperties);
    }

    @Test
    @DisplayName("AuthService Login Success Returns Valid LoginResponse")
    void testSuccessfulLogin() {
        User user = User.builder()
                .email("dispatcher@routeresq.io")
                .passwordHash("encoded_pass")
                .firstName("Lead")
                .lastName("Dispatcher")
                .role(UserRole.DISPATCHER)
                .active(true)
                .build();
        user.setId(UUID.randomUUID());

        Authentication authResult = new UsernamePasswordAuthenticationToken("dispatcher@routeresq.io", "dispatch123", Collections.emptyList());
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authResult);
        when(userRepository.findByEmail("dispatcher@routeresq.io")).thenReturn(Optional.of(user));

        LoginResponse response = authService.login(new LoginRequest("dispatcher@routeresq.io", "dispatch123"));

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isNotBlank();
        assertThat(response.getExpiresIn()).isEqualTo(900);
        assertThat(response.getUser().getEmail()).isEqualTo("dispatcher@routeresq.io");
        assertThat(response.getUser().getRole()).isEqualTo(UserRole.DISPATCHER);
    }

    @Test
    @DisplayName("AuthService Login Throws Exception on Bad Credentials")
    void testFailedLogin() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        assertThatThrownBy(() -> authService.login(new LoginRequest("dispatcher@routeresq.io", "wrongpassword")))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Invalid credentials");
    }
}
