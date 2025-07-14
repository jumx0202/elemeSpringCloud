package org.example.constants;

/**
 * 通用常量类
 */
public class CommonConstants {

    /**
     * 成功状态码
     */
    public static final int SUCCESS_CODE = 200;

    /**
     * 失败状态码
     */
    public static final int ERROR_CODE = 500;

    /**
     * 未授权状态码
     */
    public static final int UNAUTHORIZED_CODE = 401;

    /**
     * 禁止访问状态码
     */
    public static final int FORBIDDEN_CODE = 403;

    /**
     * 资源不存在状态码
     */
    public static final int NOT_FOUND_CODE = 404;

    /**
     * 用户状态 - 正常
     */
    public static final int USER_STATUS_NORMAL = 1;

    /**
     * 用户状态 - 禁用
     */
    public static final int USER_STATUS_DISABLED = 0;

    /**
     * 订单状态 - 未支付
     */
    public static final int ORDER_STATUS_UNPAID = 0;

    /**
     * 订单状态 - 已支付
     */
    public static final int ORDER_STATUS_PAID = 1;

    /**
     * 订单状态 - 已完成
     */
    public static final int ORDER_STATUS_COMPLETED = 2;

    /**
     * 订单状态 - 已取消
     */
    public static final int ORDER_STATUS_CANCELLED = 3;

    /**
     * 食物状态 - 上架
     */
    public static final int FOOD_STATUS_ON_SALE = 1;

    /**
     * 食物状态 - 下架
     */
    public static final int FOOD_STATUS_OFF_SALE = 0;

    /**
     * 商家状态 - 正常营业
     */
    public static final int BUSINESS_STATUS_NORMAL = 1;

    /**
     * 商家状态 - 暂停营业
     */
    public static final int BUSINESS_STATUS_DISABLED = 0;

    /**
     * JWT Token 前缀
     */
    public static final String TOKEN_PREFIX = "Bearer ";

    /**
     * JWT Token Header名称
     */
    public static final String TOKEN_HEADER = "Authorization";

    /**
     * Redis Key 前缀
     */
    public static final String REDIS_PREFIX = "eleme:";

    /**
     * 用户Token Redis Key前缀
     */
    public static final String USER_TOKEN_PREFIX = REDIS_PREFIX + "user:token:";

    /**
     * 验证码 Redis Key前缀
     */
    public static final String CAPTCHA_PREFIX = REDIS_PREFIX + "captcha:";

    /**
     * 邮箱验证码 Redis Key前缀
     */
    public static final String EMAIL_CODE_PREFIX = REDIS_PREFIX + "email:code:";

    /**
     * 商家缓存 Redis Key前缀
     */
    public static final String BUSINESS_CACHE_PREFIX = REDIS_PREFIX + "business:";

    /**
     * 食物缓存 Redis Key前缀
     */
    public static final String FOOD_CACHE_PREFIX = REDIS_PREFIX + "food:";

    /**
     * 默认缓存过期时间（秒）
     */
    public static final long DEFAULT_CACHE_EXPIRE = 3600L;

    /**
     * Token过期时间（秒）
     */
    public static final long TOKEN_EXPIRE = 7200L;

    /**
     * 验证码过期时间（秒）
     */
    public static final long CAPTCHA_EXPIRE = 300L;

    /**
     * 邮箱验证码过期时间（秒）
     */
    public static final long EMAIL_CODE_EXPIRE = 300L;
} 