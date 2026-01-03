package app.intelligent.resume.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 求职者统计响应DTO
 *
 * @author Intelligent Resume Team
 */
@Data
@Builder
@Schema(description = "求职者统计响应")
public class SeekerStatisticsResponse {

    @Schema(description = "简历数量")
    private Integer resumeCount;

    @Schema(description = "投递次数")
    private Integer applicationCount;

    @Schema(description = "被查看次数")
    private Integer viewedCount;

    @Schema(description = "面试邀请数")
    private Integer interviewCount;

    @Schema(description = "简历平均分（默认简历分数）")
    private BigDecimal averageScore;

    @Schema(description = "通过筛选数")
    private Integer passedCount;

    @Schema(description = "待处理投递数")
    private Integer pendingCount;

    @Schema(description = "收到Offer数")
    private Integer offerCount;

    @Schema(description = "默认简历ID")
    private Long defaultResumeId;

    @Schema(description = "默认简历是否有分析报告")
    private Boolean hasAnalysisReport;
}
