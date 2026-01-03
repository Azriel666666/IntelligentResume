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

    @Schema(description = "工作年限范围（如：0, 1-3, 3-5, 5-10, 10+）")
    private String workYears;

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

    @Schema(description = "技能标签（逗号分隔）- 前端使用skills参数")
    private String skills;

    @Schema(description = "技能标签（逗号分隔）")
    private String skillTags;

    @Schema(description = "页码", example = "1")
    private Integer page = 1;

    @Schema(description = "每页数量", example = "10")
    private Integer size = 10;

    /**
     * 解析工作年限范围
     * 支持格式：0, 1-3, 3-5, 5-10, 10+
     */
    public void parseWorkYears() {
        if (workYears == null || workYears.isEmpty()) {
            return;
        }
        
        if ("0".equals(workYears)) {
            // 应届生
            workYearsMin = 0;
            workYearsMax = 0;
        } else if (workYears.endsWith("+")) {
            // 10年以上
            String minStr = workYears.substring(0, workYears.length() - 1);
            try {
                workYearsMin = Integer.parseInt(minStr);
                workYearsMax = null; // 无上限
            } catch (NumberFormatException e) {
                // 忽略解析错误
            }
        } else if (workYears.contains("-")) {
            // 范围格式：1-3, 3-5, 5-10
            String[] parts = workYears.split("-");
            if (parts.length == 2) {
                try {
                    workYearsMin = Integer.parseInt(parts[0]);
                    workYearsMax = Integer.parseInt(parts[1]);
                } catch (NumberFormatException e) {
                    // 忽略解析错误
                }
            }
        }
    }

    /**
     * 获取有效的技能标签
     * 优先使用 skillTags，如果为空则使用 skills
     */
    public String getEffectiveSkillTags() {
        if (skillTags != null && !skillTags.isEmpty()) {
            return skillTags;
        }
        return skills;
    }
}