package app.intelligent.resume.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * 文件存储服务接口
 *
 * @author Intelligent Resume Team
 */
public interface IFileStorageService {

    /**
     * 上传文件
     *
     * @param file     文件
     * @param fileType 文件类型（avatar-头像, resume-简历, other-其他）
     * @return 文件访问URL
     */
    String uploadFile(MultipartFile file, String fileType);

    /**
     * 上传文件到指定目录
     *
     * @param file      文件
     * @param directory 目录名
     * @param fileType  文件类型
     * @return 文件访问URL
     */
    String uploadFile(MultipartFile file, String directory, String fileType);

    /**
     * 删除文件
     *
     * @param fileUrl 文件URL或对象名称
     * @return 是否删除成功
     */
    boolean deleteFile(String fileUrl);

    /**
     * 获取文件访问URL（带签名，有过期时间）
     *
     * @param objectName 对象名称
     * @param expiry     过期时间（秒）
     * @return 带签名的访问URL
     */
    String getPresignedUrl(String objectName, int expiry);

    /**
     * 检查文件是否存在
     *
     * @param objectName 对象名称
     * @return 是否存在
     */
    boolean fileExists(String objectName);
}