package app.intelligent.resume.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 登录请求DTO
 *
 * @author Intelligent Resume Team
 */
@Data
@Schema(description = "登录请求")
public class LoginRequest {

    @Schema(description = "手机号", example = "13800138000")
    @NotBlank(message = "手机号不能为空")
    private String phone;

    @Schema(description = "密码", example = "abc12345")
    @NotBlank(message = "密码不能为空")
    private String password;
}
