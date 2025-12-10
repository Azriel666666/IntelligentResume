package app.intelligent.resume.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建用户请求DTO
 *
 * @author Intelligent Resume Team
 */
@Data
@Schema(description = "创建用户请求")
public class UserCreateRequest {

    @Schema(description = "用户名")
    @NotBlank(message = "用户名不能为空")
    private String username;

    @Schema(description = "密码")
    @NotBlank(message = "密码不能为空")
    private String password;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "邮箱")
    @Email(message = "邮箱格式不正确")
    private String email;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "用户类型：1-管理员 2-HR 3-求职者")
    @NotNull(message = "用户类型不能为空")
    private Integer userType;

    @Schema(description = "公司名称")
    private String companyName;

    @Schema(description = "公司职位")
    private String companyPosition;
}
