package app.intelligent.resume.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 岗位实体类
 *
 * @author Intelligent Resume Team
 */
@Data
@TableName("job")
public class Job implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 岗位ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 发布者ID（HR）
     */
    private Long publisherId;

    /**
     * 职位名称
     */
    private String jobTitle;

    /**
     * 公司名称
     */
    private String companyName;

    /**
     * 最低薪资（K）
     */
    private BigDecimal salaryMin;

    /**
     * 最高薪资（K）
     */
    private BigDecimal salaryMax;

    /**
     * 薪资范围：8K-15K
     */
    private String salaryRange;

    /**
     * 工作城市
     */
    private String city;

    /**
     * 详细地址
     */
    private String workAddress;

    /**
     * 最低工作年限
     */
    private Integer workYearsMin;

    /**
     * 最高工作年限
     */
    private Integer workYearsMax;

    /**
     * 学历要求：本科/硕士/博士
     */
    private String educationRequired;

    /**
     * 工作类型：全职/兼职/实习
     */
    private String jobType;

    /**
     * 所属部门
     */
    private String department;

    /**
     * 岗位描述
     */
    private String jobDescription;

    /**
     * 岗位要求
     */
    private String jobRequirements;

    /**
     * 技能要求（JSON数组）
     */
    private String skillsRequired;

    /**
     * 技能标签（逗号分隔）
     */
    private String skillTags;

    /**
     * 福利标签
     */
    private String welfareTags;

    /**
     * 招聘人数
     */
    private Integer recruiterCount;

    /**
     * 浏览次数
     */
    private Integer viewCount;

    /**
     * 申请次数
     */
    private Integer applyCount;

    /**
     * 状态：0-已下架 1-招聘中 2-已暂停 3-待审核
     */
    private Integer status;

    /**
     * 是否置顶
     */
    private Integer isTop;

    /**
     * 过期时间
     */
    private LocalDateTime expireTime;

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

    /**
     * 逻辑删除
     */
    @TableLogic
    private Integer deleted;
}
