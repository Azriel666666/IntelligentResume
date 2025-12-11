package app.intelligent.resume.service.impl;

import app.intelligent.resume.common.exception.BusinessException;
import app.intelligent.resume.common.result.ResultCode;
import app.intelligent.resume.common.util.PasswordValidator;
import app.intelligent.resume.common.util.PhoneValidator;
import app.intelligent.resume.dto.request.PasswordUpdateRequest;
import app.intelligent.resume.dto.request.PhoneUpdateRequest;
import app.intelligent.resume.dto.request.UserQueryRequest;
import app.intelligent.resume.entity.User;
import app.intelligent.resume.repository.UserRepository;
import app.intelligent.resume.security.SecurityUtils;
import app.intelligent.resume.service.IUserService;
import app.intelligent.resume.service.IVerifyCodeService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户Service实现类
 *
 * @author Intelligent Resume Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserRepository, User> implements IUserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final IVerifyCodeService verifyCodeService;

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
    public User getCurrentUser() {
        String phone = SecurityUtils.getCurrentPhone();
        if (phone == null) {
            throw new BusinessException(ResultCode.USER_NOT_LOGIN);
        }
        User user = getByPhone(phone);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }
        return user;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public User updateCurrentUser(User updateData) {
        User currentUser = getCurrentUser();

        // 更新允许修改的字段
        if (updateData.getNickname() != null) {
            currentUser.setNickname(updateData.getNickname());
        }
        if (updateData.getRealName() != null) {
            currentUser.setRealName(updateData.getRealName());
        }
        if (updateData.getEmail() != null) {
            // 检查邮箱是否被其他用户使用
            User emailUser = getByEmail(updateData.getEmail());
            if (emailUser != null && !emailUser.getId().equals(currentUser.getId())) {
                throw new BusinessException("该邮箱已被其他用户使用");
            }
            currentUser.setEmail(updateData.getEmail());
        }
        if (updateData.getGender() != null) {
            currentUser.setGender(updateData.getGender());
        }
        if (updateData.getBirthDate() != null) {
            currentUser.setBirthDate(updateData.getBirthDate());
        }
        // HR专属字段
        if (currentUser.getUserType() == 2) {
            if (updateData.getCompanyName() != null) {
                currentUser.setCompanyName(updateData.getCompanyName());
            }
            if (updateData.getCompanyPosition() != null) {
                currentUser.setCompanyPosition(updateData.getCompanyPosition());
            }
        }

        updateById(currentUser);
        log.info("用户更新个人信息成功, userId={}", currentUser.getId());
        return currentUser;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePassword(PasswordUpdateRequest request) {
        User currentUser = getCurrentUser();

        // 验证原密码
        if (!passwordEncoder.matches(request.getOldPassword(), currentUser.getPassword())) {
            throw new BusinessException(ResultCode.OLD_PASSWORD_ERROR);
        }

        // 验证新密码格式
        PasswordValidator.validate(request.getNewPassword());

        // 验证两次密码一致
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException(ResultCode.PASSWORD_NOT_MATCH);
        }

        // 新密码不能与原密码相同
        if (passwordEncoder.matches(request.getNewPassword(), currentUser.getPassword())) {
            throw new BusinessException(ResultCode.NEW_PASSWORD_SAME_AS_OLD);
        }

        // 更新密码
        currentUser.setPassword(passwordEncoder.encode(request.getNewPassword()));
        updateById(currentUser);
        log.info("用户修改密码成功, userId={}", currentUser.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePhone(PhoneUpdateRequest request) {
        User currentUser = getCurrentUser();

        // 验证新手机号格式
        PhoneValidator.validate(request.getNewPhone());

        // 验证新手机号是否与当前手机号相同
        if (request.getNewPhone().equals(currentUser.getPhone())) {
            throw new BusinessException("新手机号不能与当前手机号相同");
        }

        // 检查新手机号是否已被其他用户使用
        User phoneUser = getByPhone(request.getNewPhone());
        if (phoneUser != null) {
            throw new BusinessException(ResultCode.PHONE_ALREADY_USED);
        }

        // 验证验证码
        boolean verified = verifyCodeService.isCodeVerified(request.getNewPhone(), "BIND_PHONE");
        if (!verified) {
            throw new BusinessException(ResultCode.VERIFY_CODE_NOT_VERIFIED);
        }

        // 更新手机号
        String oldPhone = currentUser.getPhone();
        currentUser.setPhone(request.getNewPhone());
        currentUser.setIsPhoneVerified(1);
        updateById(currentUser);

        // 清除验证状态
        verifyCodeService.clearVerifiedStatus(request.getNewPhone(), "BIND_PHONE");

        log.info("用户修改手机号成功, userId={}, oldPhone={}, newPhone={}",
                currentUser.getId(), oldPhone, request.getNewPhone());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String updateAvatar(Long userId, String avatarUrl) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }

        user.setAvatar(avatarUrl);
        updateById(user);
        log.info("用户更新头像成功, userId={}", userId);
        return avatarUrl;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserStatus(Long userId, Integer status) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }

        // 验证状态值有效性
        if (status != 0 && status != 1) {
            throw new BusinessException(ResultCode.USER_STATUS_INVALID);
        }

        // 不能禁用自己
        User currentUser = getCurrentUser();
        if (currentUser.getId().equals(userId)) {
            throw new BusinessException("不能禁用自己的账号");
        }

        user.setStatus(status);
        updateById(user);
        log.info("更新用户状态成功, userId={}, status={}", userId, status);
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
    public Page<User> pageUsers(UserQueryRequest request) {
        Page<User> pageParam = new Page<>(request.getPage(), request.getSize());
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();

        // 关键词搜索（手机号或用户名）
        if (StringUtils.hasText(request.getKeyword())) {
            queryWrapper.and(w -> w
                    .like(User::getPhone, request.getKeyword())
                    .or()
                    .like(User::getUsername, request.getKeyword())
                    .or()
                    .like(User::getNickname, request.getKeyword())
            );
        }

        // 用户类型筛选
        if (request.getUserType() != null) {
            queryWrapper.eq(User::getUserType, request.getUserType());
        }

        // 状态筛选
        if (request.getStatus() != null) {
            queryWrapper.eq(User::getStatus, request.getStatus());
        }

        // 按创建时间倒序
        queryWrapper.orderByDesc(User::getCreateTime);

        return page(pageParam, queryWrapper);
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

        // 检查手机号是否已存在
        if (user.getPhone() != null && getByPhone(user.getPhone()) != null) {
            throw new BusinessException(ResultCode.PHONE_ALREADY_REGISTERED);
        }

        // 加密密码
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // 设置默认值
        if (user.getStatus() == null) {
            user.setStatus(1); // 默认正常状态
        }

        save(user);
        log.info("创建用户成功, userId={}, username={}", user.getId(), user.getUsername());
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
        if (user.getStatus() != null) {
            existingUser.setStatus(user.getStatus());
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

        // 不能删除自己
        User currentUser = getCurrentUser();
        if (currentUser.getId().equals(id)) {
            throw new BusinessException("不能删除自己的账号");
        }

        log.info("删除用户, userId={}, username={}", id, user.getUsername());
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