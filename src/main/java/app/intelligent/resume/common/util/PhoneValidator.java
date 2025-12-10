package app.intelligent.resume.common.util;

import app.intelligent.resume.common.exception.BusinessException;
import app.intelligent.resume.common.result.ResultCode;

import java.util.regex.Pattern;

/**
 * 手机号验证工具类
 *
 * @author Intelligent Resume Team
 */
public class PhoneValidator {

    /**
     * 中国大陆手机号正则表达式
     * 规则：11位数字，13-19开头
     */
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");

    /**
     * 验证手机号格式
     *
     * @param phone 手机号
     * @return 是否有效
     */
    public static boolean isValid(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        return PHONE_PATTERN.matcher(phone.trim()).matches();
    }

    /**
     * 验证手机号格式，不通过则抛出异常
     *
     * @param phone 手机号
     * @throws BusinessException 格式错误时抛出
     */
    public static void validate(String phone) {
        if (!isValid(phone)) {
            throw new BusinessException(ResultCode.PHONE_FORMAT_ERROR);
        }
    }

    /**
     * 脱敏手机号
     * 138****0000
     *
     * @param phone 手机号
     * @return 脱敏后的手机号
     */
    public static String desensitize(String phone) {
        if (phone == null || phone.length() != 11) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }

    /**
     * 格式化手机号
     * 去除空格、横杠等
     *
     * @param phone 手机号
     * @return 格式化后的手机号
     */
    public static String format(String phone) {
        if (phone == null) {
            return null;
        }
        return phone.replaceAll("[\\s-]", "").trim();
    }
}
