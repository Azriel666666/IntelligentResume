package app.intelligent.resume.service;

import app.intelligent.resume.common.enums.VerifyCodeType;

/**
 * 短信服务接口
 *
 * @author Intelligent Resume Team
 */
public interface ISmsService {

    /**
     * 发送短信验证码
     *
     * @param phone 手机号
     * @param code  验证码
     * @param type  验证码类型
     * @return 是否发送成功
     */
    boolean sendSms(String phone, String code, VerifyCodeType type);

    /**
     * 发送通知短信
     *
     * @param phone   手机号
     * @param message 短信内容
     * @return 是否发送成功
     */
    boolean sendNotification(String phone, String message);
}
