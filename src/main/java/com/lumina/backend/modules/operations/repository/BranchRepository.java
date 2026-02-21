package com.lumina.backend.modules.operations.repository;

import com.lumina.backend.modules.operations.entity.Branch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BranchRepository extends JpaRepository<Branch, Long> {
}