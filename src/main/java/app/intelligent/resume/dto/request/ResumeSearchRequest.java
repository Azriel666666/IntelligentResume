package app.intelligent.resume.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 简历搜索请求DTO - HR搜索简历库
 *
 * @author Intelligent Resume Team
 */
@Data
@Schema(description = "简历搜索请求")
public class ResumeSearchRequest {

    @Schema(description = "关键词（姓名、技能、职位）")
    private String keyword;

    @Schema(description = "学历要求")
    private String education;

    @Schema(description = "最小工作年限")
    private Integer workYearsMin;

    @Schema(description = "最大工作年限")
    private Integer workYearsMax;

    @Schema(description = "最低期望薪资")
    private String salaryMin;

    @Schema(description = "最高期望薪资")
    private String salaryMax;

    @Schema(description = "所在城市")
    private String city;

    @Schema(description = "技能标签（逗号分隔）")
    private String skillTags;

    @Schema(description = "页码", example = "1")
    private Integer page = 1;

    @Schema(description = "每页数量", example = "10")
    private Integer size = 10;
}