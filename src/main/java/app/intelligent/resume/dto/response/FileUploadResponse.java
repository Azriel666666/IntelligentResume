package app.intelligent.resume.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文件上传响应DTO
 * 对应需求文档4.9.1
 *
 * @author Intelligent Resume Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "文件上传响应")
public class FileUploadResponse {

    /**
     * 文件访问URL
     */
    @Schema(description = "文件访问URL", example = "https://storage.example.com/files/xxx.pdf")
    private String url;

    /**
     * 原始文件名
     */
    @Schema(description = "原始文件名", example = "简历.pdf")
    private String fileName;

    /**
     * 文件大小（字节）
     */
    @Schema(description = "文件大小（字节）", example = "102400")
    private Long fileSize;

    /**
     * 文件类型/扩展名
     */
    @Schema(description = "文件类型", example = "pdf")
    private String fileType;

    /**
     * 文件MIME类型
     */
    @Schema(description = "MIME类型", example = "application/pdf")
    private String contentType;
}
