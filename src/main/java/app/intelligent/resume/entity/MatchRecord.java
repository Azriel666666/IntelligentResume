package app.intelligent.resume.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 匹配记录实体类
 *
 * @author Intelligent Resume Team
 */
@Data
@TableName("match_record")
public class MatchRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 匹配ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 简历ID
     */
    private Long resumeId;

    /**
     * 岗位ID
     */
    private Long jobId;

    /**
     * 综合匹配度（0-100）
     */
    private BigDecimal matchScore;

    /**
     * 技能匹配度
     */
    private BigDecimal skillMatchScore;

    /**
     * 经验匹配度
     */
    private BigDecimal experienceMatchScore;

    /**
     * 学历匹配度
     */
    private BigDecimal educationMatchScore;

    /**
     * 匹配的技能（JSON数组）
     */
    private String matchedSkills;

    /**
     * 缺失的技能（JSON数组）
     */
    private String missingSkills;

    /**
     * 详细匹配信息（JSON）
     */
    private String matchDetail;

    /**
     * AI分析建议
     */
    private String aiAnalysis;

    /**
     * 匹配类型：1-系统推荐 2-用户主动
     */
    private Integer matchType;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
