package app.intelligent.resume.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 简历详情实体类
 *
 * @author Intelligent Resume Team
 */
@Data
@TableName("resume_detail")
public class ResumeDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 详情ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 简历ID
     */
    private Long resumeId;

    /**
     * 姓名
     */
    private String name;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 性别：0-女 1-男
     */
    private Integer gender;

    /**
     * 出生日期
     */
    private LocalDate birthDate;

    /**
     * 年龄
     */
    private Integer age;

    /**
     * 工作年限
     */
    private Integer workYears;

    /**
     * 所在城市
     */
    private String currentCity;

    /**
     * 求职状态：在职/离职
     */
    private String jobStatus;

    /**
     * 期望职位
     */
    private String expectedPosition;

    /**
     * 期望薪资
     */
    private String expectedSalary;

    /**
     * 期望城市
     */
    private String expectedCity;

    /**
     * 工作类型：全职/兼职/实习
     */
    private String jobType;

    /**
     * 教育经历（JSON数组）
     */
    private String educationJson;

    /**
     * 最高学历
     */
    private String highestEducation;

    /**
     * 工作经历（JSON数组）
     */
    private String workExperienceJson;

    /**
     * 项目经验（JSON数组）
     */
    private String projectExperienceJson;

    /**
     * 技能列表（JSON数组）
     */
    private String skillsJson;

    /**
     * 技能标签（逗号分隔）
     */
    private String skillTags;

    /**
     * 证书奖项（JSON数组）
     */
    private String certificatesJson;

    /**
     * 自我评价
     */
    private String selfEvaluation;

    /**
     * 个人优势
     */
    private String advantages;

    /**
     * 其他信息
     */
    private String extraInfo;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
