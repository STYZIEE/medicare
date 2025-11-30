package com.medicare.backend.service;

import com.medicare.backend.dto.MedicationRequest;
import com.medicare.backend.dto.MedicationResponse;
import com.medicare.backend.entity.Medication;
import com.medicare.backend.entity.User;
import com.medicare.backend.repository.MedicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MedicationService {

    @Autowired
    private MedicationRepository medicationRepository;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    public List<MedicationResponse> getUserMedications(String userEmail) {
        User user = customUserDetailsService.loadUserEntityByEmail(userEmail);
        List<Medication> medications = medicationRepository.findByUser(user);
        
        return medications.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public MedicationResponse addMedication(String userEmail, MedicationRequest medicationRequest) {
        User user = customUserDetailsService.loadUserEntityByEmail(userEmail);
        
        Medication medication = new Medication(
            medicationRequest.getName(),
            medicationRequest.getDosage(),
            medicationRequest.getTime(),
            medicationRequest.getDuration(),
            user
        );
        
        Medication savedMedication = medicationRepository.save(medication);
        return convertToResponse(savedMedication);
    }

    public MedicationResponse updateMedication(String userEmail, Long medicationId, MedicationRequest medicationRequest) {
        User user = customUserDetailsService.loadUserEntityByEmail(userEmail);
        
        Medication medication = medicationRepository.findById(medicationId)
                .orElseThrow(() -> new RuntimeException("Medication not found"));
        
        // Check if medication belongs to the user
        if (!medication.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("You can only update your own medications");
        }
        
        // Update medication fields
        medication.setName(medicationRequest.getName());
        medication.setDosage(medicationRequest.getDosage());
        medication.setTime(medicationRequest.getTime());
        medication.setDuration(medicationRequest.getDuration());
        
        Medication updatedMedication = medicationRepository.save(medication);
        return convertToResponse(updatedMedication);
    }

    public void deleteMedication(String userEmail, Long medicationId) {
        User user = customUserDetailsService.loadUserEntityByEmail(userEmail);
        
        Medication medication = medicationRepository.findById(medicationId)
                .orElseThrow(() -> new RuntimeException("Medication not found"));
        
        // Check if medication belongs to the user
        if (!medication.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("You can only delete your own medications");
        }
        
        medicationRepository.delete(medication);
    }

    private MedicationResponse convertToResponse(Medication medication) {
        String createdAt = medication.getCreatedAt() != null 
            ? medication.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            : null;
            
        return new MedicationResponse(
            medication.getId(),
            medication.getName(),
            medication.getDosage(),
            medication.getTime(),
            medication.getDuration(),
            createdAt
        );
    }
}