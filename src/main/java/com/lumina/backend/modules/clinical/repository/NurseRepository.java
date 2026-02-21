package com.lumina.backend.modules.clinical.repository;

import com.lumina.backend.modules.clinical.entity.Nurse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NurseRepository extends JpaRepository<Nurse, Long> {
}