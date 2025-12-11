package app.intelligent.resume.service.impl;

import app.intelligent.resume.common.constant.RedisKeyConstants;
import app.intelligent.resume.common.enums.VerifyCodeType;
import app.intelligent.resume.common.exception.BusinessException;
import app.intelligent.resume.common.result.ResultCode;
import app.intelligent.resume.common.util.PhoneValidator;
import app.intelligent.resume.common.util.VerifyCodeGenerator;
import app.intelligent.resume.service.ISmsService;
import app.intelligent.resume.service.IVerifyCodeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 验证码服务实现类
 *
 * @author Intelligent Resume Team
 */
@Slf4j
@Service
public class VerifyCodeServiceImpl implements IVerifyCodeService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ISmsService smsService;

    /**
     * 验证码有效期（分钟）
     */
    private static final long CODE_EXPIRE_MINUTES = 10;

    /**
     * 发送间隔（秒）
     */
    private static final long SEND_INTERVAL_SECONDS = 60;

    /**
     * 每日发送次数限制（每个手机号）
     */
    // todo 开发阶段将每日限制次数增加，最后调整为每日每个手机号限制10次
    private static final int DAILY_LIMIT_PER_PHONE = 9999;

    /**
     * 每日发送次数限制（每个IP）
     */
    private static final int DAILY_LIMIT_PER_IP = 20;

    /**
     * 最大重试次数
     */
    private static final int MAX_RETRY_COUNT = 3;

    @Override
    public boolean sendVerifyCode(String phone, VerifyCodeType type, String requestId, String ip) {
        log.info("开始发送验证码, phone={}, type={}, requestId={}, ip={}", phone, type.getCode(), requestId, ip);

        // 1. 验证手机号格式
        PhoneValidator.validate(phone);

        // 2. 检查请求幂等性
        checkRequestIdempotency(requestId);

        // 3. 检查发送间隔（60秒）
        checkSendInterval(phone, type);

        // 4. 检查每日发送次数（手机号维度）
        checkDailyLimitByPhone(phone, type);

        // 5. 检查每日发送次数（IP维度）
        checkDailyLimitByIp(ip, type);

        // 6. 生成验证码
        String code = VerifyCodeGenerator.generate();
        log.info("生成验证码成功, phone={}, code={}", phone, code);

        // 7. 发送短信
        boolean sendSuccess = smsService.sendSms(phone, code, type);
        if (!sendSuccess) {
            log.error("短信发送失败, phone={}, code={}", phone, code);
            throw new BusinessException(ResultCode.SMS_SEND_FAILED);
        }

        // 8. 保存验证码到Redis（Hash结构）
        saveVerifyCodeToRedis(phone, code, type);

        // 9. 增加每日发送计数
        incrementDailyCount(phone, ip, type);

        // 10. 标记请求ID已处理
        markRequestIdProcessed(requestId);

        log.info("验证码发送成功, phone={}", phone);
        return true;
    }

    @Override
    public boolean verifyCode(String phone, String code, VerifyCodeType type) {
        log.info("开始验证验证码, phone={}, inputCode={}, type={}", phone, code, type.getCode());

        // 1. 参数校验
        if (code == null || code.trim().isEmpty()) {
            throw new BusinessException(ResultCode.VERIFY_CODE_EMPTY);
        }

        // 2. 从Redis获取验证码信息
        String redisKey = RedisKeyConstants.buildVerifyCodeKey(phone, type);
        Map<Object, Object> codeInfo = redisTemplate.opsForHash().entries(redisKey);

        if (codeInfo.isEmpty()) {
            log.warn("验证码不存在或已过期, phone={}, type={}", phone, type.getCode());
            throw new BusinessException(ResultCode.VERIFY_CODE_NOT_EXIST);
        }

        // 3. 检查重试次数
        Integer retryCount = (Integer) codeInfo.get("retryCount");
        if (retryCount != null && retryCount >= MAX_RETRY_COUNT) {
            log.warn("验证码重试次数超限, phone={}, retryCount={}", phone, retryCount);
            // 删除验证码
            redisTemplate.delete(redisKey);
            throw new BusinessException(ResultCode.VERIFY_CODE_RETRY_EXCEEDED);
        }

        // 4. 验证码比对
        String actualCode = (String) codeInfo.get("code");
        boolean isMatch = VerifyCodeGenerator.verify(code, actualCode);

        if (!isMatch) {
            // 验证失败，增加重试次数
            incrementRetryCount(phone, type);
            log.warn("验证码错误, phone={}, inputCode={}, actualCode={}", phone, code, actualCode);
            throw new BusinessException(ResultCode.VERIFY_CODE_ERROR);
        }

        // 5. 验证成功，标记为已验证
        redisTemplate.opsForHash().put(redisKey, "verified", true);
        log.info("验证码验证成功, phone={}", phone);

        return true;
    }

    @Override
    public boolean isVerified(String phone, VerifyCodeType type) {
        String redisKey = RedisKeyConstants.buildVerifyCodeKey(phone, type);
        Boolean verified = (Boolean) redisTemplate.opsForHash().get(redisKey, "verified");
        return verified != null && verified;
    }

    @Override
    public void deleteVerifyCode(String phone, VerifyCodeType type) {
        String redisKey = RedisKeyConstants.buildVerifyCodeKey(phone, type);
        redisTemplate.delete(redisKey);
        log.info("删除验证码, phone={}, type={}", phone, type.getCode());
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
     * 检查发送间隔
     */
    private void checkSendInterval(String phone, VerifyCodeType type) {
        String redisKey = RedisKeyConstants.buildVerifyCodeKey(phone, type);
        Object sendTimeObj = redisTemplate.opsForHash().get(redisKey, "sendTime");

        if (sendTimeObj != null) {
            String sendTimeStr = (String) sendTimeObj;
            LocalDateTime lastSendTime = LocalDateTime.parse(sendTimeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            LocalDateTime now = LocalDateTime.now();

            long secondsSinceLastSend = java.time.Duration.between(lastSendTime, now).getSeconds();

            if (secondsSinceLastSend < SEND_INTERVAL_SECONDS) {
                log.warn("发送验证码过于频繁, phone={}, secondsSinceLastSend={}", phone, secondsSinceLastSend);
                throw new BusinessException(ResultCode.SEND_CODE_TOO_FREQUENT);
            }
        }
    }

    /**
     * 检查每日发送次数限制（手机号维度）
     */
    private void checkDailyLimitByPhone(String phone, VerifyCodeType type) {
        String countKey = RedisKeyConstants.buildDailyCountKey(phone, type);
        Integer count = (Integer) redisTemplate.opsForValue().get(countKey);
        if (count != null && count >= DAILY_LIMIT_PER_PHONE) {
            log.warn("手机号每日发送次数超限, phone={}, count={}", phone, count);
            throw new BusinessException(ResultCode.DAILY_SEND_LIMIT_EXCEEDED);
        }
    }

    /**
     * 检查每日发送次数限制（IP维度）
     */
    private void checkDailyLimitByIp(String ip, VerifyCodeType type) {
        String countKey = RedisKeyConstants.buildIpDailyCountKey(ip, type);
        Integer count = (Integer) redisTemplate.opsForValue().get(countKey);

        if (count != null && count >= DAILY_LIMIT_PER_IP) {
            log.warn("IP每日发送次数超限, ip={}, count={}", ip, count);
            throw new BusinessException(ResultCode.IP_SEND_LIMIT_EXCEEDED);
        }
    }

    /**
     * 保存验证码到Redis（Hash结构）
     */
    private void saveVerifyCodeToRedis(String phone, String code, VerifyCodeType type) {
        String redisKey = RedisKeyConstants.buildVerifyCodeKey(phone, type);

        Map<String, Object> codeInfo = new HashMap<>();
        codeInfo.put("code", code);
        codeInfo.put("sendTime", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        codeInfo.put("retryCount", 0);
        codeInfo.put("verified", false);

        redisTemplate.opsForHash().putAll(redisKey, codeInfo);
        redisTemplate.expire(redisKey, CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);
    }

    /**
     * 增加每日发送计数
     */
    private void incrementDailyCount(String phone, String ip, VerifyCodeType type) {
        // 手机号维度计数
        String phoneCountKey = RedisKeyConstants.buildDailyCountKey(phone, type);
        redisTemplate.opsForValue().increment(phoneCountKey);
        // 设置过期时间为当天结束
        redisTemplate.expireAt(phoneCountKey, getEndOfDay());

        // IP维度计数
        String ipCountKey = RedisKeyConstants.buildIpDailyCountKey(ip, type);
        redisTemplate.opsForValue().increment(ipCountKey);
        redisTemplate.expireAt(ipCountKey, getEndOfDay());
    }

    /**
     * 增加重试次数
     */
    private void incrementRetryCount(String phone, VerifyCodeType type) {
        String redisKey = RedisKeyConstants.buildVerifyCodeKey(phone, type);
        redisTemplate.opsForHash().increment(redisKey, "retryCount", 1);
    }

    /**
     * 标记请求ID已处理
     */
    private void markRequestIdProcessed(String requestId) {
        String key = RedisKeyConstants.buildRequestIdKey(requestId);
        redisTemplate.opsForValue().set(key, true, 5, TimeUnit.MINUTES);
    }

    /**
     * 获取当天结束时间
     */
    private java.util.Date getEndOfDay() {
        LocalDateTime endOfDay = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);
        return java.sql.Timestamp.valueOf(endOfDay);
    }

    @Override
    public boolean isCodeVerified(String phone, String typeCode) {
        VerifyCodeType type = VerifyCodeType.getByCode(typeCode);
        if (type == null) {
            return false;
        }
        return isVerified(phone, type);
    }

    @Override
    public void clearVerifiedStatus(String phone, String typeCode) {
        VerifyCodeType type = VerifyCodeType.getByCode(typeCode);
        if (type != null) {
            deleteVerifyCode(phone, type);
        }
    }
}
