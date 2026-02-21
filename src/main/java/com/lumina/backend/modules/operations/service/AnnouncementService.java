package com.lumina.backend.modules.operations.service;

import com.lumina.backend.modules.operations.entity.Announcement;
import com.lumina.backend.modules.auth.entity.Role;
import com.lumina.backend.modules.auth.entity.User;
import com.lumina.backend.modules.operations.repository.AnnouncementRepository;
import com.lumina.backend.modules.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AnnouncementService {

    private final AnnouncementRepository announcementRepository;
    private final UserRepository userRepository;

    public Announcement createAnnouncement(String title, String content, Role targetRole) {
        Announcement announcement = Announcement.builder()
                .title(title)
                .content(content)
                .targetRole(targetRole)
                .build();
        return announcementRepository.save(announcement);
    }

    public List<Announcement> getMyAnnouncements() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow();
         if (user.getRole() == Role.ADMIN) {
            return announcementRepository.findAllByOrderByCreatedAtDesc();
        }
        return announcementRepository.findAllActiveForRole(user.getRole());
    }

    public void deleteAnnouncement(Long id) {
        announcementRepository.deleteById(id);
    }
}