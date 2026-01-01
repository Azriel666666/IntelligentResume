package app.intelligent.resume.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 候选人推荐响应DTO
 *
 * @author Intelligent Resume Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "候选人推荐响应")
public class CandidateRecommendResponse {

    @Schema(description = "简历ID")
    private Long resumeId;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "候选人姓名")
    private String name;

    @Schema(description = "手机号（脱敏）")
    private String phone;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "最高学历")
    private String highestEducation;

    @Schema(description = "工作年限")
    private Integer workYears;

    @Schema(description = "当前城市")
    private String currentCity;

    @Schema(description = "期望职位")
    private String expectedPosition;

    @Schema(description = "期望薪资")
    private String expectedSalary;

    @Schema(description = "技能标签")
    private String skillTags;

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

    @Schema(description = "AI推荐理由")
    private String recommendReason;

    @Schema(description = "推荐等级：A-强烈推荐 B-推荐 C-一般")
    private String recommendLevel;
}
