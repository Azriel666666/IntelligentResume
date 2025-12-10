package app.intelligent.resume.service.impl;

import app.intelligent.resume.common.exception.BusinessException;
import app.intelligent.resume.common.result.ResultCode;
import app.intelligent.resume.entity.User;
import app.intelligent.resume.repository.UserRepository;
import app.intelligent.resume.service.IUserService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户Service实现类
 *
 * @author Intelligent Resume Team
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserRepository, User> implements IUserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User getByUsername(String username) {
        return userRepository.selectByUsername(username);
    }

    @Override
    public User getByEmail(String email) {
        return userRepository.selectByEmail(email);
    }

    @Override
    public User getByPhone(String phone) {
        return userRepository.selectByPhone(phone);
    }

    @Override
    public List<User> listAllUsers() {
        return list();
    }

    @Override
    public Page<User> pageUsers(int page, int size) {
        return page(new Page<>(page, size));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public User createUser(User user) {
        // 检查用户名是否已存在
        if (getByUsername(user.getUsername()) != null) {
            throw new BusinessException(ResultCode.USER_EXIST);
        }

        // 检查邮箱是否已存在
        if (user.getEmail() != null && getByEmail(user.getEmail()) != null) {
            throw new BusinessException("邮箱已被使用");
        }

        // 加密密码
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // 设置默认值
        if (user.getStatus() == null) {
            user.setStatus(1); // 默认正常状态
        }

        save(user);
        return user;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public User updateUser(Long id, User user) {
        User existingUser = getById(id);
        if (existingUser == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }

        // 更新字段
        if (user.getNickname() != null) {
            existingUser.setNickname(user.getNickname());
        }
        if (user.getRealName() != null) {
            existingUser.setRealName(user.getRealName());
        }
        if (user.getEmail() != null) {
            existingUser.setEmail(user.getEmail());
        }
        if (user.getPhone() != null) {
            existingUser.setPhone(user.getPhone());
        }
        if (user.getAvatar() != null) {
            existingUser.setAvatar(user.getAvatar());
        }
        if (user.getCompanyName() != null) {
            existingUser.setCompanyName(user.getCompanyName());
        }
        if (user.getCompanyPosition() != null) {
            existingUser.setCompanyPosition(user.getCompanyPosition());
        }
        if (user.getGender() != null) {
            existingUser.setGender(user.getGender());
        }
        if (user.getBirthDate() != null) {
            existingUser.setBirthDate(user.getBirthDate());
        }

        updateById(existingUser);
        return existingUser;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteUser(Long id) {
        User user = getById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }
        return removeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateLastLoginTime(Long userId, String ip) {
        User user = getById(userId);
        if (user != null) {
            user.setLastLoginTime(LocalDateTime.now());
            user.setLastLoginIp(ip);
            updateById(user);
        }
    }
}
