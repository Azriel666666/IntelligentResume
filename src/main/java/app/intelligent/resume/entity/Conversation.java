package app.intelligent.resume.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 会话实体类
 * 对应数据库表 conversation
 *
 * @author Intelligent Resume Team
 */
@Data
@TableName("conversation")
public class Conversation implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 会话ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * HR用户ID
     */
    private Long hrUserId;

    /**
     * 求职者用户ID
     */
    private Long seekerUserId;

    /**
     * 关联岗位ID
     */
    private Long jobId;

    /**
     * 关联投递ID
     */
    private Long applicationId;

    /**
     * 最后一条消息ID
     */
    private Long lastMessageId;

    /**
     * 最后一条消息内容（预览）
     */
    private String lastMessageContent;

    /**
     * 最后消息时间
     */
    private LocalDateTime lastMessageTime;

    /**
     * HR未读消息数
     */
    private Integer hrUnreadCount;

    /**
     * 求职者未读消息数
     */
    private Integer seekerUnreadCount;

    /**
     * 状态：1-正常 2-HR关闭 3-系统关闭
     */
    private Integer status;

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
