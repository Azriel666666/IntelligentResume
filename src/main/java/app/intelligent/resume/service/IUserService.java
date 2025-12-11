package app.intelligent.resume.service;

import app.intelligent.resume.dto.request.PasswordUpdateRequest;
import app.intelligent.resume.dto.request.PhoneUpdateRequest;
import app.intelligent.resume.dto.request.UserQueryRequest;
import app.intelligent.resume.entity.User;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 用户Service接口
 *
 * @author Intelligent Resume Team
 */
public interface IUserService extends IService<User> {

    /**
     * 根据用户名查询用户
     */
    User getByUsername(String username);

    /**
     * 根据邮箱查询用户
     */
    User getByEmail(String email);

    /**
     * 根据手机号查询用户
     */
    User getByPhone(String phone);

    /**
     * 获取当前登录用户信息
     */
    User getCurrentUser();

    /**
     * 更新当前用户个人信息
     */
    User updateCurrentUser(User user);

    /**
     * 修改当前用户密码
     */
    void updatePassword(PasswordUpdateRequest request);

    /**
     * 修改当前用户手机号
     */
    void updatePhone(PhoneUpdateRequest request);

    /**
     * 更新用户头像
     */
    String updateAvatar(Long userId, String avatarUrl);

    /**
     * 更新用户状态（禁用/启用）
     */
    void updateUserStatus(Long userId, Integer status);

    /**
     * 查询所有用户列表
     */
    List<User> listAllUsers();

    /**
     * 分页查询用户
     */
    Page<User> pageUsers(int page, int size);

    /**
     * 分页查询用户（带条件）
     */
    Page<User> pageUsers(UserQueryRequest request);

    /**
     * 创建用户
     */
    User createUser(User user);

    /**
     * 更新用户
     */
    User updateUser(Long id, User user);

    /**
     * 删除用户（逻辑删除）
     */
    boolean deleteUser(Long id);

    /**
     * 更新最后登录时间
     */
    void updateLastLoginTime(Long userId, String ip);
}