package app.intelligent.resume.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 匹配结果响应DTO
 *
 * @author Intelligent Resume Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "匹配结果响应")
public class MatchResponse {

    @Schema(description = "匹配记录ID")
    private Long id;

    @Schema(description = "简历ID")
    private Long resumeId;

    @Schema(description = "岗位ID")
    private Long jobId;

    @Schema(description = "综合匹配度（0-100）")
    private BigDecimal matchScore;

    @Schema(description = "技能匹配度")
    private BigDecimal skillMatchScore;

    @Schema(description = "经验匹配度")
    private BigDecimal experienceMatchScore;

    @Schema(description = "学历匹配度")
    private BigDecimal educationMatchScore;

    @Schema(description = "匹配的技能")
    private List<String> matchedSkills;

    @Schema(description = "缺失的技能")
    private List<String> missingSkills;

    @Schema(description = "匹配详情")
    private MatchDetail matchDetail;

    @Schema(description = "AI分析建议")
    private String aiAnalysis;

    @Schema(description = "匹配类型：1-系统推荐 2-用户主动")
    private Integer matchType;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    // ========== 关联信息 ==========

    @Schema(description = "简历标题")
    private String resumeTitle;

    @Schema(description = "候选人姓名")
    private String candidateName;

    @Schema(description = "岗位名称")
    private String jobTitle;

    @Schema(description = "公司名称")
    private String companyName;

    /**
     * 匹配详情内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MatchDetail {
        @Schema(description = "技能匹配详情")
        private String skillDetail;

        @Schema(description = "经验匹配详情")
        private String experienceDetail;

        @Schema(description = "学历匹配详情")
        private String educationDetail;

        @Schema(description = "其他匹配详情")
        private String otherDetail;
    }
}
