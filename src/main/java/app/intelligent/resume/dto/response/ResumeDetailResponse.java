package app.intelligent.resume.dto.response;

import app.intelligent.resume.dto.request.ResumeDetailDTO;
import app.intelligent.resume.entity.AnalysisReport;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 简历详情响应DTO - 包含简历基本信息、详情和分析报告
 *
 * @author Intelligent Resume Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "简历详情响应")
public class ResumeDetailResponse {

    @Schema(description = "简历ID")
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "简历标题")
    private String title;

    @Schema(description = "文件URL")
    private String fileUrl;

    @Schema(description = "原始文件名")
    private String fileName;

    @Schema(description = "文件类型")
    private String fileType;

    @Schema(description = "文件大小（字节）")
    private Long fileSize;

    @Schema(description = "解析状态：0-未解析 1-解析中 2-成功 3-失败")
    private Integer parseStatus;

    @Schema(description = "解析错误信息")
    private String parseError;

    @Schema(description = "综合评分")
    private BigDecimal totalScore;

    @Schema(description = "完整度评分")
    private BigDecimal completenessScore;

    @Schema(description = "质量评分")
    private BigDecimal qualityScore;

    @Schema(description = "格式评分")
    private BigDecimal formatScore;

    @Schema(description = "是否默认简历")
    private Integer isDefault;

    @Schema(description = "状态：0-隐藏 1-正常 2-草稿")
    private Integer status;

    @Schema(description = "查看次数")
    private Integer viewCount;

    @Schema(description = "下载次数")
    private Integer downloadCount;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "简历详情")
    private ResumeDetailDTO detail;

    @Schema(description = "分析报告")
    private AnalysisReport analysisReport;

    @Schema(description = "校园经历JSON")
    private String extraInfo;
}