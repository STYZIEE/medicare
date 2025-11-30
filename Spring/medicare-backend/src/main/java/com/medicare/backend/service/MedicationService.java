package com.medicare.backend.service;

import com.medicare.backend.dto.MedicationRequest;
import com.medicare.backend.dto.MedicationResponse;
import com.medicare.backend.entity.Medication;
import com.medicare.backend.entity.User;
import com.medicare.backend.repository.MedicationRepository;
import com.medicare.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MedicationService {

    @Autowired
    private MedicationRepository medicationRepository;

    @Autowired
    private UserRepository userRepository;

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

    private MedicationResponse convertToResponse(Medication medication) {
        return new MedicationResponse(
            medication.getId(),
            medication.getName(),
            medication.getDosage(),
            medication.getTime(),
            medication.getDuration(),
            medication.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        );
    }
}