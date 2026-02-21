package com.lumina.backend.modules.clinical.repository;

import com.lumina.backend.modules.clinical.entity.LabTechnician;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LabTechnicianRepository extends JpaRepository<LabTechnician, Long> {
}