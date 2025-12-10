package app.intelligent.resume.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户实体类
 *
 * @author Intelligent Resume Team
 */
@Data
@TableName("sys_user")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码（不返回给前端）
     */
    @JsonIgnore
    private String password;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 注册来源：WEB-网页 ANDROID-安卓 IOS-苹果
     */
    private String registerSource;

    /**
     * 注册IP
     */
    private String registerIp;

    /**
     * 手机号是否已验证：0-未验证 1-已验证
     */
    private Integer isPhoneVerified;

    /**
     * 头像URL
     */
    private String avatar;

    /**
     * 用户类型：1-管理员 2-HR 3-求职者
     */
    private Integer userType;

    /**
     * 状态：0-禁用 1-正常 2-待审核
     */
    private Integer status;

    /**
     * HR所属公司名称
     */
    private String companyName;

    /**
     * HR职位
     */
    private String companyPosition;

    /**
     * 性别：0-女 1-男 2-保密
     */
    private Integer gender;

    /**
     * 出生日期
     */
    private String birthDate;

    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginTime;

    /**
     * 最后登录IP
     */
    private String lastLoginIp;

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
     * 逻辑删除：0-未删除 1-已删除
     */
    @TableLogic
    private Integer deleted;
}
