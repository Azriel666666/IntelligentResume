package app.intelligent.resume.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 岗位查询请求DTO
 *
 * @author Intelligent Resume Team
 */
@Data
@Schema(description = "岗位查询请求")
public class JobQueryRequest {

    @Schema(description = "关键词（职位名称/公司名称/岗位描述）")
    private String keyword;

    @Schema(description = "工作城市")
    private String city;

    @Schema(description = "最低薪资（K）")
    private BigDecimal salaryMin;

    @Schema(description = "最高薪资（K）")
    private BigDecimal salaryMax;

    @Schema(description = "最低工作年限")
    private Integer workYearsMin;

    @Schema(description = "最高工作年限")
    private Integer workYearsMax;

    @Schema(description = "学历要求：本科/硕士/博士")
    private String educationRequired;

    @Schema(description = "工作类型：全职/兼职/实习")
    private String jobType;

    @Schema(description = "技能标签（逗号分隔）")
    private String skillTags;

    @Schema(description = "岗位状态：0-下架 1-招聘中 2-暂停")
    private Integer status;

    @Schema(description = "页码", example = "1")
    private Integer page = 1;

    @Schema(description = "每页数量", example = "10")
    private Integer size = 10;

    @Schema(description = "排序字段：createTime/salary/viewCount")
    private String sortBy = "createTime";

    @Schema(description = "排序方式：asc/desc")
    private String sortOrder = "desc";
}
