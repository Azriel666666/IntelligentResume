package app.intelligent.resume.common.util;

import java.security.SecureRandom;
import java.util.Random;

/**
 * 验证码生成工具类
 *
 * @author Intelligent Resume Team
 */
public class VerifyCodeGenerator {

    private static final Random RANDOM = new SecureRandom();

    /**
     * 验证码长度
     */
    private static final int CODE_LENGTH = 6;

    /**
     * 最大重试次数
     */
    private static final int MAX_RETRY = 10;

    /**
     * 生成6位数字验证码
     * 不包含连续数字和重复数字
     *
     * @return 验证码
     */
    public static String generate() {
        for (int retry = 0; retry < MAX_RETRY; retry++) {
            String code = generateRandomCode();
            if (isValidCode(code)) {
                return code;
            }
        }

        // 如果重试多次仍然失败，返回一个简单的随机码
        return String.format("%06d", RANDOM.nextInt(1000000));
    }

    /**
     * 生成随机6位数字
     *
     * @return 6位数字字符串
     */
    private static String generateRandomCode() {
        int code = RANDOM.nextInt(900000) + 100000; // 100000 到 999999
        return String.valueOf(code);
    }

    /**
     * 验证验证码是否有效
     * 不能包含连续数字（如123456）
     * 不能包含过多重复数字（如111111）
     *
     * @param code 验证码
     * @return 是否有效
     */
    private static boolean isValidCode(String code) {
        if (code == null || code.length() != CODE_LENGTH) {
            return false;
        }

        // 检查连续数字
        if (hasContinuousDigits(code)) {
            return false;
        }

        // 检查重复数字
        if (hasTooManyRepeatedDigits(code)) {
            return false;
        }

        return true;
    }

    /**
     * 检查是否包含连续数字
     * 例如：123456、987654
     *
     * @param code 验证码
     * @return 是否包含连续数字
     */
    private static boolean hasContinuousDigits(String code) {
        int continuousCount = 1;
        char[] chars = code.toCharArray();

        for (int i = 1; i < chars.length; i++) {
            int diff = chars[i] - chars[i - 1];
            // 检查是否连续（差值为1或-1）
            if (Math.abs(diff) == 1) {
                continuousCount++;
                if (continuousCount >= 4) { // 允许3个连续，不允许4个及以上
                    return true;
                }
            } else {
                continuousCount = 1;
            }
        }

        return false;
    }

    /**
     * 检查是否包含过多重复数字
     * 例如：111111、222222
     * 允许连续重复2次，不允许3次及以上
     *
     * @param code 验证码
     * @return 是否包含过多重复数字
     */
    private static boolean hasTooManyRepeatedDigits(String code) {
        int repeatCount = 1;
        char[] chars = code.toCharArray();

        for (int i = 1; i < chars.length; i++) {
            if (chars[i] == chars[i - 1]) {
                repeatCount++;
                if (repeatCount >= 3) { // 允许重复2次，不允许3次及以上
                    return true;
                }
            } else {
                repeatCount = 1;
            }
        }

        return false;
    }

    /**
     * 验证用户输入的验证码
     *
     * @param input  用户输入
     * @param actual 实际验证码
     * @return 是否匹配
     */
    public static boolean verify(String input, String actual) {
        if (input == null || actual == null) {
            return false;
        }
        return input.trim().equals(actual);
    }
}
