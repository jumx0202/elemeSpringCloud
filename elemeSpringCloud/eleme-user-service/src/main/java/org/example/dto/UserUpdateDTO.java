package org.example.dto;

import lombok.Data;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

/**
 * 用户信息更新DTO
 */
@Data
public class UserUpdateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户姓名
     */
    @Size(max = 20, message = "用户姓名长度不能超过20位")
    private String name;

    /**
     * 邮箱
     */
    @Email(message = "邮箱格式不正确")
    private String email;

    /**
     * 性别
     */
    @Pattern(regexp = "^(男|女)$", message = "性别只能为男或女")
    private String gender;

    /**
     * 头像URL
     */
    private String avatar;
} 