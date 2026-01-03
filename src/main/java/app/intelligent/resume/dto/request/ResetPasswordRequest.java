package app.intelligent.resume.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 重置密码请求DTO
 *
 * @author Intelligent Resume Team
 */
@Data
@Schema(description = "重置密码请求")
public class ResetPasswordRequest {

    @Schema(description = "手机号", example = "13800138000")
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @Schema(description = "验证码", example = "123456")
    @NotBlank(message = "验证码不能为空")
    private String code;

    @Schema(description = "新密码", example = "abc12345")
    @NotBlank(message = "新密码不能为空")
    private String newPassword;

    @Schema(description = "确认新密码", example = "abc12345")
    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;

    @Schema(description = "请求ID（用于幂等性校验）", example = "uuid-123456")
    @NotBlank(message = "请求ID不能为空")
    private String requestId;
}