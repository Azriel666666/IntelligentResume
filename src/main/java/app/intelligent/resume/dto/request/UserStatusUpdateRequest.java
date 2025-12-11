package app.intelligent.resume.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 更新用户状态请求DTO
 *
 * @author Intelligent Resume Team
 */
@Data
@Schema(description = "更新用户状态请求")
public class UserStatusUpdateRequest {

    @Schema(description = "用户状态：0-禁用 1-正常", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "状态不能为空")
    private Integer status;
}