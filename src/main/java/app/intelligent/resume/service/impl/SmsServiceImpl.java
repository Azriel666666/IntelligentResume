package app.intelligent.resume.service.impl;

import app.intelligent.resume.common.enums.VerifyCodeType;
import app.intelligent.resume.service.ISmsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 短信服务实现类（Mock版本）
 *
 * 生产环境需要替换为真实的短信服务提供商，如：
 * - 阿里云短信服务
 * - 腾讯云短信服务
 * - 华为云短信服务
 *
 * @author Intelligent Resume Team
 */
@Slf4j
@Service
public class SmsServiceImpl implements ISmsService {

    @Override
    public boolean sendSms(String phone, String code, VerifyCodeType type) {
        log.info("=== [Mock SMS] 发送验证码 ===");
        log.info("手机号: {}", phone);
        log.info("验证码: {}", code);
        log.info("类型: {}", type.getDescription());
        log.info("短信内容: 【智能简历】您的{}验证码为：{}，10分钟内有效，请勿泄露给他人。", type.getDescription(), code);
        log.info("========================");

        // TODO: 生产环境替换为真实短信发送逻辑
        // 示例集成代码：
        /*
        try {
            // 阿里云短信示例
            SendSmsRequest request = new SendSmsRequest();
            request.setPhoneNumbers(phone);
            request.setSignName("智能简历");
            request.setTemplateCode("SMS_123456789");
            request.setTemplateParam("{\"code\":\"" + code + "\"}");

            SendSmsResponse response = client.sendSms(request);
            if ("OK".equals(response.getCode())) {
                log.info("短信发送成功, phone={}, requestId={}", phone, response.getRequestId());
                return true;
            } else {
                log.error("短信发送失败, phone={}, code={}, message={}",
                    phone, response.getCode(), response.getMessage());
                return false;
            }
        } catch (Exception e) {
            log.error("短信发送异常, phone={}", phone, e);
            return false;
        }
        */

        // Mock: 模拟发送成功
        return true;
    }

    @Override
    public boolean sendNotification(String phone, String message) {
        log.info("=== [Mock SMS] 发送通知 ===");
        log.info("手机号: {}", phone);
        log.info("短信内容: 【智能简历】{}", message);
        log.info("======================");

        // TODO: 生产环境替换为真实短信发送逻辑

        // Mock: 模拟发送成功
        return true;
    }
}
