package com.medicare.backend.dto;

public class MedicationResponse {
    private Long id;
    private String name;
    private String dosage;
    private String time;
    private Integer duration;
    private String createdAt;
    
    // Constructors
    public MedicationResponse() {}
    
    public MedicationResponse(Long id, String name, String dosage, String time, Integer duration, String createdAt) {
        this.id = id;
        this.name = name;
        this.dosage = dosage;
        this.time = time;
        this.duration = duration;
        this.createdAt = createdAt;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }
    
    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }
    
    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }
    
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}