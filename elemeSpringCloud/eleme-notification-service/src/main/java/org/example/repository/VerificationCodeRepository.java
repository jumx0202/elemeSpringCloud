package org.example.repository;

import org.example.entity.VerificationCode;
import org.example.entity.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 验证码Repository
 */
@Repository
public interface VerificationCodeRepository extends JpaRepository<VerificationCode, Long> {
    
    /**
     * 根据接收者、类型和发送方式查询最新的验证码
     */
    Optional<VerificationCode> findTopByReceiverAndCodeTypeAndSendTypeOrderByCreateTimeDesc(
            String receiver, String codeType, NotificationType sendType);
    
    /**
     * 根据接收者和类型查询所有验证码
     */
    List<VerificationCode> findByReceiverAndCodeType(String receiver, String codeType);
    
    /**
     * 根据接收者、类型和验证码查询
     */
    Optional<VerificationCode> findByReceiverAndCodeTypeAndCode(String receiver, String codeType, String code);
    
    /**
     * 查询某个接收者在指定时间范围内的验证码数量
     */
    @Query("SELECT COUNT(v) FROM VerificationCode v WHERE v.receiver = :receiver AND v.createTime BETWEEN :startTime AND :endTime")
    Long countByReceiverAndTimeRange(@Param("receiver") String receiver, 
                                   @Param("startTime") LocalDateTime startTime, 
                                   @Param("endTime") LocalDateTime endTime);
    
    /**
     * 查询某个IP在指定时间范围内的验证码数量
     */
    @Query("SELECT COUNT(v) FROM VerificationCode v WHERE v.clientIp = :clientIp AND v.createTime BETWEEN :startTime AND :endTime")
    Long countByClientIpAndTimeRange(@Param("clientIp") String clientIp, 
                                   @Param("startTime") LocalDateTime startTime, 
                                   @Param("endTime") LocalDateTime endTime);
    
    /**
     * 查询过期的验证码
     */
    List<VerificationCode> findByExpireTimeBefore(LocalDateTime expireTime);
    
    /**
     * 删除过期的验证码
     */
    void deleteByExpireTimeBefore(LocalDateTime expireTime);
    
    /**
     * 查询需要清理的验证码（创建时间超过指定天数）
     */
    @Query("SELECT v FROM VerificationCode v WHERE v.createTime < :cleanupTime")
    List<VerificationCode> findExpiredCodes(@Param("cleanupTime") LocalDateTime cleanupTime);
} 