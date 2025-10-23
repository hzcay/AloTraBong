package com.example.Alotrabong.service;

import com.example.Alotrabong.entity.Notification;

import java.util.List;

public interface NotificationService {
    
    List<Notification> getNotificationsByUser(String userId);
    
    Notification createNotification(Notification notification);
    
    Notification markAsRead(String notificationId);
    
    void deleteNotification(String notificationId);
}