package app.intelligent.resume.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户响应DTO
 *
 * @author Intelligent Resume Team
 */
@Data
@Schema(description = "用户响应")
public class UserResponse {

    @Schema(description = "用户ID")
    private Long id;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "头像URL")
    private String avatar;

    @Schema(description = "用户类型：1-管理员 2-HR 3-求职者")
    private Integer userType;

    @Schema(description = "状态：0-禁用 1-正常 2-待审核")
    private Integer status;

    @Schema(description = "公司名称")
    private String companyName;

    @Schema(description = "公司职位")
    private String companyPosition;

    @Schema(description = "性别：0-女 1-男 2-保密")
    private Integer gender;

    @Schema(description = "出生日期")
    private LocalDate birthDate;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
