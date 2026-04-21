package com.project.userauthservice.controller;

import com.project.userauthservice.domain.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users")
public class UserController {

    private final UserRepository userRepository;

    @GetMapping("/me")
    @Operation(summary = "Get current user profile")
    public ResponseEntity<Map<String, Object>> getMe(
            @RequestHeader("X-User-Id") UUID userId) {
        return userRepository.findById(userId)
                .map(u -> ResponseEntity.ok(Map.of(
                        "id",        u.getId().toString(),
                        "email",     u.getEmail(),
                        "firstName", u.getFirstName(),
                        "lastName",  u.getLastName(),
                        "roles",     u.getRoles().stream().map(Enum::name).toList(),
                        "status",    u.getStatus().name()
                )))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID (internal — used by notification-service)")
    public ResponseEntity<Map<String, String>> getById(@PathVariable UUID id) {
        return userRepository.findById(id)
                .map(u -> ResponseEntity.ok(Map.of(
                        "id",        u.getId().toString(),
                        "email",     u.getEmail(),
                        "firstName", u.getFirstName(),
                        "lastName",  u.getLastName()
                )))
                .orElse(ResponseEntity.notFound().build());
    }
}
