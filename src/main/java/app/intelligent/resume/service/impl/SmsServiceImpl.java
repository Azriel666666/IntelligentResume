package app.intelligent.resume.service.impl;

import app.intelligent.resume.common.enums.VerifyCodeType;
import app.intelligent.resume.config.AliyunSmsConfig;
import app.intelligent.resume.service.ISmsService;
import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.dysmsapi20170525.models.SendSmsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 短信服务实现类
 * 
 * 支持两种模式：
 * 1. Mock模式（alibaba.sms.enabled=false）：验证码打印到日志，用于开发测试
 * 2. 真实模式（alibaba.sms.enabled=true）：通过阿里云短信服务发送真实短信
 *
 * @author Intelligent Resume Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SmsServiceImpl implements ISmsService {

    private final AliyunSmsConfig smsConfig;
    private final Client aliyunSmsClient;

    @Override
    public boolean sendSms(String phone, String code, VerifyCodeType type) {
        // 检查是否启用真实短信发送
        if (!smsConfig.isEnabled() || aliyunSmsClient == null) {
            return sendMockSms(phone, code, type);
        }

        return sendAliyunSms(phone, code, type);
    }

    /**
     * Mock模式发送短信（仅打印日志）
     */
    private boolean sendMockSms(String phone, String code, VerifyCodeType type) {
        log.info("========================================");
        log.info("=== [Mock SMS] 发送验证码 ===");
        log.info("手机号: {}", phone);
        log.info("验证码: {}", code);
        log.info("类型: {}", type.getDescription());
        log.info("短信内容: 【智能简历】您的{}验证码为：{}，10分钟内有效，请勿泄露给他人。", type.getDescription(), code);
        log.info("========================================");
        log.info("提示: 如需发送真实短信，请在配置文件中设置 alibaba.sms.enabled=true 并配置阿里云短信参数");
        
        // Mock模式始终返回成功
        return true;
    }

    /**
     * 通过阿里云短信服务发送真实短信
     */
    private boolean sendAliyunSms(String phone, String code, VerifyCodeType type) {
        try {
            log.info("开始发送阿里云短信, phone={}, type={}", phone, type.getDescription());

            // 构建短信请求
            SendSmsRequest request = new SendSmsRequest()
                    .setPhoneNumbers(phone)
                    .setSignName(smsConfig.getSignName())
                    .setTemplateCode(smsConfig.getTemplateCode())
                    .setTemplateParam("{\"code\":\"" + code + "\"}");

            // 发送短信
            SendSmsResponse response = aliyunSmsClient.sendSms(request);

            // 检查发送结果
            if ("OK".equals(response.getBody().getCode())) {
                log.info("阿里云短信发送成功, phone={}, requestId={}, bizId={}", 
                        phone, response.getBody().getRequestId(), response.getBody().getBizId());
                return true;
            } else {
                log.error("阿里云短信发送失败, phone={}, code={}, message={}", 
                        phone, response.getBody().getCode(), response.getBody().getMessage());
                return false;
            }

        } catch (Exception e) {
            log.error("阿里云短信发送异常, phone={}", phone, e);
            return false;
        }
    }

    @Override
    public boolean sendNotification(String phone, String message) {
        // 检查是否启用真实短信发送
        if (!smsConfig.isEnabled() || aliyunSmsClient == null) {
            log.info("========================================");
            log.info("=== [Mock SMS] 发送通知 ===");
            log.info("手机号: {}", phone);
            log.info("短信内容: 【智能简历】{}", message);
            log.info("========================================");
            return true;
        }

        // 通知类短信需要单独的模板，这里暂时使用Mock
        log.warn("通知类短信暂未配置模板，使用Mock模式");
        log.info("========================================");
        log.info("=== [Mock SMS] 发送通知 ===");
        log.info("手机号: {}", phone);
        log.info("短信内容: 【智能简历】{}", message);
        log.info("========================================");
        return true;
    }
}
