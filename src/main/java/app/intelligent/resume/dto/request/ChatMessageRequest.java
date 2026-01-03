package app.intelligent.resume.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 发送消息请求DTO
 *
 * @author Intelligent Resume Team
 */
@Data
@Schema(description = "发送消息请求")
public class ChatMessageRequest {

    /**
     * 消息内容
     */
    @NotBlank(message = "消息内容不能为空")
    @Schema(description = "消息内容", example = "您好，请问还在招人吗？")
    private String content;

    /**
     * 消息类型：TEXT-文本 IMAGE-图片 FILE-文件
     */
    @Schema(description = "消息类型", example = "TEXT", defaultValue = "TEXT")
    private String messageType = "TEXT";

    /**
     * 附件列表（JSON字符串）
     */
    @Schema(description = "附件列表JSON")
    private String attachments;
}
