package app.intelligent.resume.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 投递记录响应DTO
 *
 * @author Intelligent Resume Team
 */
@Data
@Schema(description = "投递记录响应")
public class ApplicationResponse {

    /**
     * 投递ID
     */
    @Schema(description = "投递ID")
    private Long id;

    /**
     * 岗位ID
     */
    @Schema(description = "岗位ID")
    private Long jobId;

    /**
     * 简历ID
     */
    @Schema(description = "简历ID")
    private Long resumeId;

    /**
     * 求职者ID
     */
    @Schema(description = "求职者ID")
    private Long userId;

    /**
     * 求职信
     */
    @Schema(description = "求职信")
    private String coverLetter;

    /**
     * 投递状态：0-待查看 1-已查看 2-通过筛选 3-不合适 4-已发offer
     */
    @Schema(description = "投递状态")
    private Integer applicationStatus;

    /**
     * 状态描述
     */
    @Schema(description = "状态描述")
    private String statusText;

    /**
     * HR反馈
     */
    @Schema(description = "HR反馈")
    private String hrFeedback;

    /**
     * 投递时间
     */
    @Schema(description = "投递时间")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    // ========== 岗位信息（求职者查看时） ==========

    /**
     * 职位名称
     */
    @Schema(description = "职位名称")
    private String jobTitle;

    /**
     * 公司名称
     */
    @Schema(description = "公司名称")
    private String companyName;

    /**
     * 工作城市
     */
    @Schema(description = "工作城市")
    private String city;

    /**
     * 薪资范围
     */
    @Schema(description = "薪资范围")
    private String salaryRange;

    /**
     * HR用户ID（用于发起会话）
     */
    @Schema(description = "HR用户ID")
    private Long hrUserId;

    /**
     * 匹配度分数（0-100）
     */
    @Schema(description = "匹配度分数")
    private Integer matchScore;

    // ========== 简历信息（HR查看时） ==========

    /**
     * 简历标题
     */
    @Schema(description = "简历标题")
    private String resumeTitle;

    /**
     * 求职者姓名
     */
    @Schema(description = "求职者姓名")
    private String seekerName;

    /**
     * 求职者姓名（别名，用于前端兼容）
     */
    @Schema(description = "求职者姓名")
    private String userName;

    /**
     * 求职者电话
     */
    @Schema(description = "求职者电话")
    private String seekerPhone;

    /**
     * 求职者邮箱
     */
    @Schema(description = "求职者邮箱")
    private String seekerEmail;

    /**
     * 技能标签
     */
    @Schema(description = "技能标签")
    private String skillTags;

    /**
     * 获取状态文本描述
     */
    public String getStatusText() {
        if (applicationStatus == null) return "未知";
        return switch (applicationStatus) {
            case 0 -> "待查看";
            case 1 -> "已查看";
            case 2 -> "通过筛选";
            case 3 -> "不合适";
            case 4 -> "已发offer";
            default -> "未知";
        };
    }
}
