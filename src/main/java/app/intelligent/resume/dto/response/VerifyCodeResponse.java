package app.intelligent.resume.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 验证验证码响应DTO
 *
 * @author Intelligent Resume Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "验证验证码响应")
public class VerifyCodeResponse {

    @Schema(description = "验证是否成功", example = "true")
    private Boolean success;

    @Schema(description = "提示信息", example = "验证成功")
    private String message;
}
