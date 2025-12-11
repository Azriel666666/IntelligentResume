package app.intelligent.resume.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 修改手机号请求DTO
 *
 * @author Intelligent Resume Team
 */
@Data
@Schema(description = "修改手机号请求")
public class PhoneUpdateRequest {

    @Schema(description = "新手机号", requiredMode = Schema.RequiredMode.REQUIRED, example = "13900139000")
    @NotBlank(message = "新手机号不能为空")
    private String newPhone;

    @Schema(description = "验证码", requiredMode = Schema.RequiredMode.REQUIRED, example = "123456")
    @NotBlank(message = "验证码不能为空")
    private String code;
}