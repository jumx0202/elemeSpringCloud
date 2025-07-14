package org.example.dto;

import lombok.Data;

/**
 * 邮件附件DTO
 */
@Data
public class EmailAttachmentDTO {
    
    /**
     * 附件名称
     */
    private String name;
    
    /**
     * 附件类型
     */
    private String contentType;
    
    /**
     * 附件内容（Base64编码）
     */
    private String content;
    
    /**
     * 附件大小（字节）
     */
    private Long size;
} 