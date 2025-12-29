package app.intelligent.resume.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 在线创建简历请求DTO
 *
 * @author Intelligent Resume Team
 */
@Data
@Schema(description = "在线创建简历请求")
public class ResumeOnlineCreateRequest {

    @Schema(description = "简历标题", required = true)
    @NotBlank(message = "简历标题不能为空")
    private String title;

    @Schema(description = "简历详情")
    private ResumeDetailDTO detail;
}