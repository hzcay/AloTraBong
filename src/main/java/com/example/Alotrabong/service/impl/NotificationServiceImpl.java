package com.example.Alotrabong.service.impl;

import com.example.Alotrabong.entity.Notification;
import com.example.Alotrabong.exception.ResourceNotFoundException;
import com.example.Alotrabong.repository.NotificationRepository;
import com.example.Alotrabong.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Notification> getNotificationsByUser(String userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    public Notification createNotification(Notification notification) {
        log.info("Creating notification for user: {}", notification.getUserId());
        Notification savedNotification = notificationRepository.save(notification);
        log.info("Notification created successfully: {}", savedNotification.getNotificationId());
        return savedNotification;
    }

    @Override
    public Notification markAsRead(String notificationId) {
        log.info("Marking notification as read: {}", notificationId);
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        
        notification.setIsRead(true);
        notification = notificationRepository.save(notification);
        log.info("Notification marked as read: {}", notificationId);
        return notification;
    }

    @Override
    public void deleteNotification(String notificationId) {
        log.info("Deleting notification: {}", notificationId);
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        
        notificationRepository.delete(notification);
        log.info("Notification deleted: {}", notificationId);
    }
}
