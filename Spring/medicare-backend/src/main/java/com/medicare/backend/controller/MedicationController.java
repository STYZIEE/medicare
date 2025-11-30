package com.medicare.backend.controller;

import com.medicare.backend.dto.MedicationRequest;
import com.medicare.backend.dto.MedicationResponse;
import com.medicare.backend.service.MedicationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/medications")
@CrossOrigin(origins = "*")
public class MedicationController {

    @Autowired
    private MedicationService medicationService;

    @GetMapping
    public ResponseEntity<List<MedicationResponse>> getUserMedications(Authentication authentication) {
        String userEmail = authentication.getName();
        List<MedicationResponse> medications = medicationService.getUserMedications(userEmail);
        return ResponseEntity.ok(medications);
    }

    @PostMapping
    public ResponseEntity<MedicationResponse> addMedication(
            Authentication authentication,
            @Valid @RequestBody MedicationRequest medicationRequest) {
        String userEmail = authentication.getName();
        MedicationResponse medication = medicationService.addMedication(userEmail, medicationRequest);
        return ResponseEntity.ok(medication);
    }

    @PutMapping("/{medicationId}")
    public ResponseEntity<MedicationResponse> updateMedication(
            Authentication authentication,
            @PathVariable Long medicationId,
            @Valid @RequestBody MedicationRequest medicationRequest) {
        String userEmail = authentication.getName();
        MedicationResponse medication = medicationService.updateMedication(userEmail, medicationId, medicationRequest);
        return ResponseEntity.ok(medication);
    }

    @DeleteMapping("/{medicationId}")
    public ResponseEntity<?> deleteMedication(
            Authentication authentication,
            @PathVariable Long medicationId) {
        String userEmail = authentication.getName();
        medicationService.deleteMedication(userEmail, medicationId);
        return ResponseEntity.ok().build();
    }
}