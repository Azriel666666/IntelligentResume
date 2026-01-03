package app.intelligent.resume.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建会话请求DTO
 *
 * @author Intelligent Resume Team
 */
@Data
@Schema(description = "创建会话请求")
public class ConversationCreateRequest {

    /**
     * 目标用户ID
     */
    @NotNull(message = "目标用户ID不能为空")
    @Schema(description = "目标用户ID", example = "100")
    private Long targetUserId;

    /**
     * 关联岗位ID（可选）
     */
    @Schema(description = "关联岗位ID", example = "200")
    private Long jobId;

    /**
     * 关联投递ID（可选）
     */
    @Schema(description = "关联投递ID", example = "50")
    private Long applicationId;

    /**
     * 初始消息（可选）
     */
    @Schema(description = "初始消息", example = "您好，我对这个岗位很感兴趣")
    private String initialMessage;
}
