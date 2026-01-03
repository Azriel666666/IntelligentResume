package app.intelligent.resume.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 通知响应DTO
 *
 * @author Intelligent Resume Team
 */
@Data
@Schema(description = "通知响应")
public class NotificationResponse {

    @Schema(description = "通知ID")
    private Long id;

    @Schema(description = "通知类型")
    private String type;

    @Schema(description = "通知标题")
    private String title;

    @Schema(description = "通知内容")
    private String content;

    @Schema(description = "额外数据")
    private Map<String, Object> extraData;

    @Schema(description = "关联业务ID")
    private Long relatedId;

    @Schema(description = "关联业务类型")
    private String relatedType;

    @Schema(description = "是否已读")
    private Boolean isRead;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
