package com.example.medicarereminder.model;

import com.google.gson.annotations.SerializedName;

public class Medication {
    @SerializedName("id")
    private Long id;

    @SerializedName("name")
    private String name;

    @SerializedName("dosage")
    private String dosage;

    @SerializedName("time")
    private String time;

    @SerializedName("duration")
    private Integer duration;

    @SerializedName("createdAt")
    private String createdAt;

    // LOCAL FIELDS ONLY - NOT SENT TO BACKEND
    private transient boolean notificationEnabled = true;
    private transient int notificationHour = 8;
    private transient int notificationMinute = 0;
    private transient boolean[] notificationDays = {true, true, true, true, true, true, true}; // Sun-Sat

    public Medication() {}

    public Medication(Long id, String name, String dosage, String time, Integer duration, String createdAt) {
        this.id = id;
        this.name = name;
        this.dosage = dosage;
        this.time = time;
        this.duration = duration;
        this.createdAt = createdAt;
        parseTimeString(); // Parse time when creating object
    }

    // Getters and Setters for backend fields...
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }

    public String getTime() { return time; }
    public void setTime(String time) {
        this.time = time;
        parseTimeString(); // Re-parse when time changes
    }

    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    // LOCAL GETTERS AND SETTERS ONLY
    public boolean isNotificationEnabled() { return notificationEnabled; }
    public void setNotificationEnabled(boolean notificationEnabled) { this.notificationEnabled = notificationEnabled; }

    public int getNotificationHour() { return notificationHour; }
    public void setNotificationHour(int notificationHour) { this.notificationHour = notificationHour; }

    public int getNotificationMinute() { return notificationMinute; }
    public void setNotificationMinute(int notificationMinute) { this.notificationMinute = notificationMinute; }

    public boolean[] getNotificationDays() { return notificationDays; }
    public void setNotificationDays(boolean[] notificationDays) { this.notificationDays = notificationDays; }
    public void setNotificationDay(int dayIndex, boolean enabled) {
        if (dayIndex >= 0 && dayIndex < 7) {
            notificationDays[dayIndex] = enabled;
        }
    }

    private void parseTimeString() {
        if (time != null && !time.isEmpty()) {
            try {
                String cleanTime = time.replaceAll("[^0-9:]", "");
                String[] parts = cleanTime.split(":");
                if (parts.length >= 2) {
                    notificationHour = Integer.parseInt(parts[0]);
                    notificationMinute = Integer.parseInt(parts[1]);
                    if (time.toLowerCase().contains("pm") && notificationHour < 12) {
                        notificationHour += 12;
                    }
                    if (time.toLowerCase().contains("am") && notificationHour == 12) {
                        notificationHour = 0;
                    }
                }
            } catch (NumberFormatException e) {
            }
        }
    }
}