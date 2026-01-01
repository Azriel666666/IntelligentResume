package app.intelligent.resume.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 匹配请求DTO
 *
 * @author Intelligent Resume Team
 */
@Data
@Schema(description = "匹配请求")
public class MatchRequest {

    @Schema(description = "简历ID")
    @NotNull(message = "简历ID不能为空")
    private Long resumeId;

    @Schema(description = "岗位ID")
    @NotNull(message = "岗位ID不能为空")
    private Long jobId;
}
