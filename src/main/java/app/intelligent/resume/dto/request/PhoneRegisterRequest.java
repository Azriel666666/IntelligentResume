package app.intelligent.resume.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 手机号注册请求DTO
 *
 * @author Intelligent Resume Team
 */
@Data
@Schema(description = "手机号注册请求")
public class PhoneRegisterRequest {

    @Schema(description = "手机号", example = "13800138000", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "手机号不能为空")
    private String phone;

    @Schema(description = "密码（8-16位，必须包含数字和字母）", example = "abc12345", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "密码不能为空")
    @Size(min = 8, max = 16, message = "密码长度必须在8-16位之间")
    private String password;

    @Schema(description = "确认密码", example = "abc12345", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;

    @Schema(description = "验证码", example = "123456", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "验证码不能为空")
    private String code;

    @Schema(description = "用户类型：2-HR, 3-求职者", example = "3", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "用户类型不能为空")
    private Integer userType;

    @Schema(description = "公司名称（HR用户必填）", example = "某某科技公司")
    private String companyName;

    @Schema(description = "请求ID（用于幂等性）", example = "uuid-123456", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "请求ID不能为空")
    private String requestId;
}
