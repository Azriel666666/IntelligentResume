package app.intelligent.resume.service;

/**
 * 阿里云通义千问API服务接口
 *
 * @author Intelligent Resume Team
 */
public interface IQwenService {

    /**
     * 发送对话请求
     * 
     * @param prompt 提示词
     * @return AI回复内容
     */
    String chat(String prompt);

    /**
     * 分析简历内容质量
     * 
     * @param resumeContent 简历内容
     * @return 分析结果（JSON格式）
     */
    String analyzeResumeQuality(String resumeContent);

    /**
     * 生成简历优化建议
     * 
     * @param resumeContent 简历内容
     * @return 优化建议（JSON格式）
     */
    String generateSuggestions(String resumeContent);

    /**
     * 生成职业发展建议
     * 
     * @param resumeContent 简历内容
     * @return 职业发展建议
     */
    String generateCareerAdvice(String resumeContent);

    /**
     * 提取简历关键词
     * 
     * @param resumeContent 简历内容
     * @return 关键词列表（逗号分隔）
     */
    String extractKeywords(String resumeContent);

    /**
     * 分析简历优势和劣势
     * 
     * @param resumeContent 简历内容
     * @return 优劣势分析（JSON格式）
     */
    String analyzeStrengthsAndWeaknesses(String resumeContent);

    /**
     * 检查AI服务是否可用
     * 
     * @return 是否可用
     */
    boolean isAvailable();
}
