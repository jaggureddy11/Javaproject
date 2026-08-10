package com.routeresq.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.routeresq.auth.dto.LoginRequest;
import com.routeresq.domain.DockerAvailableCondition;
import com.routeresq.user.model.User;
import com.routeresq.user.model.UserRole;
import com.routeresq.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ExtendWith(DockerAvailableCondition.class)
@ActiveProfiles("test")
@Transactional
class SecurityIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            DockerImageName.parse("postgis/postgis:16-3.4").asCompatibleSubstituteFor("postgres")
    )
            .withDatabaseName("routeresq_sec_test_db")
            .withUsername("test_user")
            .withPassword("test_password");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        // Seed users with BCrypt password hashes
        userRepository.save(User.builder()
                .email("admin@routeresq.io")
                .passwordHash(passwordEncoder.encode("admin123"))
                .firstName("Admin")
                .lastName("User")
                .role(UserRole.ADMIN)
                .active(true)
                .build());

        userRepository.save(User.builder()
                .email("dispatcher@routeresq.io")
                .passwordHash(passwordEncoder.encode("dispatch123"))
                .firstName("Dispatcher")
                .lastName("User")
                .role(UserRole.DISPATCHER)
                .active(true)
                .build());

        userRepository.save(User.builder()
                .email("driver@routeresq.io")
                .passwordHash(passwordEncoder.encode("driver123"))
                .firstName("Driver")
                .lastName("User")
                .role(UserRole.DRIVER)
                .active(true)
                .build());
    }

    @Test
    @DisplayName("POST /api/v1/auth/login - Valid Credentials Returns JWT & User DTO")
    void testSuccessfulLogin() throws Exception {
        LoginRequest loginRequest = new LoginRequest("dispatcher@routeresq.io", "dispatch123");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", notNullValue()))
                .andExpect(jsonPath("$.tokenType", is("Bearer")))
                .andExpect(jsonPath("$.expiresIn", is(900)))
                .andExpect(jsonPath("$.user.email", is("dispatcher@routeresq.io")))
                .andExpect(jsonPath("$.user.role", is("DISPATCHER")));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login - Invalid Password Returns 401 Unauthorized JSON")
    void testInvalidPasswordLogin() throws Exception {
        LoginRequest loginRequest = new LoginRequest("dispatcher@routeresq.io", "wrongpassword");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)))
                .andExpect(jsonPath("$.code", is("UNAUTHORIZED")));
    }

    @Test
    @DisplayName("GET Protected API without Bearer Token Returns 401 Unauthorized JSON")
    void testUnauthenticatedAccess() throws Exception {
        mockMvc.perform(get("/api/v1/orders"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)))
                .andExpect(jsonPath("$.code", is("UNAUTHORIZED")))
                .andExpect(jsonPath("$.message", containsString("Authentication is required")));
    }

    @Test
    @DisplayName("GET Admin Endpoint with DRIVER Role Returns 403 Forbidden JSON")
    void testForbiddenRoleAccess() throws Exception {
        // Login as Driver
        LoginRequest loginRequest = new LoginRequest("driver@routeresq.io", "driver123");
        String responseStr = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String driverToken = objectMapper.readTree(responseStr).get("accessToken").asText();

        // Attempt to access Admin-only endpoint with Driver token
        mockMvc.perform(get("/api/v1/admin/users")
                        .header("Authorization", "Bearer " + driverToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)))
                .andExpect(jsonPath("$.code", is("FORBIDDEN")))
                .andExpect(jsonPath("$.message", containsString("You do not have permission")));
    }

    @Test
    @DisplayName("GET Admin Endpoint with ADMIN Role Returns 200 OK")
    void testAuthorizedAdminAccess() throws Exception {
        // Login as Admin
        LoginRequest loginRequest = new LoginRequest("admin@routeresq.io", "admin123");
        String responseStr = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String adminToken = objectMapper.readTree(responseStr).get("accessToken").asText();

        mockMvc.perform(get("/api/v1/admin/users")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")));
    }
}
