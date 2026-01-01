package app.intelligent.resume.service.impl;

import app.intelligent.resume.config.QwenConfig;
import app.intelligent.resume.service.IQwenService;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 阿里云通义千问API服务实现类
 * 使用OpenAI兼容接口
 *
 * @author Intelligent Resume Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QwenServiceImpl implements IQwenService {

    private final QwenConfig qwenConfig;
    private final ObjectMapper objectMapper;

    private static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");
    private static final int MAX_RETRIES = 3;

    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();

    @Override
    public String chat(String prompt) {
        if (!isAvailable()) {
            log.warn("通义千问服务未启用或配置不完整");
            return null;
        }

        for (int retry = 0; retry < MAX_RETRIES; retry++) {
            try {
                // 构建OpenAI兼容格式的请求体
                Map<String, Object> requestBody = Map.of(
                        "model", qwenConfig.getModel(),
                        "messages", List.of(
                                Map.of("role", "user", "content", prompt)
                        )
                );
                String requestJson = objectMapper.writeValueAsString(requestBody);

                Request request = new Request.Builder()
                        .url(qwenConfig.getChatUrl())
                        .addHeader("Authorization", "Bearer " + qwenConfig.getApiKey())
                        .addHeader("Content-Type", "application/json")
                        .post(RequestBody.create(requestJson, JSON_MEDIA_TYPE))
                        .build();

                try (Response response = httpClient.newCall(request).execute()) {
                    if (response.isSuccessful() && response.body() != null) {
                        String responseBody = response.body().string();
                        JsonNode jsonNode = objectMapper.readTree(responseBody);

                        // OpenAI兼容格式的响应解析
                        if (jsonNode.has("choices") && jsonNode.get("choices").isArray() 
                                && jsonNode.get("choices").size() > 0) {
                            JsonNode choice = jsonNode.get("choices").get(0);
                            if (choice.has("message") && choice.get("message").has("content")) {
                                return choice.get("message").get("content").asText();
                            }
                        }

                        // 错误处理
                        if (jsonNode.has("error")) {
                            JsonNode error = jsonNode.get("error");
                            String errorMsg = error.has("message") ? error.get("message").asText() : "未知错误";
                            String errorCode = error.has("code") ? error.get("code").asText() : "";
                            log.error("通义千问API调用失败，错误码：{}，错误信息：{}", errorCode, errorMsg);
                            
                            // 如果是认证错误，不重试
                            if ("InvalidApiKey".equals(errorCode) || "Unauthorized".equals(errorCode)) {
                                return null;
                            }
                        }
                    } else {
                        log.error("通义千问API请求失败，HTTP状态码：{}", response.code());
                    }
                }
            } catch (IOException e) {
                log.error("调用通义千问API异常，重试第{}次", retry + 1, e);
                if (retry < MAX_RETRIES - 1) {
                    try {
                        Thread.sleep(1000 * (retry + 1));
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }

        log.error("通义千问API调用失败，已重试{}次", MAX_RETRIES);
        return null;
    }

    @Override
    public String analyzeResumeQuality(String resumeContent) {
        String prompt = """
                你是一位专业的HR和简历分析专家。请分析以下简历的内容质量，并以JSON格式返回分析结果。
                
                分析维度：
                1. 内容专业性（0-100分）
                2. 表达清晰度（0-100分）
                3. 信息完整性（0-100分）
                4. 格式规范性（0-100分）
                
                请返回以下JSON格式（只返回JSON，不要其他内容）：
                {
                    "professionalScore": 85,
                    "clarityScore": 80,
                    "completenessScore": 90,
                    "formatScore": 75,
                    "overallComment": "整体评价..."
                }
                
                简历内容：
                """ + resumeContent;

        return chat(prompt);
    }

    @Override
    public String generateSuggestions(String resumeContent) {
        String prompt = """
                你是一位专业的HR和简历优化专家。请根据以下简历内容，提供具体的优化建议。
                
                请返回以下JSON格式（只返回JSON，不要其他内容）：
                {
                    "suggestions": [
                        "建议1：具体的优化建议...",
                        "建议2：具体的优化建议...",
                        "建议3：具体的优化建议..."
                    ],
                    "prioritySuggestion": "最重要的一条建议..."
                }
                
                简历内容：
                """ + resumeContent;

        return chat(prompt);
    }

    @Override
    public String generateCareerAdvice(String resumeContent) {
        String prompt = """
                你是一位资深的职业规划师。请根据以下简历内容，分析求职者的职业发展方向，并提供职业发展建议。
                
                请返回以下JSON格式（只返回JSON，不要其他内容）：
                {
                    "currentLevel": "当前职业水平评估",
                    "suitablePositions": ["适合的职位1", "适合的职位2"],
                    "developmentPath": "建议的职业发展路径",
                    "skillsToImprove": ["需要提升的技能1", "需要提升的技能2"],
                    "careerAdvice": "综合职业发展建议..."
                }
                
                简历内容：
                """ + resumeContent;

        return chat(prompt);
    }

    @Override
    public String extractKeywords(String resumeContent) {
        String prompt = """
                请从以下简历内容中提取关键词，包括技能、工具、技术栈、行业术语等。
                
                请直接返回关键词，用逗号分隔，不要其他内容。例如：Java,Spring Boot,MySQL,微服务
                
                简历内容：
                """ + resumeContent;

        return chat(prompt);
    }

    @Override
    public String analyzeStrengthsAndWeaknesses(String resumeContent) {
        String prompt = """
                你是一位专业的HR和简历分析专家。请分析以下简历的优势和不足。
                
                请返回以下JSON格式（只返回JSON，不要其他内容）：
                {
                    "strengths": [
                        "优势1：具体描述...",
                        "优势2：具体描述...",
                        "优势3：具体描述..."
                    ],
                    "weaknesses": [
                        "不足1：具体描述...",
                        "不足2：具体描述..."
                    ],
                    "competitiveness": "优秀/良好/一般/需改进"
                }
                
                简历内容：
                """ + resumeContent;

        return chat(prompt);
    }

    @Override
    public boolean isAvailable() {
        return qwenConfig.getEnabled()
                && StrUtil.isNotBlank(qwenConfig.getApiKey())
                && !"your-api-key".equals(qwenConfig.getApiKey())
                && !qwenConfig.getApiKey().startsWith("sk-xxx");
    }
}
