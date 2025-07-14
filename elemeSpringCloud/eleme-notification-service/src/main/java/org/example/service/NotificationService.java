package org.example.service;

import org.example.dto.NotificationResponseDTO;
import org.example.entity.Notification;
import org.example.entity.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 通知服务接口
 */
public interface NotificationService {
    
    /**
     * 创建通知记录
     */
    Notification createNotification(NotificationType type, String receiver, String title, String content, 
                                  String businessType, String businessId, Integer userId);
    
    /**
     * 更新通知状态
     */
    void updateNotificationStatus(Long notificationId, String status, String failureReason);
    
    /**
     * 查询用户通知
     */
    Page<NotificationResponseDTO> getUserNotifications(Integer userId, Pageable pageable);
    
    /**
     * 查询通知详情
     */
    NotificationResponseDTO getNotificationById(Long notificationId);
    
    /**
     * 处理待发送通知
     */
    void processPendingNotifications();
    
    /**
     * 处理失败重试通知
     */
    void processFailedNotifications();
    
    /**
     * 清理过期通知
     */
    void cleanupExpiredNotifications();
    
    /**
     * 根据业务类型和ID查询通知
     */
    List<NotificationResponseDTO> getNotificationsByBusiness(String businessType, String businessId);
} 