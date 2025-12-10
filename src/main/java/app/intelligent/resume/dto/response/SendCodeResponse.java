package app.intelligent.resume.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 发送验证码响应DTO
 *
 * @author Intelligent Resume Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "发送验证码响应")
public class SendCodeResponse {

    @Schema(description = "是否发送成功", example = "true")
    private Boolean success;

    @Schema(description = "下次可发送时间（秒）", example = "60")
    private Integer nextSendSeconds;

    @Schema(description = "提示信息", example = "验证码已发送")
    private String message;
}
