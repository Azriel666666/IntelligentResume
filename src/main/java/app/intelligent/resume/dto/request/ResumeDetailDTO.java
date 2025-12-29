package app.intelligent.resume.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 简历详情DTO - 用于在线创建/更新简历
 */
@Data
@Schema(description = "简历详情")
public class ResumeDetailDTO {

    @Schema(description = "姓名")
    private String name;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "性别：0-女 1-男")
    private Integer gender;

    @Schema(description = "出生日期")
    private LocalDate birthDate;

    @Schema(description = "工作年限")
    private Integer workYears;

    @Schema(description = "所在城市")
    private String currentCity;

    @Schema(description = "求职状态")
    private String jobStatus;

    @Schema(description = "期望职位")
    private String expectedPosition;

    @Schema(description = "期望薪资")
    private String expectedSalary;

    @Schema(description = "期望城市")
    private String expectedCity;

    @Schema(description = "工作类型")
    private String jobType;

    @Schema(description = "最高学历")
    private String highestEducation;

    @Schema(description = "教育经历列表")
    private List<EducationDTO> educationList;

    @Schema(description = "工作经历列表")
    private List<WorkExperienceDTO> workExperienceList;

    @Schema(description = "项目经验列表")
    private List<ProjectExperienceDTO> projectExperienceList;

    @Schema(description = "技能列表")
    private List<String> skills;

    @Schema(description = "证书奖项列表")
    private List<String> certificates;

    @Schema(description = "自我评价")
    private String selfEvaluation;

    @Data
    @Schema(description = "教育经历")
    public static class EducationDTO {
        @Schema(description = "学校名称")
        private String school;
        @Schema(description = "专业")
        private String major;
        @Schema(description = "学历")
        private String degree;
        @Schema(description = "开始时间")
        private String startDate;
        @Schema(description = "结束时间")
        private String endDate;
    }

    @Data
    @Schema(description = "工作经历")
    public static class WorkExperienceDTO {
        @Schema(description = "公司名称")
        private String company;
        @Schema(description = "职位")
        private String position;
        @Schema(description = "开始时间")
        private String startDate;
        @Schema(description = "结束时间")
        private String endDate;
        @Schema(description = "工作内容")
        private String description;
    }

    @Data
    @Schema(description = "项目经验")
    public static class ProjectExperienceDTO {
        @Schema(description = "项目名称")
        private String projectName;
        @Schema(description = "担任角色")
        private String role;
        @Schema(description = "开始时间")
        private String startDate;
        @Schema(description = "结束时间")
        private String endDate;
        @Schema(description = "项目描述")
        private String description;
    }
}
