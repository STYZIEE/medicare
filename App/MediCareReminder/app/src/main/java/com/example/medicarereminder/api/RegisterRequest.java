package com.example.medicarereminder.api;

public class RegisterRequest {

    private String username;
    private String email;
    private String dob;
    private String password;

    public RegisterRequest(String username, String email, String dob, String password) {
        this.username = username;
        this.email = email;
        this.dob = dob;
        this.password = password;
    }

    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getDob() { return dob; }
    public String getPassword() { return password; }

    // SETTERS
    public void setUsername(String username) { this.username = username; }
    public void setEmail(String email) { this.email = email; }
    public void setDob(String dob) { this.dob = dob; }
    public void setPassword(String password) { this.password = password; }
}
