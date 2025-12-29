package app.intelligent.resume.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 更新简历请求DTO
 *
 * @author Intelligent Resume Team
 */
@Data
@Schema(description = "更新简历请求")
public class ResumeUpdateRequest {

    @Schema(description = "简历标题")
    private String title;

    @Schema(description = "状态：0-隐藏 1-正常 2-草稿")
    private Integer status;

    @Schema(description = "简历详情")
    private ResumeDetailDTO detail;
}