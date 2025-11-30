package com.example.medicarereminder.api;

import com.example.medicarereminder.model.User;

public class AuthResponse {
    private boolean success;
    private String message;
    private String token;
    private User user;

    public AuthResponse() {}

    // Getters and Setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}