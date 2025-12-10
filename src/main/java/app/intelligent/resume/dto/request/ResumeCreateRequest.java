package app.intelligent.resume.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 创建简历请求DTO
 *
 * @author Intelligent Resume Team
 */
@Data
@Schema(description = "创建简历请求")
public class ResumeCreateRequest {

    @Schema(description = "简历标题")
    @NotBlank(message = "简历标题不能为空")
    private String title;

    @Schema(description = "简历文件URL")
    private String fileUrl;

    @Schema(description = "原始文件名")
    private String fileName;

    @Schema(description = "文件类型：pdf/word/image")
    private String fileType;

    @Schema(description = "文件大小（字节）")
    private Long fileSize;
}
