package app.intelligent.resume.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 批量匹配请求DTO
 *
 * @author Intelligent Resume Team
 */
@Data
@Schema(description = "批量匹配请求")
public class BatchMatchRequest {

    @Schema(description = "岗位ID")
    @NotNull(message = "岗位ID不能为空")
    private Long jobId;

    @Schema(description = "简历ID列表")
    @NotEmpty(message = "简历ID列表不能为空")
    private List<Long> resumeIds;
}
