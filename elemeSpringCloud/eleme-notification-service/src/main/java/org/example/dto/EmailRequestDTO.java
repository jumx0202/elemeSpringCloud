package org.example.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 邮件发送请求DTO
 */
@Data
public class EmailRequestDTO {
    
    /**
     * 收件人邮箱
     */
    @NotBlank(message = "收件人邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String to;
    
    /**
     * 抄送邮箱列表
     */
    private List<String> cc;
    
    /**
     * 密送邮箱列表
     */
    private List<String> bcc;
    
    /**
     * 邮件主题
     */
    @NotBlank(message = "邮件主题不能为空")
    private String subject;
    
    /**
     * 邮件内容
     */
    private String content;
    
    /**
     * 是否HTML格式
     */
    private Boolean isHtml = false;
    
    /**
     * 模板代码
     */
    private String templateCode;
    
    /**
     * 模板参数
     */
    private Map<String, Object> templateParams;
    
    /**
     * 附件列表
     */
    private List<EmailAttachmentDTO> attachments;
    
    /**
     * 业务类型
     */
    private String businessType;
    
    /**
     * 业务ID
     */
    private String businessId;
    
    /**
     * 用户ID
     */
    private Integer userId;
} 