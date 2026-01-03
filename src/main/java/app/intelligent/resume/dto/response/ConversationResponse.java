package app.intelligent.resume.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 会话响应DTO
 *
 * @author Intelligent Resume Team
 */
@Data
@Schema(description = "会话响应")
public class ConversationResponse {

    @Schema(description = "会话ID")
    private Long id;

    @Schema(description = "HR用户ID")
    private Long hrUserId;

    @Schema(description = "求职者用户ID")
    private Long seekerUserId;

    @Schema(description = "关联岗位ID")
    private Long jobId;

    @Schema(description = "职位名称")
    private String jobTitle;

    @Schema(description = "公司名称")
    private String companyName;

    @Schema(description = "最后一条消息内容")
    private String lastMessage;

    @Schema(description = "最后消息时间")
    private LocalDateTime lastMessageTime;

    @Schema(description = "HR未读消息数")
    private Integer hrUnreadCount;

    @Schema(description = "求职者未读消息数")
    private Integer seekerUnreadCount;

    @Schema(description = "当前用户未读数")
    private Integer unreadCount;

    @Schema(description = "状态：1-正常 2-HR关闭 3-系统关闭")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    // ========== 对方用户信息 ==========

    @Schema(description = "对方用户ID")
    private Long otherUserId;

    @Schema(description = "对方用户名")
    private String otherUserName;

    @Schema(description = "对方头像")
    private String otherUserAvatar;

    @Schema(description = "最后消息内容")
    private String lastMessageContent;

    @Schema(description = "关联投递ID")
    private Long applicationId;
}
