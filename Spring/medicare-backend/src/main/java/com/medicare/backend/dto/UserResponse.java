package com.medicare.backend.dto;

public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private String dateOfBirth;
    
    // Constructors
    public UserResponse() {}
    
    public UserResponse(Long id, String username, String email, String dateOfBirth) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.dateOfBirth = dateOfBirth;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }
}