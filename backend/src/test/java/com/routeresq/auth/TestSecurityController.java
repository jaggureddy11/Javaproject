package com.routeresq.auth;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class TestSecurityController {

    @GetMapping("/api/v1/orders")
    public ResponseEntity<Map<String, String>> getOrders() {
        return ResponseEntity.ok(Map.of("status", "success", "data", "orders list"));
    }

    @GetMapping("/api/v1/optimization")
    public ResponseEntity<Map<String, String>> getOptimization() {
        return ResponseEntity.ok(Map.of("status", "success", "data", "optimization run"));
    }

    @GetMapping("/api/v1/admin/users")
    public ResponseEntity<Map<String, String>> getUsers() {
        return ResponseEntity.ok(Map.of("status", "success", "data", "users list"));
    }
}
