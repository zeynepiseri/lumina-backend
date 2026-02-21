package com.lumina.backend.modules.medicalrecord.repository;

import com.lumina.backend.modules.medicalrecord.entity.MedicalResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MedicalResultRepository extends JpaRepository<MedicalResult, Long> {
    List<MedicalResult> findByPatientId(Long patientId);
}