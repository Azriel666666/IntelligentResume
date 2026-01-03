package app.intelligent.resume.service.impl;

import app.intelligent.resume.common.constant.RedisKeyConstants;
import app.intelligent.resume.common.enums.VerifyCodeType;
import app.intelligent.resume.common.exception.BusinessException;
import app.intelligent.resume.common.result.ResultCode;
import app.intelligent.resume.common.util.PasswordValidator;
import app.intelligent.resume.common.util.PhoneValidator;
import app.intelligent.resume.dto.request.LoginRequest;
import app.intelligent.resume.dto.request.PhoneRegisterRequest;
import app.intelligent.resume.dto.request.RegisterRequest;
import app.intelligent.resume.dto.request.ResetPasswordRequest;
import app.intelligent.resume.dto.response.LoginResponse;
import app.intelligent.resume.dto.response.UserResponse;
import app.intelligent.resume.entity.User;
import app.intelligent.resume.security.JwtTokenProvider;
import app.intelligent.resume.service.IAuthService;
import app.intelligent.resume.service.IUserService;
import app.intelligent.resume.service.IVerifyCodeService;
import cn.hutool.core.bean.BeanUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

/**
 * 认证Service实现类
 *
 * @author Intelligent Resume Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements IAuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final IUserService userService;
    private final PasswordEncoder passwordEncoder;
    private final IVerifyCodeService verifyCodeService;
    private final RedissonClient redissonClient;
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${jwt.expiration}")
    private Long expiration;

    @Override
    public LoginResponse login(LoginRequest request) {
        log.info("用户登录, phone={}", request.getPhone());

        // 1. 验证手机号格式
        PhoneValidator.validate(request.getPhone());

        // 2. 检查用户是否存在
        User user = userService.getByPhone(request.getPhone());
        if (user == null) {
            log.warn("登录失败，手机号未注册, phone={}", request.getPhone());
            throw new BusinessException(ResultCode.PHONE_NOT_REGISTERED);
        }

        // 3. 检查用户状态
        if (user.getStatus() == 0) {
            log.warn("登录失败，账号已被禁用, phone={}", request.getPhone());
            throw new BusinessException(ResultCode.ACCOUNT_DISABLED);
        }

        try {
            // 4. 认证
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getPhone(),
                            request.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 5. 生成Token
            String accessToken = tokenProvider.generateToken(authentication);
            String refreshToken = tokenProvider.generateRefreshToken(request.getPhone());

            // 6. 构建返回信息
            UserResponse userResponse = BeanUtil.copyProperties(user, UserResponse.class);

            log.info("用户登录成功, phone={}, userId={}", request.getPhone(), user.getId());

            return LoginResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(expiration)
                    .userInfo(userResponse)
                    .build();

        } catch (Exception e) {
            log.warn("登录失败，密码错误, phone={}", request.getPhone());
            throw new BusinessException(ResultCode.PASSWORD_ERROR);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginResponse register(RegisterRequest request) {
        // 检查用户名是否已存在
        if (userService.getByUsername(request.getUsername()) != null) {
            throw new BusinessException(ResultCode.USER_EXIST);
        }

        // 检查邮箱是否已存在
        if (userService.getByEmail(request.getEmail()) != null) {
            throw new BusinessException("邮箱已被使用");
        }

        // 检查手机号是否已注册
        if (request.getPhone() != null && userService.getByPhone(request.getPhone()) != null) {
            throw new BusinessException(ResultCode.PHONE_ALREADY_REGISTERED);
        }

        // 创建用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setUserType(request.getUserType());
        user.setCompanyName(request.getCompanyName());
        user.setStatus(1); // 默认正常状态

        userService.save(user);

        // 自动登录（使用手机号登录）
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setPhone(request.getPhone());
        loginRequest.setPassword(request.getPassword());

        return login(loginRequest);
    }

    @Override
    public LoginResponse refreshToken(String refreshToken) {
        // 验证刷新Token
        if (!tokenProvider.validateToken(refreshToken)) {
            throw new BusinessException(ResultCode.TOKEN_INVALID);
        }

        // 从Token中获取手机号（之前存储的是手机号）
        String phone = tokenProvider.getUsernameFromToken(refreshToken);

        // 生成新的访问Token
        String newAccessToken = tokenProvider.generateToken(phone);
        String newRefreshToken = tokenProvider.generateRefreshToken(phone);

        // 查询用户信息（通过手机号）
        User user = userService.getByPhone(phone);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }
        UserResponse userResponse = BeanUtil.copyProperties(user, UserResponse.class);

        return LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(expiration)
                .userInfo(userResponse)
                .build();
    }

    @Override
    public void logout() {
        SecurityContextHolder.clearContext();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginResponse registerByPhone(PhoneRegisterRequest request, String ip) {
        log.info("开始手机号注册流程, phone={}, ip={}", request.getPhone(), ip);

        // 1. 验证手机号格式
        PhoneValidator.validate(request.getPhone());

        // 2. 验证密码格式和一致性
        PasswordValidator.validate(request.getPassword());
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException(ResultCode.PASSWORD_NOT_MATCH);
        }

        // 3. 验证用户类型（不允许注册管理员）
        if (request.getUserType() == null || request.getUserType() == 1) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED);
        }

        // 4. 如果是HR，公司名称必填
        if (request.getUserType() == 2 && (request.getCompanyName() == null || request.getCompanyName().trim().isEmpty())) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED);
        }

        // 5. 检查验证码是否已验证
        boolean isVerified = verifyCodeService.isVerified(request.getPhone(), VerifyCodeType.REGISTER);
        if (!isVerified) {
            throw new BusinessException(ResultCode.VERIFY_CODE_NOT_VERIFIED);
        }

        // 6. 获取分布式锁
        String lockKey = RedisKeyConstants.buildRegisterLockKey(request.getPhone());
        RLock lock = redissonClient.getLock(lockKey);

        try {
            // 尝试获取锁，最多等待3秒，锁10秒后自动释放
            boolean isLocked = lock.tryLock(3, 10, TimeUnit.SECONDS);
            if (!isLocked) {
                log.warn("获取注册锁失败, phone={}", request.getPhone());
                throw new BusinessException(ResultCode.REGISTER_LOCK_FAILED);
            }

            // 7. 检查请求幂等性
            checkRequestIdempotency(request.getRequestId());

            // 8. 检查手机号是否已注册
            if (userService.getByPhone(request.getPhone()) != null) {
                log.warn("手机号已注册, phone={}", request.getPhone());
                throw new BusinessException(ResultCode.PHONE_ALREADY_REGISTERED);
            }

            // 9. 创建用户
            User user = new User();
            user.setPhone(request.getPhone());
            user.setUsername("系统昵称" + (int)(Math.random() * 900000 + 100000)); // 默认用户名：系统昵称+6位随机数
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setUserType(request.getUserType());
            user.setCompanyName(request.getCompanyName());
            user.setRegisterSource("WEB");
            user.setRegisterIp(ip);
            user.setIsPhoneVerified(1);
            user.setStatus(1); // 默认正常状态

            userService.save(user);
            log.info("用户注册成功, phone={}, userId={}", request.getPhone(), user.getId());

            // 10. 删除验证码
            verifyCodeService.deleteVerifyCode(request.getPhone(), VerifyCodeType.REGISTER);

            // 11. 标记请求ID已处理
            markRequestIdProcessed(request.getRequestId());

            // 12. 生成Token并返回（使用手机号，因为UserDetailsServiceImpl.loadUserByUsername期望手机号）
            String accessToken = tokenProvider.generateToken(user.getPhone());
            String refreshToken = tokenProvider.generateRefreshToken(user.getPhone());

            UserResponse userResponse = BeanUtil.copyProperties(user, UserResponse.class);

            return LoginResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(expiration)
                    .userInfo(userResponse)
                    .build();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("获取注册锁被中断, phone={}", request.getPhone(), e);
            throw new BusinessException(ResultCode.REGISTER_LOCK_FAILED);
        } finally {
            // 释放锁
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.info("释放注册锁, phone={}", request.getPhone());
            }
        }
    }

    /**
     * 检查请求幂等性
     */
    private void checkRequestIdempotency(String requestId) {
        if (requestId == null || requestId.trim().isEmpty()) {
            throw new BusinessException(ResultCode.REQUEST_ID_EMPTY);
        }

        String key = RedisKeyConstants.buildRequestIdKey(requestId);
        Boolean exists = redisTemplate.hasKey(key);

        if (Boolean.TRUE.equals(exists)) {
            log.warn("重复请求, requestId={}", requestId);
            throw new BusinessException(ResultCode.REQUEST_DUPLICATE);
        }
    }

    /**
     * 标记请求ID已处理
     */
    private void markRequestIdProcessed(String requestId) {
        String key = RedisKeyConstants.buildRequestIdKey(requestId);
        redisTemplate.opsForValue().set(key, true, 5, TimeUnit.MINUTES);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(ResetPasswordRequest request) {
        log.info("开始重置密码流程, phone={}", request.getPhone());

        // 1. 验证手机号格式
        PhoneValidator.validate(request.getPhone());

        // 2. 验证密码格式和一致性
        PasswordValidator.validate(request.getNewPassword());
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException(ResultCode.PASSWORD_NOT_MATCH);
        }

        // 3. 验证验证码（直接验证，不需要预先验证）
        verifyCodeService.verifyCode(request.getPhone(), request.getCode(), VerifyCodeType.RESET_PASSWORD);

        // 4. 获取分布式锁
        String lockKey = RedisKeyConstants.buildResetPasswordLockKey(request.getPhone());
        RLock lock = redissonClient.getLock(lockKey);

        try {
            // 尝试获取锁，最多等待3秒，锁10秒后自动释放
            boolean isLocked = lock.tryLock(3, 10, TimeUnit.SECONDS);
            if (!isLocked) {
                log.warn("获取重置密码锁失败, phone={}", request.getPhone());
                throw new BusinessException(ResultCode.RESET_PASSWORD_LOCK_FAILED);
            }

            // 5. 检查请求幂等性
            checkRequestIdempotency(request.getRequestId());

            // 6. 检查手机号是否已注册
            User user = userService.getByPhone(request.getPhone());
            if (user == null) {
                log.warn("手机号未注册, phone={}", request.getPhone());
                throw new BusinessException(ResultCode.PHONE_NOT_REGISTERED);
            }

            // 7. 检查账号状态
            if (user.getStatus() == 0) {
                log.warn("账号已被禁用, phone={}", request.getPhone());
                throw new BusinessException(ResultCode.ACCOUNT_DISABLED);
            }

            // 8. 更新密码
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
            userService.updateById(user);
            log.info("密码重置成功, phone={}, userId={}", request.getPhone(), user.getId());

            // 9. 删除验证码
            verifyCodeService.deleteVerifyCode(request.getPhone(), VerifyCodeType.RESET_PASSWORD);

            // 10. 标记请求ID已处理
            markRequestIdProcessed(request.getRequestId());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("获取重置密码锁被中断, phone={}", request.getPhone(), e);
            throw new BusinessException(ResultCode.RESET_PASSWORD_LOCK_FAILED);
        } finally {
            // 释放锁
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.info("释放重置密码锁, phone={}", request.getPhone());
            }
        }
    }
}
