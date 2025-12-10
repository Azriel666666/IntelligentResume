package app.intelligent.resume.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import lombok.Data;

/**
 * 更新用户请求DTO
 *
 * @author Intelligent Resume Team
 */
@Data
@Schema(description = "更新用户请求")
public class UserUpdateRequest {

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "邮箱")
    @Email(message = "邮箱格式不正确")
    private String email;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "头像URL")
    private String avatar;

    @Schema(description = "公司名称")
    private String companyName;

    @Schema(description = "公司职位")
    private String companyPosition;

    @Schema(description = "性别：0-女 1-男 2-保密")
    private Integer gender;

    @Schema(description = "出生日期")
    private String birthDate;
}
