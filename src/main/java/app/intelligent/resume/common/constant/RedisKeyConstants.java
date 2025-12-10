package app.intelligent.resume.common.constant;

import app.intelligent.resume.common.enums.VerifyCodeType;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Redis Key常量类
 *
 * @author Intelligent Resume Team
 */
public class RedisKeyConstants {

    /**
     * 验证码Key前缀：verify_code:{type}:{phone}
     */
    public static final String VERIFY_CODE_PREFIX = "verify_code:";

    /**
     * 验证码发送次数Key前缀：verify_code_count:{type}:{phone}:{date}
     */
    public static final String VERIFY_CODE_COUNT_PREFIX = "verify_code_count:";

    /**
     * IP发送验证码次数Key前缀：verify_code_ip:{type}:{ip}:{date}
     */
    public static final String VERIFY_CODE_IP_PREFIX = "verify_code_ip:";

    /**
     * 请求幂等性Key前缀：request_idempotent:{requestId}
     */
    public static final String REQUEST_IDEMPOTENT_PREFIX = "request_idempotent:";

    /**
     * 注册分布式锁Key前缀：register_lock:{phone}
     */
    public static final String REGISTER_LOCK_PREFIX = "register_lock:";

    /**
     * 验证码错误重试次数Key前缀：verify_code_retry:{phone}
     */
    public static final String VERIFY_CODE_RETRY_PREFIX = "verify_code_retry:";

    /**
     * 构建验证码Key（带类型）
     */
    public static String buildVerifyCodeKey(String phone, VerifyCodeType type) {
        return VERIFY_CODE_PREFIX + type.getCode() + ":" + phone;
    }

    /**
     * 构建验证码Key（不带类型，向后兼容）
     */
    public static String buildVerifyCodeKey(String phone) {
        return VERIFY_CODE_PREFIX + phone;
    }

    /**
     * 构建验证码发送次数Key（每日限制）
     */
    public static String buildDailyCountKey(String phone, VerifyCodeType type) {
        String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        return VERIFY_CODE_COUNT_PREFIX + type.getCode() + ":" + phone + ":" + today;
    }

    /**
     * 构建IP发送验证码次数Key（每日限制）
     */
    public static String buildIpDailyCountKey(String ip, VerifyCodeType type) {
        String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        return VERIFY_CODE_IP_PREFIX + type.getCode() + ":" + ip + ":" + today;
    }

    /**
     * 构建验证码发送次数Key
     */
    public static String buildVerifyCodeCountKey(String phone, String date) {
        return VERIFY_CODE_COUNT_PREFIX + phone + ":" + date;
    }

    /**
     * 构建IP发送验证码次数Key
     */
    public static String buildVerifyCodeIpKey(String ip, String date) {
        return VERIFY_CODE_IP_PREFIX + ip + ":" + date;
    }

    /**
     * 构建请求幂等性Key
     */
    public static String buildRequestIdempotentKey(String requestId) {
        return REQUEST_IDEMPOTENT_PREFIX + requestId;
    }

    /**
     * 构建请求ID Key（简化版）
     */
    public static String buildRequestIdKey(String requestId) {
        return REQUEST_IDEMPOTENT_PREFIX + requestId;
    }

    /**
     * 构建注册分布式锁Key
     */
    public static String buildRegisterLockKey(String phone) {
        return REGISTER_LOCK_PREFIX + phone;
    }

    /**
     * 构建验证码错误重试次数Key
     */
    public static String buildVerifyCodeRetryKey(String phone) {
        return VERIFY_CODE_RETRY_PREFIX + phone;
    }
}
