package app.intelligent.resume.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 投递简历请求DTO
 * 对应需求文档4.8.1
 *
 * @author Intelligent Resume Team
 */
@Data
@Schema(description = "投递简历请求")
public class ApplicationCreateRequest {

    /**
     * 岗位ID
     */
    @NotNull(message = "岗位ID不能为空")
    @Schema(description = "岗位ID", example = "200")
    private Long jobId;

    /**
     * 简历ID
     */
    @NotNull(message = "简历ID不能为空")
    @Schema(description = "简历ID", example = "100")
    private Long resumeId;

    /**
     * 求职信（可选）
     */
    @Schema(description = "求职信", example = "您好，我对贵公司的岗位非常感兴趣...")
    private String coverLetter;
}
