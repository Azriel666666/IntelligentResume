package app.intelligent.resume.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 简历实体类
 *
 * @author Intelligent Resume Team
 */
@Data
@TableName("resume")
public class Resume implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 简历ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 简历标题
     */
    private String title;

    /**
     * 简历文件URL
     */
    private String fileUrl;

    /**
     * 原始文件名
     */
    private String fileName;

    /**
     * 文件类型：pdf/word/image
     */
    private String fileType;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * 解析状态：0-未解析 1-解析中 2-解析成功 3-解析失败
     */
    private Integer parseStatus;

    /**
     * 解析错误信息
     */
    private String parseError;

    /**
     * 综合评分（0-100）
     */
    private BigDecimal totalScore;

    /**
     * 完整度评分
     */
    private BigDecimal completenessScore;

    /**
     * 质量评分
     */
    private BigDecimal qualityScore;

    /**
     * 格式评分
     */
    private BigDecimal formatScore;

    /**
     * 是否默认简历：0-否 1-是
     */
    private Integer isDefault;

    /**
     * 状态：0-隐藏 1-正常 2-草稿
     */
    private Integer status;

    /**
     * 被查看次数
     */
    private Integer viewCount;

    /**
     * 被下载次数
     */
    private Integer downloadCount;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 逻辑删除
     */
    @TableLogic
    private Integer deleted;
}
