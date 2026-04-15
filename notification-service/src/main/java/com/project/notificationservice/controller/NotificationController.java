package com.project.notificationservice.controller;

import com.project.notificationservice.service.EmailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications")
public class NotificationController {

    private final EmailService emailService;

    @PostMapping("/test-email")
    @Operation(summary = "Send a test email (admin only)")
    public ResponseEntity<Map<String, String>> sendTestEmail(
            @RequestParam String to,
            @RequestHeader("X-User-Roles") String roles) {

        if (!roles.contains("ROLE_ADMIN")) {
            return ResponseEntity.status(403)
                    .body(Map.of("error", "Admin role required"));
        }

        emailService.sendHtmlEmail(
                to,
                "Test email from Job Platform",
                "welcome",
                Map.of(
                        "firstName", "Test",
                        "lastName",  "User",
                        "userId",    "test-id"
                )
        );

        return ResponseEntity.ok(Map.of(
                "status",  "queued",
                "message", "Test email sent to " + to
        ));
    }

    @GetMapping("/health")
    @Operation(summary = "Notification service health check")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "service", "notification-service",
                "status",  "UP"
        ));
    }
}
