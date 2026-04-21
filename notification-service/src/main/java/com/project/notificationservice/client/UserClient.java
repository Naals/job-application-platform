package com.project.notificationservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-auth-service", url = "${user-auth-service.url}")
public interface UserClient {

    @GetMapping("/api/v1/users/{id}")
    UserDto getUserById(@PathVariable String id);

    record UserDto(String id, String email, String firstName, String lastName) {}
}
