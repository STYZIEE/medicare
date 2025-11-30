package com.medicare.backend.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {

    @GetMapping("/public")
    public String publicEndpoint() {
        return "This is a public endpoint - accessible without authentication";
    }

    @GetMapping("/hello")
    public String helloEndpoint() {
        return "Hello World! This endpoint is completely open and requires no authentication.";
    }

    @GetMapping("/protected")
    public String protectedEndpoint(Authentication authentication) {
        return "This is a protected endpoint. Authenticated user: " + authentication.getName();
    }
}