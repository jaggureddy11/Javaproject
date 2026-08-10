package com.routeresq.auth.service;

import com.routeresq.auth.config.JwtProperties;
import com.routeresq.auth.dto.LoginRequest;
import com.routeresq.auth.dto.LoginResponse;
import com.routeresq.auth.dto.UserResponse;
import com.routeresq.auth.jwt.JwtService;
import com.routeresq.user.model.User;
import com.routeresq.user.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;

    public AuthService(AuthenticationManager authenticationManager,
                       UserRepository userRepository,
                       JwtService jwtService,
                       JwtProperties jwtProperties) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.jwtProperties = jwtProperties;
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UsernameNotFoundException("Invalid user credentials"));

        String token = jwtService.generateToken(user);
        long expiresInSeconds = jwtProperties.getExpirationMs() / 1000;

        return new LoginResponse(token, expiresInSeconds, UserResponse.fromEntity(user));
    }
}
