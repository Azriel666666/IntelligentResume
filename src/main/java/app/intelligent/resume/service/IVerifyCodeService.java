package app.intelligent.resume.service;

import app.intelligent.resume.common.enums.VerifyCodeType;

/**
 * 验证码服务接口
 *
 * @author Intelligent Resume Team
 */
public interface IVerifyCodeService {

    /**
     * 发送验证码
     *
     * @param phone     手机号
     * @param type      验证码类型
     * @param requestId 请求ID（用于幂等性）
     * @param ip        请求IP
     * @return 是否发送成功
     */
    boolean sendVerifyCode(String phone, VerifyCodeType type, String requestId, String ip);

    /**
     * 验证验证码
     *
     * @param phone 手机号
     * @param code  验证码
     * @param type  验证码类型
     * @return 是否验证成功
     */
    boolean verifyCode(String phone, String code, VerifyCodeType type);

    /**
     * 检查验证码是否已验证
     *
     * @param phone 手机号
     * @param type  验证码类型
     * @return 是否已验证
     */
    boolean isVerified(String phone, VerifyCodeType type);

    /**
     * 删除验证码（验证成功后或注册完成后）
     *
     * @param phone 手机号
     * @param type  验证码类型
     */
    void deleteVerifyCode(String phone, VerifyCodeType type);
}
