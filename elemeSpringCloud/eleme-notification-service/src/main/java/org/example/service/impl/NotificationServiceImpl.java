package org.example.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.NotificationResponseDTO;
import org.example.entity.Notification;
import org.example.entity.NotificationType;
import org.example.entity.NotificationStatus;
import org.example.repository.NotificationRepository;
import org.example.service.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 通知服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class NotificationServiceImpl implements NotificationService {
    
    private final NotificationRepository notificationRepository;
    
    @Override
    public Notification createNotification(NotificationType type, String receiver, String title, 
                                         String content, String businessType, String businessId, 
                                         Integer userId) {
        Notification notification = new Notification();
        notification.setType(type);
        notification.setReceiver(receiver);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setBusinessType(businessType);
        notification.setBusinessId(businessId);
        notification.setUserId(userId);
        notification.setStatus(NotificationStatus.PENDING);
        notification.setCreateTime(LocalDateTime.now());
        
        return notificationRepository.save(notification);
    }
    
    @Override
    public void updateNotificationStatus(Long notificationId, String status, String failureReason) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            notification.setStatus(NotificationStatus.valueOf(status));
            notification.setFailureReason(failureReason);
            notification.setUpdateTime(LocalDateTime.now());
            notificationRepository.save(notification);
        });
    }
    
    @Override
    public Page<NotificationResponseDTO> getUserNotifications(Integer userId, Pageable pageable) {
        Page<Notification> notifications = notificationRepository.findByUserId(userId, pageable);
        return notifications.map(this::convertToResponseDTO);
    }
    
    @Override
    public NotificationResponseDTO getNotificationById(Long notificationId) {
        return notificationRepository.findById(notificationId)
                .map(this::convertToResponseDTO)
                .orElse(null);
    }
    
    @Override
    public void processPendingNotifications() {
        List<Notification> pendingNotifications = notificationRepository.findPendingNotifications(NotificationStatus.PENDING);
        log.info("处理待发送通知，数量：{}", pendingNotifications.size());
        
        for (Notification notification : pendingNotifications) {
            try {
                // 这里可以添加具体的处理逻辑
                log.info("处理通知：{}", notification.getId());
            } catch (Exception e) {
                log.error("处理通知失败：{}", notification.getId(), e);
            }
        }
    }
    
    @Override
    public void processFailedNotifications() {
        List<Notification> failedNotifications = notificationRepository.findPendingNotifications(NotificationStatus.FAILED);
        log.info("处理失败重试通知，数量：{}", failedNotifications.size());
        
        for (Notification notification : failedNotifications) {
            if (notification.getRetryCount() < notification.getMaxRetryCount()) {
                try {
                    // 重试逻辑
                    notification.setRetryCount(notification.getRetryCount() + 1);
                    notificationRepository.save(notification);
                    log.info("重试通知：{}", notification.getId());
                } catch (Exception e) {
                    log.error("重试通知失败：{}", notification.getId(), e);
                }
            }
        }
    }
    
    @Override
    public void cleanupExpiredNotifications() {
        List<Notification> expiredNotifications = notificationRepository.findExpiredNotifications(
                NotificationStatus.PENDING, LocalDateTime.now());
        log.info("清理过期通知，数量：{}", expiredNotifications.size());
        
        for (Notification notification : expiredNotifications) {
            notification.setStatus(NotificationStatus.FAILED);
            notification.setFailureReason("已过期");
            notificationRepository.save(notification);
        }
    }
    
    @Override
    public List<NotificationResponseDTO> getNotificationsByBusiness(String businessType, String businessId) {
        List<Notification> notifications = notificationRepository.findByBusinessTypeAndBusinessId(businessType, businessId);
        return notifications.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }
    
    private NotificationResponseDTO convertToResponseDTO(Notification notification) {
        NotificationResponseDTO dto = new NotificationResponseDTO();
        dto.setId(notification.getId());
        dto.setType(notification.getType().name());
        dto.setReceiver(notification.getReceiver());
        dto.setTitle(notification.getTitle());
        dto.setContent(notification.getContent());
        dto.setStatus(notification.getStatus().name());
        dto.setBusinessType(notification.getBusinessType());
        dto.setBusinessId(notification.getBusinessId());
        dto.setCreateTime(notification.getCreateTime());
        dto.setSendTime(notification.getSendTime());
        return dto;
    }
}
