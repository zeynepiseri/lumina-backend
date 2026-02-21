package com.lumina.backend.modules.operations.repository;

import com.lumina.backend.modules.operations.entity.Announcement;
import com.lumina.backend.modules.auth.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {
     @Query("SELECT a FROM Announcement a WHERE a.isActive = true AND (a.targetRole IS NULL OR a.targetRole = :role) ORDER BY a.createdAt DESC")
    List<Announcement> findAllActiveForRole(Role role);
     List<Announcement> findAllByOrderByCreatedAtDesc();
}