package app.intelligent.resume.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 验证码清理定时任务
 *
 * @author Intelligent Resume Team
 */
@Slf4j
@Component
@EnableScheduling
public class VerifyCodeCleanupTask {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 清理过期的验证码相关数据
     * 每天凌晨2点执行
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupExpiredVerifyCodes() {
        log.info("开始执行验证码清理任务...");

        try {
            // 清理过期的每日计数
            cleanupDailyCountKeys();

            // 清理过期的IP计数
            cleanupIpCountKeys();

            // 清理过期的请求幂等性Key
            cleanupRequestIdempotentKeys();

            log.info("验证码清理任务执行完成");
        } catch (Exception e) {
            log.error("验证码清理任务执行失败", e);
        }
    }

    /**
     * 清理每日发送计数Key
     */
    private void cleanupDailyCountKeys() {
        String pattern = "verify_code_count:*";
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            long deleted = redisTemplate.delete(keys);
            log.info("清理每日计数Key: {} 个", deleted);
        }
    }

    /**
     * 清理IP发送计数Key
     */
    private void cleanupIpCountKeys() {
        String pattern = "verify_code_ip:*";
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            long deleted = redisTemplate.delete(keys);
            log.info("清理IP计数Key: {} 个", deleted);
        }
    }

    /**
     * 清理请求幂等性Key
     */
    private void cleanupRequestIdempotentKeys() {
        String pattern = "request_idempotent:*";
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            long deleted = redisTemplate.delete(keys);
            log.info("清理请求幂等性Key: {} 个", deleted);
        }
    }
}