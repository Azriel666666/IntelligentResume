package app.intelligent.resume.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 创建岗位请求DTO
 *
 * @author Intelligent Resume Team
 */
@Data
@Schema(description = "创建岗位请求")
public class JobCreateRequest {

    @Schema(description = "职位名称")
    @NotBlank(message = "职位名称不能为空")
    private String jobTitle;

    @Schema(description = "公司名称")
    @NotBlank(message = "公司名称不能为空")
    private String companyName;

    @Schema(description = "最低薪资（K）")
    private BigDecimal salaryMin;

    @Schema(description = "最高薪资（K）")
    private BigDecimal salaryMax;

    @Schema(description = "薪资范围")
    private String salaryRange;

    @Schema(description = "工作城市")
    @NotBlank(message = "工作城市不能为空")
    private String city;

    @Schema(description = "详细地址")
    private String workAddress;

    @Schema(description = "最低工作年限")
    private Integer workYearsMin;

    @Schema(description = "最高工作年限")
    private Integer workYearsMax;

    @Schema(description = "学历要求")
    private String educationRequired;

    @Schema(description = "工作类型：全职/兼职/实习")
    private String jobType;

    @Schema(description = "所属部门")
    private String department;

    @Schema(description = "岗位描述")
    @NotBlank(message = "岗位描述不能为空")
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
}
