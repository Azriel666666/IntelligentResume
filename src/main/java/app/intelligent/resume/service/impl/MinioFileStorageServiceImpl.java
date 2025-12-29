package app.intelligent.resume.service.impl;

import app.intelligent.resume.common.exception.BusinessException;
import app.intelligent.resume.common.result.ResultCode;
import app.intelligent.resume.config.MinioConfig;
import app.intelligent.resume.service.IFileStorageService;
import io.minio.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * MinIO文件存储服务实现类
 *
 * @author Intelligent Resume Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MinioFileStorageServiceImpl implements IFileStorageService {

    private final MinioClient minioClient;
    private final MinioConfig minioConfig;

    /**
     * 初始化：检查并创建存储桶
     */
    @PostConstruct
    public void init() {
        // 初始化默认存储桶
        initBucket(minioConfig.getBucketName());
        
        // 初始化头像存储桶
        if (minioConfig.getAvatarBucket() != null && !minioConfig.getAvatarBucket().equals(minioConfig.getBucketName())) {
            initBucket(minioConfig.getAvatarBucket());
        }
        
        // 初始化简历存储桶
        if (minioConfig.getResumeBucket() != null && !minioConfig.getResumeBucket().equals(minioConfig.getBucketName())) {
            initBucket(minioConfig.getResumeBucket());
        }
    }

    /**
     * 初始化存储桶
     */
    private void initBucket(String bucketName) {
        if (bucketName == null || bucketName.isEmpty()) {
            return;
        }
        
        try {
            boolean bucketExists = minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(bucketName)
                    .build());

            if (!bucketExists) {
                minioClient.makeBucket(MakeBucketArgs.builder()
                        .bucket(bucketName)
                        .build());
                log.info("创建MinIO存储桶成功: {}", bucketName);

                // 设置存储桶策略为公开读取
                String policy = """
                    {
                        "Version": "2012-10-17",
                        "Statement": [
                            {
                                "Effect": "Allow",
                                "Principal": {"AWS": ["*"]},
                                "Action": ["s3:GetObject"],
                                "Resource": ["arn:aws:s3:::%s/*"]
                            }
                        ]
                    }
                    """.formatted(bucketName);

                minioClient.setBucketPolicy(SetBucketPolicyArgs.builder()
                        .bucket(bucketName)
                        .config(policy)
                        .build());
                log.info("设置存储桶 {} 公开读取策略成功", bucketName);
            } else {
                log.info("存储桶 {} 已存在", bucketName);
            }
        } catch (Exception e) {
            log.error("初始化MinIO存储桶 {} 失败", bucketName, e);
        }
    }

    @Override
    public String uploadFile(MultipartFile file, String fileType) {
        return uploadFile(file, fileType, fileType);
    }

    @Override
    public String uploadFile(MultipartFile file, String directory, String fileType) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ResultCode.FILE_EMPTY);
        }

        try {
            // 根据文件类型获取存储桶
            String bucketName = minioConfig.getBucketByType(fileType);
            String urlPrefix = minioConfig.getUrlPrefixByType(fileType);

            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = getFileExtension(originalFilename);
            String objectName = generateObjectName(directory, extension);

            // 获取文件输入流
            InputStream inputStream = file.getInputStream();
            String contentType = file.getContentType();
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            // 上传到MinIO
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(inputStream, file.getSize(), -1)
                    .contentType(contentType)
                    .build());

            // 构建访问URL
            String fileUrl = urlPrefix + "/" + objectName;
            log.info("文件上传成功, bucket={}, objectName={}, fileUrl={}", bucketName, objectName, fileUrl);

            return fileUrl;
        } catch (Exception e) {
            log.error("文件上传失败", e);
            throw new BusinessException(ResultCode.FILE_UPLOAD_FAILED);
        }
    }

    @Override
    public boolean deleteFile(String fileUrl) {
        try {
            // 从URL提取对象名称和存储桶
            String objectName = extractObjectName(fileUrl);
            String bucketName = extractBucketName(fileUrl);
            
            if (objectName == null || bucketName == null) {
                return false;
            }

            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build());

            log.info("文件删除成功, bucket={}, objectName={}", bucketName, objectName);
            return true;
        } catch (Exception e) {
            log.error("文件删除失败, fileUrl={}", fileUrl, e);
            return false;
        }
    }

    @Override
    public String getPresignedUrl(String objectName, int expiry) {
        try {
            return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(minioConfig.getBucketName())
                    .object(objectName)
                    .expiry(expiry, TimeUnit.SECONDS)
                    .build());
        } catch (Exception e) {
            log.error("获取预签名URL失败, objectName={}", objectName, e);
            throw new BusinessException(ResultCode.FILE_URL_GENERATE_FAILED);
        }
    }

    /**
     * 获取预签名URL（指定存储桶）
     */
    public String getPresignedUrl(String bucketName, String objectName, int expiry) {
        try {
            return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(bucketName)
                    .object(objectName)
                    .expiry(expiry, TimeUnit.SECONDS)
                    .build());
        } catch (Exception e) {
            log.error("获取预签名URL失败, bucket={}, objectName={}", bucketName, objectName, e);
            throw new BusinessException(ResultCode.FILE_URL_GENERATE_FAILED);
        }
    }

    @Override
    public boolean fileExists(String objectName) {
        try {
            minioClient.statObject(StatObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(objectName)
                    .build());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 检查文件是否存在（指定存储桶）
     */
    public boolean fileExists(String bucketName, String objectName) {
        try {
            minioClient.statObject(StatObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 生成对象名称
     * 格式：目录/年/月/日/UUID.扩展名
     */
    private String generateObjectName(String directory, String extension) {
        LocalDate now = LocalDate.now();
        String datePath = now.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String uuid = UUID.randomUUID().toString().replace("-", "");
        return String.format("%s/%s/%s%s", directory, datePath, uuid, extension);
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".")).toLowerCase();
    }

    /**
     * 从URL提取对象名称
     */
    private String extractObjectName(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return null;
        }

        // 尝试从各种URL前缀中提取
        String[] prefixes = {
            minioConfig.getResumeUrlPrefix(),
            minioConfig.getAvatarUrlPrefix(),
            minioConfig.getUrlPrefix()
        };

        for (String prefix : prefixes) {
            if (prefix != null && fileUrl.startsWith(prefix)) {
                return fileUrl.substring(prefix.length() + 1);
            }
        }

        // 如果不是完整URL，假设直接是对象名称
        return fileUrl;
    }

    /**
     * 从URL提取存储桶名称
     */
    private String extractBucketName(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return null;
        }

        // 根据URL前缀判断存储桶
        if (minioConfig.getResumeUrlPrefix() != null && fileUrl.startsWith(minioConfig.getResumeUrlPrefix())) {
            return minioConfig.getResumeBucket();
        }
        if (minioConfig.getAvatarUrlPrefix() != null && fileUrl.startsWith(minioConfig.getAvatarUrlPrefix())) {
            return minioConfig.getAvatarBucket();
        }
        if (minioConfig.getUrlPrefix() != null && fileUrl.startsWith(minioConfig.getUrlPrefix())) {
            return minioConfig.getBucketName();
        }

        // 默认返回默认存储桶
        return minioConfig.getBucketName();
    }
}