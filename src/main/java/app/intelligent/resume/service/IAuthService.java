package app.intelligent.resume.service;

import app.intelligent.resume.dto.request.LoginRequest;
import app.intelligent.resume.dto.request.PhoneRegisterRequest;
import app.intelligent.resume.dto.request.RegisterRequest;
import app.intelligent.resume.dto.response.LoginResponse;

/**
 * 认证Service接口
 *
 * @author Intelligent Resume Team
 */
public interface IAuthService {

    /**
     * 用户登录
     */
    LoginResponse login(LoginRequest request);

    /**
     * 用户注册
     */
    LoginResponse register(RegisterRequest request);

    /**
     * 手机号注册
     *
     * @param request 注册请求
     * @param ip      请求IP
     * @return 登录响应（包含Token）
     */
    LoginResponse registerByPhone(PhoneRegisterRequest request, String ip);

    /**
     * 刷新Token
     */
    LoginResponse refreshToken(String refreshToken);

    /**
     * 登出
     */
    void logout();
}
