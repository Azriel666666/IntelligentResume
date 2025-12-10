package app.intelligent.resume.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 验证码类型枚举
 *
 * @author Intelligent Resume Team
 */
@Getter
@AllArgsConstructor
public enum VerifyCodeType {

    /**
     * 注册
     */
    REGISTER("REGISTER", "注册验证码"),

    /**
     * 登录
     */
    LOGIN("LOGIN", "登录验证码"),

    /**
     * 重置密码
     */
    RESET_PASSWORD("RESET_PASSWORD", "重置密码验证码"),

    /**
     * 修改手机号
     */
    CHANGE_PHONE("CHANGE_PHONE", "修改手机号验证码"),

    /**
     * 绑定手机号
     */
    BIND_PHONE("BIND_PHONE", "绑定手机号验证码");

    private final String code;
    private final String description;

    /**
     * 根据code获取枚举
     */
    public static VerifyCodeType getByCode(String code) {
        for (VerifyCodeType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}
