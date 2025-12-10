package app.intelligent.resume.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 分析报告实体类
 *
 * @author Intelligent Resume Team
 */
@Data
@TableName("analysis_report")
public class AnalysisReport implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 报告ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 简历ID
     */
    private Long resumeId;

    /**
     * 完整度评分
     */
    private BigDecimal completenessScore;

    /**
     * 内容质量评分
     */
    private BigDecimal contentQualityScore;

    /**
     * 格式规范评分
     */
    private BigDecimal formatScore;

    /**
     * 总分
     */
    private BigDecimal totalScore;

    /**
     * 完整度分析（JSON）
     */
    private String completenessAnalysis;

    /**
     * 技能分析（JSON）
     */
    private String skillAnalysis;

    /**
     * 经验分析（JSON）
     */
    private String experienceAnalysis;

    /**
     * 优化建议列表（JSON）
     */
    private String suggestions;

    /**
     * 优势亮点（JSON）
     */
    private String strengthPoints;

    /**
     * 待改进点（JSON）
     */
    private String weakPoints;

    /**
     * AI生成的优化建议
     */
    private String aiSuggestions;

    /**
     * 职业发展建议
     */
    private String careerAdvice;

    /**
     * 提取的关键词
     */
    private String keywords;

    /**
     * 竞争力等级：低/中/高/优秀
     */
    private String competitiveness;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
