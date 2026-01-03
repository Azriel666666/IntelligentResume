package app.intelligent.resume.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 管理员统计响应DTO
 *
 * @author Intelligent Resume Team
 */
@Data
@Builder
@Schema(description = "管理员统计响应")
public class AdminStatisticsResponse {

    // ========== 用户统计 ==========
    @Schema(description = "用户总数")
    private Long totalUserCount;

    @Schema(description = "管理员数量")
    private Long adminCount;

    @Schema(description = "HR数量")
    private Long hrCount;

    @Schema(description = "求职者数量")
    private Long seekerCount;

    @Schema(description = "今日新增用户")
    private Long todayNewUserCount;

    // ========== 简历统计 ==========
    @Schema(description = "简历总数")
    private Long totalResumeCount;

    @Schema(description = "今日新增简历")
    private Long todayNewResumeCount;

    @Schema(description = "已解析简历数")
    private Long parsedResumeCount;

    // ========== 岗位统计 ==========
    @Schema(description = "岗位总数")
    private Long totalJobCount;

    @Schema(description = "活跃岗位数（招聘中）")
    private Long activeJobCount;

    @Schema(description = "今日新增岗位")
    private Long todayNewJobCount;

    // ========== 投递统计 ==========
    @Schema(description = "投递总数")
    private Long totalApplicationCount;

    @Schema(description = "今日新增投递")
    private Long todayNewApplicationCount;

    // ========== 趋势数据 ==========
    @Schema(description = "用户增长趋势（近7天）")
    private List<TrendData> userGrowthTrend;

    @Schema(description = "投递趋势（近7天）")
    private List<TrendData> applicationTrend;

    @Schema(description = "简历趋势（近7天）")
    private List<TrendData> resumeTrend;

    /**
     * 趋势数据
     */
    @Data
    @Builder
    @Schema(description = "趋势数据")
    public static class TrendData {
        @Schema(description = "日期")
        private String date;

        @Schema(description = "数量")
        private Long count;
    }
}
