package com.medicare.backend.dto;

import jakarta.validation.constraints.NotBlank;

public class MedicationRequest {
    
    @NotBlank(message = "Medication name is required")
    private String name;
    
    private String dosage;
    
    @NotBlank(message = "Time is required")
    private String time;
    
    private Integer duration;
    
    // Constructors
    public MedicationRequest() {}
    
    public MedicationRequest(String name, String dosage, String time, Integer duration) {
        this.name = name;
        this.dosage = dosage;
        this.time = time;
        this.duration = duration;
    }
    
    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }
    
    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }
    
    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }
}