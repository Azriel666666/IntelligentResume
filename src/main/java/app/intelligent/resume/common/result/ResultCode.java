package app.intelligent.resume.common.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 统一返回状态码枚举
 *
 * @author Intelligent Resume Team
 */
@Getter
@AllArgsConstructor
public enum ResultCode {

    /**
     * 成功
     */
    SUCCESS(200, "操作成功"),

    /**
     * 失败
     */
    FAILED(500, "操作失败"),

    /**
     * 参数错误
     */
    VALIDATE_FAILED(400, "参数校验失败"),

    /**
     * 未认证
     */
    UNAUTHORIZED(401, "未认证，请先登录"),

    /**
     * 无权限
     */
    FORBIDDEN(403, "无权限访问"),

    /**
     * 资源不存在
     */
    NOT_FOUND(404, "资源不存在"),

    /**
     * 用户名或密码错误
     */
    LOGIN_FAILED(1001, "用户名或密码错误"),

    /**
     * 手机号未注册
     */
    PHONE_NOT_REGISTERED(1005, "该手机号未注册"),

    /**
     * 账号已被禁用
     */
    ACCOUNT_DISABLED(1006, "该账号已被禁用"),

    /**
     * 密码错误
     */
    PASSWORD_ERROR(1007, "密码错误"),

    /**
     * 用户已存在
     */
    USER_EXIST(1002, "用户已存在"),

    /**
     * 用户不存在
     */
    USER_NOT_EXIST(1003, "用户不存在"),

    /**
     * Token无效
     */
    TOKEN_INVALID(1004, "Token无效或已过期"),

    /**
     * 简历不存在
     */
    RESUME_NOT_EXIST(2001, "简历不存在"),

    /**
     * 无权限操作该简历
     */
    RESUME_NO_PERMISSION(2002, "无权限操作该简历"),

    /**
     * 简历解析中
     */
    RESUME_PARSING(2003, "简历正在解析中，请稍后"),

    /**
     * 简历解析失败
     */
    RESUME_PARSE_FAILED(2004, "简历解析失败"),

    /**
     * 简历文件不存在
     */
    RESUME_FILE_NOT_EXIST(2005, "简历文件不存在"),

    /**
     * 简历数量超限
     */
    RESUME_COUNT_EXCEEDED(2006, "简历数量已达上限"),

    /**
     * 岗位不存在
     */
    JOB_NOT_EXIST(3001, "岗位不存在"),

    /**
     * 文件上传失败
     */
    FILE_UPLOAD_FAILED(4001, "文件上传失败"),

    /**
     * 文件类型不支持
     */
    FILE_TYPE_NOT_SUPPORT(4002, "文件类型不支持"),

    /**
     * 文件为空
     */
    FILE_EMPTY(4003, "上传文件不能为空"),

    /**
     * 文件URL生成失败
     */
    FILE_URL_GENERATE_FAILED(4004, "文件访问地址生成失败"),

    /**
     * 文件大小超限
     */
    FILE_SIZE_EXCEEDED(4005, "文件大小超过限制"),

    // ========== 验证码相关错误码 (5000-5099) ==========

    /**
     * 手机号格式错误
     */
    PHONE_FORMAT_ERROR(5001, "手机号格式不正确"),

    /**
     * 密码格式错误
     */
    PASSWORD_FORMAT_ERROR(5002, "密码格式不正确，需8-16位且包含数字和字母"),

    /**
     * 验证码为空
     */
    VERIFY_CODE_EMPTY(5003, "验证码不能为空"),

    /**
     * 请求ID为空
     */
    REQUEST_ID_EMPTY(5004, "请求ID不能为空"),

    /**
     * 手机号已注册
     */
    PHONE_ALREADY_REGISTERED(5005, "该手机号已被注册"),

    /**
     * 发送验证码过于频繁
     */
    SEND_CODE_TOO_FREQUENT(5006, "发送验证码过于频繁，请60秒后再试"),

    /**
     * 每日发送次数超限
     */
    DAILY_SEND_LIMIT_EXCEEDED(5007, "今日发送验证码次数已达上限（5次）"),

    /**
     * IP发送次数超限
     */
    IP_SEND_LIMIT_EXCEEDED(5008, "该IP今日发送验证码次数已达上限"),

    /**
     * 验证码不存在或已过期
     */
    VERIFY_CODE_NOT_EXIST(5009, "验证码不存在或已过期，请重新获取"),

    /**
     * 验证码错误
     */
    VERIFY_CODE_ERROR(5010, "验证码错误"),

    /**
     * 验证码错误次数超限
     */
    VERIFY_CODE_RETRY_EXCEEDED(5011, "验证码错误次数过多，请重新获取"),

    /**
     * 验证码未验证
     */
    VERIFY_CODE_NOT_VERIFIED(5012, "请先验证验证码"),

    /**
     * 短信发送失败
     */
    SMS_SEND_FAILED(5013, "短信发送失败，请稍后重试"),

    /**
     * 重复请求
     */
    REQUEST_DUPLICATE(5014, "请勿重复提交"),

    /**
     * 获取注册锁失败
     */
    REGISTER_LOCK_FAILED(5015, "注册处理中，请稍候再试"),

    /**
     * 密码不一致
     */
    PASSWORD_NOT_MATCH(5016, "两次输入的密码不一致"),

    /**
     * 密码包含非法字符
     */
    PASSWORD_ILLEGAL_CHAR(5017, "密码包含非法字符"),

    /**
     * 密码包含连续字符
     */
    PASSWORD_CONTINUOUS_CHAR(5018, "密码不能包含连续字符"),

    /**
     * 密码包含重复字符
     */
    PASSWORD_REPEATED_CHAR(5019, "密码不能包含过多重复字符"),

    // ========== 用户模块相关错误码 (1010-1099) ==========

    /**
     * 原密码错误
     */
    OLD_PASSWORD_ERROR(1010, "原密码错误"),

    /**
     * 新密码不能与原密码相同
     */
    NEW_PASSWORD_SAME_AS_OLD(1011, "新密码不能与原密码相同"),

    /**
     * 手机号已被其他用户使用
     */
    PHONE_ALREADY_USED(1012, "该手机号已被其他用户使用"),

    /**
     * 用户未登录
     */
    USER_NOT_LOGIN(1013, "用户未登录"),

    /**
     * 用户状态无效
     */
    USER_STATUS_INVALID(1014, "用户状态无效");

    private final Integer code;
    private final String message;
}
