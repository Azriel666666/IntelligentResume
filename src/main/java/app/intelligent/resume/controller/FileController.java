package app.intelligent.resume.controller;

import app.intelligent.resume.common.exception.BusinessException;
import app.intelligent.resume.common.result.Result;
import app.intelligent.resume.common.result.ResultCode;
import app.intelligent.resume.dto.response.FileUploadResponse;
import app.intelligent.resume.service.IFileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 文件控制器
 * 对应需求文档4.9文件模块
 *
 * @author Intelligent Resume Team
 */
@Tag(name = "文件管理", description = "文件上传下载相关接口")
@Slf4j
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Validated
public class FileController {

    private final IFileStorageService fileStorageService;

    /**
     * 支持的图片格式
     */
    private static final List<String> IMAGE_TYPES = Arrays.asList(
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    );

    /**
     * 支持的简历格式
     */
    private static final List<String> RESUME_TYPES = Arrays.asList(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    );

    /**
     * 支持的聊天文件格式（常见文档、图片、压缩包等）
     */
    private static final List<String> CHAT_FILE_TYPES = Arrays.asList(
            // 文档
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/vnd.ms-powerpoint",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            "text/plain",
            "text/csv",
            // 图片
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp", "image/bmp",
            // 压缩包
            "application/zip",
            "application/x-rar-compressed",
            "application/x-7z-compressed",
            "application/gzip",
            // 其他
            "application/json",
            "application/xml",
            "text/html"
    );

    /**
     * 文件大小限制（字节）
     */
    private static final long MAX_AVATAR_SIZE = 2 * 1024 * 1024;    // 头像：2MB
    private static final long MAX_RESUME_SIZE = 10 * 1024 * 1024;   // 简历：10MB
    private static final long MAX_CHAT_FILE_SIZE = 20 * 1024 * 1024; // 聊天文件：20MB
    private static final long MAX_OTHER_SIZE = 20 * 1024 * 1024;    // 其他：20MB

    /**
     * 4.9.1 上传文件
     * POST /api/files/upload
     */
    @Operation(summary = "上传文件", description = "上传文件到存储服务，支持头像、简历、其他类型")
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/upload")
    public Result<FileUploadResponse> uploadFile(
            @Parameter(description = "文件") @RequestParam("file") MultipartFile file,
            @Parameter(description = "文件类型：avatar-头像, resume-简历, other-其他") 
            @RequestParam(value = "type", defaultValue = "other") String type) {
        
        log.info("上传文件, 类型: {}, 文件名: {}, 大小: {}", type, file.getOriginalFilename(), file.getSize());

        // 1. 验证文件
        validateFile(file, type);

        // 2. 上传文件
        String fileUrl = fileStorageService.uploadFile(file, type);

        // 3. 构建响应
        FileUploadResponse response = FileUploadResponse.builder()
                .url(fileUrl)
                .fileName(file.getOriginalFilename())
                .fileSize(file.getSize())
                .fileType(getFileExtension(file.getOriginalFilename()))
                .contentType(file.getContentType())
                .build();

        log.info("文件上传成功, URL: {}", fileUrl);
        return Result.success(Collections.singletonList(response));
    }

    /**
     * 上传头像（便捷接口）
     * POST /api/files/avatar
     */
    @Operation(summary = "上传头像", description = "上传用户头像，支持jpg/png/gif格式，最大2MB")
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/avatar")
    public Result<FileUploadResponse> uploadAvatar(
            @Parameter(description = "头像文件") @RequestParam("file") MultipartFile file) {
        return uploadFile(file, "avatar");
    }

    /**
     * 上传聊天文件
     * POST /api/files/chat
     */
    @Operation(summary = "上传聊天文件", description = "上传聊天中的文件，支持文档、图片、压缩包等常见格式，最大20MB")
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/chat")
    public Result<FileUploadResponse> uploadChatFile(
            @Parameter(description = "聊天文件") @RequestParam("file") MultipartFile file) {
        
        log.info("上传聊天文件, 文件名: {}, 大小: {}, 类型: {}", 
                file.getOriginalFilename(), file.getSize(), file.getContentType());

        // 1. 验证文件
        validateFile(file, "chat");

        // 2. 上传文件到 chat 目录
        String fileUrl = fileStorageService.uploadFile(file, "chat", "chat");

        // 3. 构建响应
        FileUploadResponse response = FileUploadResponse.builder()
                .url(fileUrl)
                .fileName(file.getOriginalFilename())
                .fileSize(file.getSize())
                .fileType(getFileExtension(file.getOriginalFilename()))
                .contentType(file.getContentType())
                .build();

        log.info("聊天文件上传成功, URL: {}", fileUrl);
        return Result.success(Collections.singletonList(response));
    }

    /**
     * 下载聊天文件（流式下载）
     * GET /api/files/chat/download
     */
    @Operation(summary = "下载聊天文件", description = "下载聊天中的文件")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/chat/download")
    public void downloadChatFile(
            @Parameter(description = "文件URL") @RequestParam("fileUrl") String fileUrl,
            @Parameter(description = "文件名") @RequestParam(value = "fileName", required = false) String fileName,
            jakarta.servlet.http.HttpServletResponse response) {
        
        log.info("下载聊天文件, fileUrl: {}, fileName: {}", fileUrl, fileName);

        // 从URL中提取对象名称和存储桶
        String objectName = extractObjectNameFromChatUrl(fileUrl);
        String bucketName = extractBucketNameFromUrl(fileUrl);
        
        if (objectName == null || bucketName == null) {
            throw new BusinessException(ResultCode.FILE_NOT_FOUND);
        }

        // 设置响应头
        if (fileName == null || fileName.isEmpty()) {
            fileName = objectName.substring(objectName.lastIndexOf("/") + 1);
        }
        
        try {
            String encodedFileName = java.net.URLEncoder.encode(fileName, "UTF-8").replace("+", "%20");
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + encodedFileName + "\"; filename*=UTF-8''" + encodedFileName);
            response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
            
            // 获取文件流并写入响应
            try (java.io.InputStream inputStream = fileStorageService.getFileStream(bucketName, objectName);
                 java.io.OutputStream outputStream = response.getOutputStream()) {
                
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                outputStream.flush();
            }
            
            log.info("聊天文件下载成功, fileName: {}", fileName);
        } catch (java.io.IOException e) {
            log.error("聊天文件下载失败, fileUrl: {}", fileUrl, e);
            throw new BusinessException(ResultCode.FILE_DOWNLOAD_FAILED);
        }
    }

    /**
     * 4.9.2 下载/获取文件
     * GET /api/files/download
     */
    @Operation(summary = "获取文件下载链接", description = "获取文件的预签名下载链接")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/download")
    public Result<String> getDownloadUrl(
            @Parameter(description = "文件URL或对象名称") @RequestParam("fileUrl") String fileUrl,
            @Parameter(description = "链接有效期（秒），默认3600") @RequestParam(value = "expiry", defaultValue = "3600") int expiry) {
        
        log.info("获取文件下载链接, fileUrl: {}, expiry: {}", fileUrl, expiry);

        // 从URL中提取对象名称
        String objectName = extractObjectName(fileUrl);
        
        // 获取预签名URL
        String presignedUrl = fileStorageService.getPresignedUrl(objectName, expiry);
        
        return Result.success(Collections.singletonList(presignedUrl));
    }

    /**
     * 删除文件
     * DELETE /api/files
     */
    @Operation(summary = "删除文件", description = "删除指定的文件")
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping
    public Result<Boolean> deleteFile(
            @Parameter(description = "文件URL") @RequestParam("fileUrl") String fileUrl) {
        
        log.info("删除文件, fileUrl: {}", fileUrl);

        boolean deleted = fileStorageService.deleteFile(fileUrl);
        
        if (deleted) {
            return Result.success("删除成功");
        } else {
            return Result.failed("删除失败");
        }
    }

    /**
     * 检查文件是否存在
     * GET /api/files/exists
     */
    @Operation(summary = "检查文件是否存在", description = "检查指定文件是否存在")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/exists")
    public Result<Boolean> checkFileExists(
            @Parameter(description = "文件对象名称") @RequestParam("objectName") String objectName) {
        
        boolean exists = fileStorageService.fileExists(objectName);
        return Result.success(Collections.singletonList(exists));
    }

    /**
     * 批量上传文件
     * POST /api/files/batch
     */
    @Operation(summary = "批量上传文件", description = "批量上传多个文件")
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/batch")
    public Result<FileUploadResponse> batchUpload(
            @Parameter(description = "文件列表") @RequestParam("files") MultipartFile[] files,
            @Parameter(description = "文件类型") @RequestParam(value = "type", defaultValue = "other") String type) {
        
        log.info("批量上传文件, 数量: {}, 类型: {}", files.length, type);

        if (files.length == 0) {
            return Result.failed("请选择要上传的文件");
        }

        if (files.length > 10) {
            return Result.failed("单次最多上传10个文件");
        }

        List<FileUploadResponse> responses = Arrays.stream(files)
                .map(file -> {
                    validateFile(file, type);
                    String fileUrl = fileStorageService.uploadFile(file, type);
                    return FileUploadResponse.builder()
                            .url(fileUrl)
                            .fileName(file.getOriginalFilename())
                            .fileSize(file.getSize())
                            .fileType(getFileExtension(file.getOriginalFilename()))
                            .contentType(file.getContentType())
                            .build();
                })
                .toList();

        log.info("批量上传完成, 成功数量: {}", responses.size());
        return Result.success(responses);
    }

    // ========== 私有方法 ==========

    /**
     * 验证文件
     */
    private void validateFile(MultipartFile file, String type) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ResultCode.FILE_EMPTY);
        }

        String contentType = file.getContentType();
        long fileSize = file.getSize();

        switch (type.toLowerCase()) {
            case "avatar" -> {
                // 验证头像
                if (!IMAGE_TYPES.contains(contentType)) {
                    throw new BusinessException(ResultCode.FAILED, "头像只支持jpg/png/gif格式");
                }
                if (fileSize > MAX_AVATAR_SIZE) {
                    throw new BusinessException(ResultCode.FAILED, "头像大小不能超过2MB");
                }
            }
            case "resume" -> {
                // 验证简历
                if (!RESUME_TYPES.contains(contentType)) {
                    throw new BusinessException(ResultCode.FAILED, "简历只支持pdf/doc/docx格式");
                }
                if (fileSize > MAX_RESUME_SIZE) {
                    throw new BusinessException(ResultCode.FAILED, "简历大小不能超过10MB");
                }
            }
            case "chat" -> {
                // 验证聊天文件（放宽限制，支持更多格式）
                // 不严格限制类型，只限制大小
                if (fileSize > MAX_CHAT_FILE_SIZE) {
                    throw new BusinessException(ResultCode.FAILED, "文件大小不能超过20MB");
                }
            }
            default -> {
                // 其他文件
                if (fileSize > MAX_OTHER_SIZE) {
                    throw new BusinessException(ResultCode.FAILED, "文件大小不能超过20MB");
                }
            }
        }
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }

    /**
     * 从URL提取对象名称
     */
    private String extractObjectName(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return fileUrl;
        }
        
        // 如果是完整URL，提取路径部分
        if (fileUrl.startsWith("http://") || fileUrl.startsWith("https://")) {
            int pathStart = fileUrl.indexOf("/", fileUrl.indexOf("//") + 2);
            if (pathStart > 0) {
                // 跳过bucket名称
                String path = fileUrl.substring(pathStart + 1);
                int nextSlash = path.indexOf("/");
                if (nextSlash > 0) {
                    return path.substring(nextSlash + 1);
                }
                return path;
            }
        }
        
        return fileUrl;
    }

    /**
     * 从聊天文件URL提取对象名称
     * 例如: http://localhost:9005/chat/chat/2026/01/03/xxx.pdf -> chat/2026/01/03/xxx.pdf
     */
    private String extractObjectNameFromChatUrl(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return null;
        }
        
        // 查找存储桶名称后面的路径
        // URL 格式: http://localhost:9005/{bucket}/{objectName}
        String[] buckets = {"chat", "resume", "avatar"};
        for (String bucket : buckets) {
            String pattern = "/" + bucket + "/";
            int bucketIndex = fileUrl.indexOf(pattern);
            if (bucketIndex != -1) {
                return fileUrl.substring(bucketIndex + pattern.length());
            }
        }
        
        return extractObjectName(fileUrl);
    }

    /**
     * 从URL提取存储桶名称
     */
    private String extractBucketNameFromUrl(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return null;
        }
        
        // 根据URL路径判断存储桶
        if (fileUrl.contains("/chat/")) {
            return "chat";
        }
        if (fileUrl.contains("/resume/")) {
            return "resume";
        }
        if (fileUrl.contains("/avatar/")) {
            return "avatar";
        }
        
        return "avatar"; // 默认存储桶
    }
}
