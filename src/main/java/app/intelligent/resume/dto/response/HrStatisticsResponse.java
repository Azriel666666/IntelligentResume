package app.intelligent.resume.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * HR统计响应DTO
 *
 * @author Intelligent Resume Team
 */
@Data
@Builder
@Schema(description = "HR统计响应")
public class HrStatisticsResponse {

    @Schema(description = "发布岗位数")
    private Integer jobCount;

    @Schema(description = "招聘中岗位数")
    private Integer activeJobCount;

    @Schema(description = "收到投递数")
    private Integer applicationCount;

    @Schema(description = "岗位总浏览量")
    private Long totalViewCount;

    @Schema(description = "今日新增投递")
    private Integer todayApplicationCount;

    @Schema(description = "待处理投递数")
    private Integer pendingApplicationCount;

    @Schema(description = "已发面试邀请数")
    private Integer interviewSentCount;

    @Schema(description = "已发Offer数")
    private Integer offerSentCount;
}
