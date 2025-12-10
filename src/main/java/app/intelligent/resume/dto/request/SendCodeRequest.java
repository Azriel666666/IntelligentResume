package app.intelligent.resume.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 发送验证码请求DTO
 *
 * @author Intelligent Resume Team
 */
@Data
@Schema(description = "发送验证码请求")
public class SendCodeRequest {

    @Schema(description = "手机号", example = "13800138000", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "手机号不能为空")
    private String phone;

    @Schema(description = "验证码类型：REGISTER-注册, LOGIN-登录, RESET_PASSWORD-重置密码",
            example = "REGISTER",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "验证码类型不能为空")
    private String type;

    @Schema(description = "请求ID（用于幂等性）", example = "uuid-123456", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "请求ID不能为空")
    private String requestId;
}
