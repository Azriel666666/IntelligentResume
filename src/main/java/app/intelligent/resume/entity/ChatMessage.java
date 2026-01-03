package app.intelligent.resume.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 聊天消息实体类
 * 对应数据库表 chat_message
 *
 * @author Intelligent Resume Team
 */
@Data
@TableName("chat_message")
public class ChatMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 消息ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 会话ID
     */
    private Long conversationId;

    /**
     * 发送者ID
     */
    private Long senderId;

    /**
     * 接收者ID
     */
    private Long receiverId;

    /**
     * 消息类型：TEXT-文本 IMAGE-图片 FILE-文件 INTERVIEW-面试邀请 OFFER-Offer SYSTEM-系统消息
     */
    private String messageType;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 附件信息（JSON）
     */
    private String attachments;

    /**
     * 额外数据（JSON，如面试邀请详情）
     */
    private String extraData;

    /**
     * 是否已读：0-未读 1-已读
     */
    private Integer isRead;

    /**
     * 阅读时间
     */
    private LocalDateTime readTime;

    /**
     * 是否撤回：0-正常 1-已撤回
     */
    private Integer isRecalled;

    /**
     * 撤回时间
     */
    private LocalDateTime recallTime;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
