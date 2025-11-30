package com.medicare.backend.repository;

import com.medicare.backend.entity.Medication;
import com.medicare.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicationRepository extends JpaRepository<Medication, Long> {
    List<Medication> findByUser(User user);
    List<Medication> findByUserId(Long userId);
}