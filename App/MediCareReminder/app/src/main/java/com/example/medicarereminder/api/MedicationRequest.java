package com.example.medicarereminder.api;

public class MedicationRequest {
    private String name;
    private String dosage;
    private String time;
    private Integer duration;

    public MedicationRequest(String name, String dosage, String time, Integer duration) {
        this.name = name;
        this.dosage = dosage;
        this.time = time;
        this.duration = duration;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }
}