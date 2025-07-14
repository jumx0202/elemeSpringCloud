package org.example.repository;

import org.example.entity.Notification;
import org.example.entity.NotificationStatus;
import org.example.entity.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 通知记录Repository
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    /**
     * 根据接收者查询通知
     */
    List<Notification> findByReceiver(String receiver);
    
    /**
     * 根据接收者和类型查询通知
     */
    List<Notification> findByReceiverAndType(String receiver, NotificationType type);
    
    /**
     * 根据状态查询通知
     */
    List<Notification> findByStatus(NotificationStatus status);
    
    /**
     * 根据业务类型和业务ID查询通知
     */
    List<Notification> findByBusinessTypeAndBusinessId(String businessType, String businessId);
    
    /**
     * 根据用户ID查询通知
     */
    Page<Notification> findByUserId(Integer userId, Pageable pageable);
    
    /**
     * 查询待发送的通知
     */
    @Query("SELECT n FROM Notification n WHERE n.status = :status AND n.retryCount < n.maxRetryCount")
    List<Notification> findPendingNotifications(@Param("status") NotificationStatus status);
    
    /**
     * 查询过期的通知
     */
    @Query("SELECT n FROM Notification n WHERE n.status = :status AND n.expireTime < :now")
    List<Notification> findExpiredNotifications(@Param("status") NotificationStatus status, 
                                               @Param("now") LocalDateTime now);
    
    /**
     * 根据接收者和时间范围查询通知数量
     */
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.receiver = :receiver AND n.createTime BETWEEN :startTime AND :endTime")
    Long countByReceiverAndTimeRange(@Param("receiver") String receiver, 
                                   @Param("startTime") LocalDateTime startTime, 
                                   @Param("endTime") LocalDateTime endTime);
    
    /**
     * 根据IP和时间范围查询通知数量
     */
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.receiver LIKE :ipPattern AND n.createTime BETWEEN :startTime AND :endTime")
    Long countByIpAndTimeRange(@Param("ipPattern") String ipPattern, 
                             @Param("startTime") LocalDateTime startTime, 
                             @Param("endTime") LocalDateTime endTime);
    
    /**
     * 删除过期的通知
     */
    void deleteByExpireTimeBefore(LocalDateTime expireTime);
} 