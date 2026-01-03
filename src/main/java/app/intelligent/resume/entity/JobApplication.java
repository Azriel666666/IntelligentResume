package app.intelligent.resume.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 投递记录实体类
 * 对应数据库表 job_application
 *
 * @author Intelligent Resume Team
 */
@Data
@TableName("job_application")
public class JobApplication implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 投递ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 岗位ID
     */
    private Long jobId;

    /**
     * 简历ID
     */
    private Long resumeId;

    /**
     * 求职者ID
     */
    private Long userId;

    /**
     * 求职信
     */
    private String coverLetter;

    /**
     * 投递状态：0-待查看 1-已查看 2-通过筛选 3-不合适 4-已发offer
     */
    private Integer applicationStatus;

    /**
     * HR反馈
     */
    private String hrFeedback;

    /**
     * 创建时间（投递时间）
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
