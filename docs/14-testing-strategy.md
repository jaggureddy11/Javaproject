# Testing Strategy & Benchmark Verification

## 1. Testing Pyramid & Test Types

```
         /         /   \     E2E Tests (10%) - Cypress/Playwright & Full Docker Flow
       /           /-------\    Integration Tests (30%) - Testcontainers + PostgreSQL/PostGIS
     /             /-----------\  Unit & Constraint Verification Tests (60%) - JUnit 5 + Timefold Verifier
```

---

## 2. Test Execution Categories

### 2.1 Constraint Verification Tests (Timefold)
Uses `ConstraintVerifier` to independently test each hard and soft constraint score calculation:
```java
@Test
void vehicleCapacityConstraint_shouldPenalizeOverweightVehicles() {
    constraintVerifier.verifyThat(RouteConstraintProvider::vehicleCapacity)
        .given(vehicle100kg, order60kg, order50kg) // Total 110kg > 100kg
        .penalizesBy(10); // 10kg excess
}
```

### 2.2 Integration Testing with Testcontainers
Runs true database integration tests against a real containerized PostgreSQL/PostGIS instance:
```java
@Testcontainers
@SpringBootTest
class OrderRepositorySpatialTest {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgis/postgis:16-3.4");

    @Test
    void findOrdersWithinRadius_shouldReturnNearbyOrders() { ... }
}
```

---

## 3. Performance & Solvability Benchmark Suite
Automated test suite validating optimization execution across 4 dataset tiers:
- **Small Suite (10 Orders, 2 Vehicles)**: Must solve in $< 1.0$ s with 0 hard violations.
- **Medium Suite (50 Orders, 5 Vehicles)**: Must solve in $< 5.0$ s with 0 hard violations.
- **Large Suite (100 Orders, 10 Vehicles)**: Must solve in $< 10.0$ s with 0 hard violations.
- **Stress Suite (250 Orders, 25 Vehicles)**: Must solve in $< 30.0$ s.