package app.intelligent.resume.service;

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
     * 查询所有用户列表
     */
    List<User> listAllUsers();

    /**
     * 分页查询用户
     */
    Page<User> pageUsers(int page, int size);

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
