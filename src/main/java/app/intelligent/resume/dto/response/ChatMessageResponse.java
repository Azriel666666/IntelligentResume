package app.intelligent.resume.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 聊天消息响应DTO
 *
 * @author Intelligent Resume Team
 */
@Data
@Schema(description = "聊天消息响应")
public class ChatMessageResponse {

    @Schema(description = "消息ID")
    private Long id;

    @Schema(description = "会话ID")
    private Long conversationId;

    @Schema(description = "发送者ID")
    private Long senderId;

    @Schema(description = "发送者名称")
    private String senderName;

    @Schema(description = "发送者头像")
    private String senderAvatar;

    @Schema(description = "接收者ID")
    private Long receiverId;

    @Schema(description = "消息类型")
    private String messageType;

    @Schema(description = "消息内容")
    private String content;

    @Schema(description = "附件列表")
    private List<AttachmentInfo> attachments;

    @Schema(description = "是否已读")
    private Boolean isRead;

    @Schema(description = "是否撤回")
    private Boolean isRecalled;

    @Schema(description = "是否是自己发送的")
    private Boolean isSelf;

    @Schema(description = "发送时间")
    private LocalDateTime createTime;

    /**
     * 附件信息
     */
    @Data
    @Schema(description = "附件信息")
    public static class AttachmentInfo {
        @Schema(description = "文件名")
        private String fileName;

        @Schema(description = "文件URL")
        private String fileUrl;

        @Schema(description = "文件大小")
        private Long fileSize;

        @Schema(description = "文件类型")
        private String fileType;
    }
}
