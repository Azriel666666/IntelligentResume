package app.intelligent.resume.config;

import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.teaopenapi.models.Config;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 阿里云短信服务配置
 *
 * @author Intelligent Resume Team
 */
@Slf4j
@Data
@Configuration
@ConfigurationProperties(prefix = "alibaba.sms")
public class AliyunSmsConfig {

    /**
     * 是否启用真实短信发送
     */
    private boolean enabled = false;

    /**
     * AccessKey ID
     */
    private String accessKeyId;

    /**
     * AccessKey Secret
     */
    private String accessKeySecret;

    /**
     * 短信签名
     */
    private String signName;

    /**
     * 验证码短信模板Code
     */
    private String templateCode;

    /**
     * 区域ID
     */
    private String regionId = "cn-hangzhou";

    /**
     * 创建阿里云短信客户端
     */
    @Bean
    public Client aliyunSmsClient() {
        if (!enabled) {
            log.info("阿里云短信服务未启用，使用Mock模式");
            return null;
        }

        try {
            Config config = new Config()
                    .setAccessKeyId(accessKeyId)
                    .setAccessKeySecret(accessKeySecret)
                    .setEndpoint("dysmsapi.aliyuncs.com");
            
            Client client = new Client(config);
            log.info("阿里云短信客户端初始化成功");
            return client;
        } catch (Exception e) {
            log.error("阿里云短信客户端初始化失败", e);
            return null;
        }
    }
}