package app.intelligent.resume.common.util;

import app.intelligent.resume.common.exception.BusinessException;
import app.intelligent.resume.common.result.ResultCode;

import java.util.regex.Pattern;

/**
 * 密码验证工具类
 *
 * @author Intelligent Resume Team
 */
public class PasswordValidator {

    /**
     * 密码最小长度
     */
    private static final int MIN_LENGTH = 8;

    /**
     * 密码最大长度
     */
    private static final int MAX_LENGTH = 16;

    /**
     * 密码正则：8-16位，必须包含数字和字母
     */
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[0-9])(?=.*[a-zA-Z])[0-9a-zA-Z]{8,16}$");

    /**
     * 纯数字正则
     */
    private static final Pattern PURE_NUMBER_PATTERN = Pattern.compile("^\\d+$");

    /**
     * 纯字母正则
     */
    private static final Pattern PURE_LETTER_PATTERN = Pattern.compile("^[a-zA-Z]+$");

    /**
     * 验证密码格式
     *
     * @param password 密码
     * @return 是否有效
     */
    public static boolean isValid(String password) {
        if (password == null || password.trim().isEmpty()) {
            return false;
        }

        String trimmedPassword = password.trim();

        // 长度检查
        if (trimmedPassword.length() < MIN_LENGTH || trimmedPassword.length() > MAX_LENGTH) {
            return false;
        }

        // 必须包含数字和字母
        if (!PASSWORD_PATTERN.matcher(trimmedPassword).matches()) {
            return false;
        }

        // 禁止纯数字
        if (PURE_NUMBER_PATTERN.matcher(trimmedPassword).matches()) {
            return false;
        }

        // 禁止纯字母
        if (PURE_LETTER_PATTERN.matcher(trimmedPassword).matches()) {
            return false;
        }

        // 禁止连续字符（如：123456, abcdef）
        if (hasContinuousChars(trimmedPassword)) {
            return false;
        }

        // 禁止过多重复字符（如：111111, aaaaaa）
        if (hasTooManyRepeatedChars(trimmedPassword)) {
            return false;
        }

        return true;
    }

    /**
     * 验证密码格式，不通过则抛出异常
     *
     * @param password 密码
     * @throws BusinessException 格式错误时抛出
     */
    public static void validate(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new BusinessException(ResultCode.PASSWORD_FORMAT_ERROR);
        }

        String trimmedPassword = password.trim();

        // 长度检查
        if (trimmedPassword.length() < MIN_LENGTH || trimmedPassword.length() > MAX_LENGTH) {
            throw new BusinessException(ResultCode.PASSWORD_FORMAT_ERROR);
        }

        // 必须包含数字和字母
        if (!PASSWORD_PATTERN.matcher(trimmedPassword).matches()) {
            throw new BusinessException(ResultCode.PASSWORD_FORMAT_ERROR);
        }

        // 禁止纯数字
        if (PURE_NUMBER_PATTERN.matcher(trimmedPassword).matches()) {
            throw new BusinessException(ResultCode.PASSWORD_FORMAT_ERROR);
        }

        // 禁止纯字母
        if (PURE_LETTER_PATTERN.matcher(trimmedPassword).matches()) {
            throw new BusinessException(ResultCode.PASSWORD_FORMAT_ERROR);
        }

//        // 禁止连续字符
//        if (hasContinuousChars(trimmedPassword)) {
//            throw new BusinessException(ResultCode.PASSWORD_CONTINUOUS_CHAR);
//        }
//
//        // 禁止过多重复字符
//        if (hasTooManyRepeatedChars(trimmedPassword)) {
//            throw new BusinessException(ResultCode.PASSWORD_REPEATED_CHAR);
//        }
    }

    /**
     * 检查是否包含连续字符
     * 例如：123456、abcdef、987654
     *
     * @param password 密码
     * @return 是否包含连续字符
     */
    private static boolean hasContinuousChars(String password) {
        int continuousCount = 1;
        char[] chars = password.toCharArray();

        for (int i = 1; i < chars.length; i++) {
            // 检查是否连续（ASCII值相差1）
            if (Math.abs(chars[i] - chars[i - 1]) == 1) {
                continuousCount++;
                if (continuousCount >= 3) {
                    return true;
                }
            } else {
                continuousCount = 1;
            }
        }

        return false;
    }

    /**
     * 检查是否包含过多重复字符
     * 例如：111111、aaaaaa
     * 允许连续重复2次，不允许3次及以上
     *
     * @param password 密码
     * @return 是否包含过多重复字符
     */
    private static boolean hasTooManyRepeatedChars(String password) {
        int repeatCount = 1;
        char[] chars = password.toCharArray();

        for (int i = 1; i < chars.length; i++) {
            if (chars[i] == chars[i - 1]) {
                repeatCount++;
                if (repeatCount >= 3) {
                    return true;
                }
            } else {
                repeatCount = 1;
            }
        }

        return false;
    }

    /**
     * 计算密码强度
     * 返回值：1-弱，2-中，3-强
     *
     * @param password 密码
     * @return 密码强度
     */
    public static int getStrength(String password) {
        if (!isValid(password)) {
            return 0;
        }

        int strength = 0;

        // 长度加分
        if (password.length() >= 12) {
            strength++;
        }

        // 包含大小写字母加分
        if (password.matches(".*[a-z].*") && password.matches(".*[A-Z].*")) {
            strength++;
        }

        // 包含特殊字符加分（如果允许）
        if (password.matches(".*[^a-zA-Z0-9].*")) {
            strength++;
        }

        return Math.min(strength + 1, 3);
    }
}
