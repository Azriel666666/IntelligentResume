package app.intelligent.resume.controller;

import app.intelligent.resume.common.exception.BusinessException;
import app.intelligent.resume.common.result.Result;
import app.intelligent.resume.common.result.ResultCode;
import app.intelligent.resume.config.MinioConfig;
import app.intelligent.resume.service.IFileStorageService;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 文件控制器
 * 提供文件代理访问功能
 *
 * @author Intelligent Resume Team
 */
@Tag(name = "文件管理", description = "文件上传下载接口")
@Slf4j
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final MinioClient minioClient;
    private final MinioConfig minioConfig;
    private final IFileStorageService fileStorageService;

    /**
     * 代理访问头像文件
     * 
     * @param path 文件路径（不包含bucket名称）
     */
    @Operation(summary = "获取头像", description = "代理访问MinIO中的头像文件")
    @GetMapping("/avatar/**")
    public void getAvatar(HttpServletResponse response) throws Exception {
        // 从请求路径中提取文件路径
        String requestUri = org.springframework.web.context.request.RequestContextHolder
                .currentRequestAttributes()
                .toString();
        
        // 使用 HttpServletRequest 获取完整路径
        jakarta.servlet.http.HttpServletRequest request = 
            ((org.springframework.web.context.request.ServletRequestAttributes) 
                org.springframework.web.context.request.RequestContextHolder.getRequestAttributes())
            .getRequest();
        
        String fullPath = request.getRequestURI();
        String contextPath = request.getContextPath();
        String path = fullPath.substring(contextPath.length() + "/api/files/avatar/".length());
        
        if (path.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        
        String bucketName = minioConfig.getAvatarBucket();
        if (bucketName == null) {
            bucketName = minioConfig.getBucketName();
        }
        
        streamFile(bucketName, path, response);
    }

    /**
     * 代理访问简历文件
     */
    @Operation(summary = "获取简历文件", description = "代理访问MinIO中的简历文件")
    @GetMapping("/resume/**")
    public void getResume(HttpServletResponse response) throws Exception {
        jakarta.servlet.http.HttpServletRequest request = 
            ((org.springframework.web.context.request.ServletRequestAttributes) 
                org.springframework.web.context.request.RequestContextHolder.getRequestAttributes())
            .getRequest();
        
        String fullPath = request.getRequestURI();
        String contextPath = request.getContextPath();
        String path = fullPath.substring(contextPath.length() + "/api/files/resume/".length());
        
        if (path.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        
        String bucketName = minioConfig.getResumeBucket();
        if (bucketName == null) {
            bucketName = minioConfig.getBucketName();
        }
        
        streamFile(bucketName, path, response);
    }

    /**
     * 代理访问聊天文件
     */
    @Operation(summary = "获取聊天文件", description = "代理访问MinIO中的聊天文件")
    @GetMapping("/chat/**")
    public void getChatFile(HttpServletResponse response) throws Exception {
        jakarta.servlet.http.HttpServletRequest request = 
            ((org.springframework.web.context.request.ServletRequestAttributes) 
                org.springframework.web.context.request.RequestContextHolder.getRequestAttributes())
            .getRequest();
        
        String fullPath = request.getRequestURI();
        String contextPath = request.getContextPath();
        String path = fullPath.substring(contextPath.length() + "/api/files/chat/".length());
        
        if (path.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        
        String bucketName = minioConfig.getChatBucket();
        if (bucketName == null) {
            bucketName = minioConfig.getBucketName();
        }
        
        streamFile(bucketName, path, response);
    }

    /**
     * 流式输出文件
     */
    private void streamFile(String bucketName, String objectName, HttpServletResponse response) {
        try {
            // 获取文件信息
            StatObjectResponse stat = minioClient.statObject(StatObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build());
            
            // 设置响应头
            String contentType = stat.contentType();
            if (contentType == null || contentType.isEmpty()) {
                contentType = getContentType(objectName);
            }
            response.setContentType(contentType);
            response.setContentLengthLong(stat.size());
            
            // 设置缓存头（头像可以缓存较长时间）
            response.setHeader(HttpHeaders.CACHE_CONTROL, "max-age=86400"); // 缓存1天
            
            // 获取文件流并输出
            try (InputStream inputStream = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build())) {
                StreamUtils.copy(inputStream, response.getOutputStream());
                response.flushBuffer();
            }
            
        } catch (Exception e) {
            log.error("获取文件失败, bucket={}, object={}", bucketName, objectName, e);
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    /**
     * 根据文件扩展名获取Content-Type
     */
    private String getContentType(String filename) {
        if (filename == null) {
            return MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }
        
        String lowerName = filename.toLowerCase();
        if (lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg")) {
            return MediaType.IMAGE_JPEG_VALUE;
        } else if (lowerName.endsWith(".png")) {
            return MediaType.IMAGE_PNG_VALUE;
        } else if (lowerName.endsWith(".gif")) {
            return MediaType.IMAGE_GIF_VALUE;
        } else if (lowerName.endsWith(".webp")) {
            return "image/webp";
        } else if (lowerName.endsWith(".pdf")) {
            return MediaType.APPLICATION_PDF_VALUE;
        } else if (lowerName.endsWith(".doc")) {
            return "application/msword";
        } else if (lowerName.endsWith(".docx")) {
            return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        }
        
        return MediaType.APPLICATION_OCTET_STREAM_VALUE;
    }
}
