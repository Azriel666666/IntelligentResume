package app.intelligent.resume.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 面试邀请实体类
 * 对应数据库表 interview_invitation
 *
 * @author Intelligent Resume Team
 */
@Data
@TableName("interview_invitation")
public class InterviewInvitation implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 邀请ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联会话ID
     */
    private Long conversationId;

    /**
     * 关联消息ID
     */
    private Long messageId;

    /**
     * HR用户ID
     */
    private Long hrUserId;

    /**
     * 求职者用户ID
     */
    private Long seekerUserId;

    /**
     * 岗位ID
     */
    private Long jobId;

    /**
     * 投递记录ID
     */
    private Long applicationId;

    /**
     * 面试类型：ONSITE-现场 PHONE-电话 VIDEO-视频
     */
    private String interviewType;

    /**
     * 面试时间
     */
    private LocalDateTime interviewTime;

    /**
     * 预计时长（分钟）
     */
    private Integer interviewDuration;

    /**
     * 面试地址（现场面试）
     */
    private String interviewAddress;

    /**
     * 在线会议链接（视频面试）
     */
    private String onlineMeetingUrl;

    /**
     * 面试官姓名
     */
    private String interviewerName;

    /**
     * 面试官职位
     */
    private String interviewerPosition;

    /**
     * 面试官电话
     */
    private String interviewerPhone;

    /**
     * 联系人
     */
    private String contactPerson;

    /**
     * 联系电话
     */
    private String contactPhone;

    /**
     * 备注说明
     */
    private String notes;

    /**
     * 状态：PENDING-待确认 ACCEPTED-已接受 DECLINED-已拒绝 CANCELLED-已取消 RESCHEDULED-已改期 COMPLETED-已完成
     */
    private String status;

    /**
     * 求职者回复
     */
    private String seekerResponse;

    /**
     * 回复时间
     */
    private LocalDateTime responseTime;

    /**
     * 取消原因
     */
    private String cancelReason;

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
