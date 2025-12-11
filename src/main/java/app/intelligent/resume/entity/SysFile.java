package app.intelligent.resume.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统文件实体类
 *
 * @author Intelligent Resume Team
 */
@Data
@TableName("sys_file")
public class SysFile implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 文件ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 上传用户ID
     */
    private Long userId;

    /**
     * 原始文件名
     */
    private String fileName;

    /**
     * 文件存储路径
     */
    private String filePath;

    /**
     * 文件访问URL
     */
    private String fileUrl;

    /**
     * 文件类型：resume-简历 avatar-头像 other-其他
     */
    private String fileType;

    /**
     * 文件扩展名
     */
    private String fileExt;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * MIME类型
     */
    private String mimeType;

    /**
     * 存储类型：LOCAL-本地 OSS-阿里云 MINIO-MinIO
     */
    private String storageType;

    /**
     * 上传时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 逻辑删除
     */
    @TableLogic
    private Integer deleted;
}