package app.intelligent.resume.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 处理投递状态请求DTO
 * 对应需求文档4.8.4
 *
 * @author Intelligent Resume Team
 */
@Data
@Schema(description = "处理投递状态请求")
public class ApplicationStatusRequest {

    /**
     * 投递状态：0-待查看 1-已查看 2-通过筛选 3-不合适 4-已发offer
     */
    @NotNull(message = "状态不能为空")
    @Min(value = 0, message = "状态值无效")
    @Max(value = 4, message = "状态值无效")
    @Schema(description = "投递状态：0-待查看 1-已查看 2-通过筛选 3-不合适 4-已发offer", example = "2")
    private Integer status;

    /**
     * HR反馈（可选）
     */
    @Schema(description = "HR反馈", example = "感谢您的投递，我们会尽快与您联系")
    private String feedback;
}
