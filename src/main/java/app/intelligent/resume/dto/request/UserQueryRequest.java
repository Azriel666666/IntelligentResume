package app.intelligent.resume.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户查询请求DTO
 *
 * @author Intelligent Resume Team
 */
@Data
@Schema(description = "用户查询请求")
public class UserQueryRequest {

    @Schema(description = "搜索关键词（手机号/用户名）")
    private String keyword;

    @Schema(description = "用户类型：1-管理员 2-HR 3-求职者")
    private Integer userType;

    @Schema(description = "状态：0-禁用 1-正常")
    private Integer status;

    @Schema(description = "页码", example = "1")
    private Integer page = 1;

    @Schema(description = "每页数量", example = "10")
    private Integer size = 10;
}