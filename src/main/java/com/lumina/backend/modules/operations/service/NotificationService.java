package com.lumina.backend.modules.operations.service;

import com.lumina.backend.modules.operations.entity.Notification;
import com.lumina.backend.modules.auth.entity.User;
import com.lumina.backend.modules.operations.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Import Transactional

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
 
    @Transactional
    public void sendNotification(User user, String title, String message, String type) {
         Notification notification = Notification.builder()
                .user(user)
                .title(title)
                .message(message)
                .type(type)
                .build();

        notificationRepository.save(notification);
         sendPushNotification(user, title, message);
         sendEmail(user, title, message);
    }

    private void sendPushNotification(User user, String title, String body) {
         log.info("[PUSH] To: {} | Title: {} | Body: {}", user.getEmail(), title, body);
    }

    private void sendEmail(User user, String subject, String content) {
         log.info("[EMAIL] To: {} | Subject: {} | Content: {}", user.getEmail(), subject, content);
    }

    public void markAsRead(Long notificationId) {
        Notification n = notificationRepository.findById(notificationId).orElseThrow();
        n.setRead(true);
        notificationRepository.save(n);
    }
}