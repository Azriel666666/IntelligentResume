package app.intelligent.resume.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 岗位详情响应DTO
 *
 * @author Intelligent Resume Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "岗位详情响应")
public class JobDetailResponse {

    @Schema(description = "岗位ID")
    private Long id;

    @Schema(description = "发布者ID")
    private Long publisherId;

    @Schema(description = "发布者名称")
    private String publisherName;

    @Schema(description = "职位名称")
    private String jobTitle;

    @Schema(description = "公司名称")
    private String companyName;

    @Schema(description = "最低薪资（K）")
    private BigDecimal salaryMin;

    @Schema(description = "最高薪资（K）")
    private BigDecimal salaryMax;

    @Schema(description = "薪资范围")
    private String salaryRange;

    @Schema(description = "工作城市")
    private String city;

    @Schema(description = "详细地址")
    private String workAddress;

    @Schema(description = "最低工作年限")
    private Integer workYearsMin;

    @Schema(description = "最高工作年限")
    private Integer workYearsMax;

    @Schema(description = "工作年限要求描述")
    private String workYearsDesc;

    @Schema(description = "学历要求")
    private String educationRequired;

    @Schema(description = "工作类型")
    private String jobType;

    @Schema(description = "所属部门")
    private String department;

    @Schema(description = "岗位描述")
    private String jobDescription;

    @Schema(description = "岗位要求")
    private String jobRequirements;

    @Schema(description = "技能要求（JSON数组）")
    private String skillsRequired;

    @Schema(description = "技能标签（逗号分隔）")
    private String skillTags;

    @Schema(description = "福利标签")
    private String welfareTags;

    @Schema(description = "招聘人数")
    private Integer recruiterCount;

    @Schema(description = "浏览次数")
    private Integer viewCount;

    @Schema(description = "申请次数")
    private Integer applyCount;

    @Schema(description = "状态：0-下架 1-招聘中 2-暂停")
    private Integer status;

    @Schema(description = "状态描述")
    private String statusDesc;

    @Schema(description = "是否置顶")
    private Integer isTop;

    @Schema(description = "过期时间")
    private LocalDateTime expireTime;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "与当前用户简历的匹配度（求职者查看时）")
    private BigDecimal matchScore;

    /**
     * 获取状态描述
     */
    public String getStatusDesc() {
        if (status == null) return "未知";
        return switch (status) {
            case 0 -> "已下架";
            case 1 -> "招聘中";
            case 2 -> "已暂停";
            case 3 -> "待审核";
            default -> "未知";
        };
    }

    /**
     * 获取工作年限描述
     */
    public String getWorkYearsDesc() {
        if (workYearsMin == null && workYearsMax == null) {
            return "经验不限";
        }
        if (workYearsMin != null && workYearsMax != null) {
            if (workYearsMin.equals(workYearsMax)) {
                return workYearsMin + "年";
            }
            return workYearsMin + "-" + workYearsMax + "年";
        }
        if (workYearsMin != null) {
            return workYearsMin + "年以上";
        }
        return workYearsMax + "年以下";
    }
}
