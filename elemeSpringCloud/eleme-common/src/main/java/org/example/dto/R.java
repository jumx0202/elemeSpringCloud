package org.example.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;

/**
 * 统一响应结果类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class R<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 响应代码
     */
    private Integer code;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 时间戳
     */
    private Long timestamp;

    public R(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }

    public R(Integer code, String message) {
        this(code, message, null);
    }

    /**
     * 成功响应
     */
    public static <T> R<T> success() {
        return new R<>(200, "操作成功");
    }

    /**
     * 成功响应（带数据）
     */
    public static <T> R<T> success(T data) {
        return new R<>(200, "操作成功", data);
    }

    /**
     * 成功响应（带消息和数据）
     */
    public static <T> R<T> success(String message, T data) {
        return new R<>(200, message, data);
    }

    /**
     * 失败响应
     */
    public static <T> R<T> error() {
        return new R<>(500, "操作失败");
    }

    /**
     * 失败响应（带消息）
     */
    public static <T> R<T> error(String message) {
        return new R<>(500, message);
    }

    /**
     * 失败响应（带代码和消息）
     */
    public static <T> R<T> error(Integer code, String message) {
        return new R<>(code, message);
    }

    /**
     * 失败响应（带代码、消息和数据）
     */
    public static <T> R<T> error(Integer code, String message, T data) {
        return new R<>(code, message, data);
    }

    /**
     * 判断是否成功
     */
    @JsonIgnore
    public boolean isSuccess() {
        return this.code != null && this.code == 200;
    }

    /**
     * 判断是否失败
     */
    @JsonIgnore
    public boolean isError() {
        return !isSuccess();
    }
} 