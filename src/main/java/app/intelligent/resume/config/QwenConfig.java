package app.intelligent.resume.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 阿里云通义千问API配置类
 *
 * @author Intelligent Resume Team
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "alibaba.qwen")
public class QwenConfig {

    /**
     * API Key（在阿里云百炼平台获取）
     * 获取地址：https://bailian.console.aliyun.com/
     */
    private String apiKey;

    /**
     * 模型名称
     * 可选值：qwen-turbo、qwen-plus、qwen-max、qwen-max-longcontext等
     */
    private String model = "qwen-plus";

    /**
     * 请求超时时间（秒）
     */
    private Integer timeout = 60;

    /**
     * 是否启用
     */
    private Boolean enabled = false;

    /**
     * API基础URL
     */
    private String baseUrl = "https://dashscope.aliyuncs.com/compatible-mode/v1";

    /**
     * 获取聊天API的URL
     */
    public String getChatUrl() {
        return baseUrl + "/chat/completions";
    }
}
