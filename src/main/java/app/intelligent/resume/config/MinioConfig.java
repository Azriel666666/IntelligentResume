package app.intelligent.resume.config;

import io.minio.MinioClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MinIO配置类
 *
 * @author Intelligent Resume Team
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "minio")
public class MinioConfig {

    /**
     * MinIO服务端点
     */
    private String endpoint;

    /**
     * 访问密钥
     */
    private String accessKey;

    /**
     * 秘密密钥
     */
    private String secretKey;

    /**
     * 默认存储桶名称（兼容旧代码）
     */
    private String bucketName;

    /**
     * 默认文件访问URL前缀
     */
    private String urlPrefix;

    /**
     * 头像存储桶名称
     */
    private String avatarBucket;

    /**
     * 头像文件访问URL前缀
     */
    private String avatarUrlPrefix;

    /**
     * 简历存储桶名称
     */
    private String resumeBucket;

    /**
     * 简历文件访问URL前缀
     */
    private String resumeUrlPrefix;

    /**
     * 创建MinIO客户端
     */
    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }

    /**
     * 根据文件类型获取存储桶名称
     *
     * @param fileType 文件类型：avatar-头像, resume-简历
     * @return 存储桶名称
     */
    public String getBucketByType(String fileType) {
        if ("avatar".equalsIgnoreCase(fileType)) {
            return avatarBucket != null ? avatarBucket : bucketName;
        } else if ("resume".equalsIgnoreCase(fileType)) {
            return resumeBucket != null ? resumeBucket : bucketName;
        }
        return bucketName;
    }

    /**
     * 根据文件类型获取URL前缀
     *
     * @param fileType 文件类型：avatar-头像, resume-简历
     * @return URL前缀
     */
    public String getUrlPrefixByType(String fileType) {
        if ("avatar".equalsIgnoreCase(fileType)) {
            return avatarUrlPrefix != null ? avatarUrlPrefix : urlPrefix;
        } else if ("resume".equalsIgnoreCase(fileType)) {
            return resumeUrlPrefix != null ? resumeUrlPrefix : urlPrefix;
        }
        return urlPrefix;
    }
}