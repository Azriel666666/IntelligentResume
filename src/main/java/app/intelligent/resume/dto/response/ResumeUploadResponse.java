package app.intelligent.resume.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 简历上传响应DTO
 *
 * @author Intelligent Resume Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "简历上传响应")
public class ResumeUploadResponse {

    @Schema(description = "简历ID")
    private Long id;

    @Schema(description = "简历标题")
    private String title;

    @Schema(description = "文件URL")
    private String fileUrl;

    @Schema(description = "原始文件名")
    private String fileName;

    @Schema(description = "文件类型")
    private String fileType;

    @Schema(description = "文件大小（字节）")
    private Long fileSize;

    @Schema(description = "解析状态：0-未解析 1-解析中 2-成功 3-失败")
    private Integer parseStatus;

    @Schema(description = "提示信息")
    private String message;
}