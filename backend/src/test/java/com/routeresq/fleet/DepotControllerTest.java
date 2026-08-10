package com.routeresq.fleet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.routeresq.domain.DockerAvailableCondition;
import com.routeresq.fleet.dto.DepotRequest;
import com.routeresq.shared.dto.LocationDto;
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

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ExtendWith(DockerAvailableCondition.class)
@ActiveProfiles("test")
@Transactional
class DepotControllerTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            DockerImageName.parse("postgis/postgis:16-3.4").asCompatibleSubstituteFor("postgres")
    )
            .withDatabaseName("routeresq_depot_test_db")
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

    private String adminToken;

    @BeforeEach
    void setUp() throws Exception {
        userRepository.deleteAll();

        userRepository.save(User.builder()
                .email("admin@routeresq.io")
                .passwordHash(passwordEncoder.encode("admin123"))
                .firstName("Admin")
                .lastName("User")
                .role(UserRole.ADMIN)
                .active(true)
                .build());

        String loginBody = "{\"email\":\"admin@routeresq.io\",\"password\":\"admin123\"}";
        String response = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        adminToken = objectMapper.readTree(response).get("accessToken").asText();
    }

    @Test
    @DisplayName("Depot CRUD Operations Integration Test")
    void testDepotCrud() throws Exception {
        // 1. Create Depot
        DepotRequest request = new DepotRequest("North Hub", "123 North St", new LocationDto(41.9000, -87.6500));
        String response = mockMvc.perform(post("/api/v1/depots")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name", is("North Hub")))
                .andExpect(jsonPath("$.location.latitude", is(41.9000)))
                .andReturn().getResponse().getContentAsString();

        String depotId = objectMapper.readTree(response).get("id").asText();

        // 2. Get Depot by ID
        mockMvc.perform(get("/api/v1/depots/" + depotId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("North Hub")));

        // 3. List Depots
        mockMvc.perform(get("/api/v1/depots")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));

        // 4. Update Depot
        request.setName("North Logistics Hub");
        mockMvc.perform(patch("/api/v1/depots/" + depotId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("North Logistics Hub")));

        // 5. Delete Depot
        mockMvc.perform(delete("/api/v1/depots/" + depotId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        // 6. Verify 404
        mockMvc.perform(get("/api/v1/depots/" + depotId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code", is("RESOURCE_NOT_FOUND")));
    }
}
