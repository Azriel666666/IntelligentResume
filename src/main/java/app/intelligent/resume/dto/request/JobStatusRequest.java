package app.intelligent.resume.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 岗位状态更新请求DTO
 *
 * @author Intelligent Resume Team
 */
@Data
@Schema(description = "岗位状态更新请求")
public class JobStatusRequest {

    @Schema(description = "岗位状态：0-下架 1-招聘中 2-暂停", example = "1")
    @NotNull(message = "状态不能为空")
    @Min(value = 0, message = "状态值无效")
    @Max(value = 2, message = "状态值无效")
    private Integer status;
}
