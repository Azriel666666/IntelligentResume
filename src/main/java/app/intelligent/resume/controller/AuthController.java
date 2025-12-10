package app.intelligent.resume.controller;

import app.intelligent.resume.common.enums.VerifyCodeType;
import app.intelligent.resume.common.result.Result;
import app.intelligent.resume.dto.request.LoginRequest;
import app.intelligent.resume.dto.request.PhoneRegisterRequest;
import app.intelligent.resume.dto.request.RegisterRequest;
import app.intelligent.resume.dto.request.SendCodeRequest;
import app.intelligent.resume.dto.request.VerifyCodeRequest;
import app.intelligent.resume.dto.response.LoginResponse;
import app.intelligent.resume.dto.response.SendCodeResponse;
import app.intelligent.resume.dto.response.VerifyCodeResponse;
import app.intelligent.resume.service.IAuthService;
import app.intelligent.resume.service.IVerifyCodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

/**
 * 认证控制器
 *
 * @author Intelligent Resume Team
 */
@Tag(name = "认证管理", description = "用户登录、注册、登出等认证相关接口")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {

    private final IAuthService authService;
    private final IVerifyCodeService verifyCodeService;

    @Operation(summary = "用户登录", description = "用户名密码登录，返回JWT Token")
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return Result.success(Collections.singletonList(response));
    }

    @Operation(summary = "用户注册", description = "注册新用户并自动登录")
    @PostMapping("/register")
    public Result<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        LoginResponse response = authService.register(request);
        return Result.success(Collections.singletonList(response));
    }

    @Operation(summary = "刷新Token", description = "使用刷新Token获取新的访问Token")
    @PostMapping("/refresh")
    public Result<LoginResponse> refreshToken(@RequestParam String refreshToken) {
        LoginResponse response = authService.refreshToken(refreshToken);
        return Result.success(Collections.singletonList(response));
    }

    @Operation(summary = "用户登出", description = "清除当前用户的认证信息")
    @PostMapping("/logout")
    public Result<Void> logout() {
        authService.logout();
        return Result.success("登出成功");
    }

    @Operation(summary = "发送验证码", description = "发送手机验证码，用于注册、登录、重置密码等场景")
    @PostMapping("/send-code")
    public Result<SendCodeResponse> sendCode(@Valid @RequestBody SendCodeRequest request,
                                              HttpServletRequest httpRequest) {
        String ip = getClientIp(httpRequest);
        VerifyCodeType type = VerifyCodeType.getByCode(request.getType());
        if (type == null) {
            type = VerifyCodeType.REGISTER;
        }

        verifyCodeService.sendVerifyCode(request.getPhone(), type, request.getRequestId(), ip);

        SendCodeResponse response = SendCodeResponse.builder()
                .success(true)
                .nextSendSeconds(60)
                .message("验证码已发送")
                .build();

        return Result.success(Collections.singletonList(response));
    }

    @Operation(summary = "验证验证码", description = "验证手机验证码是否正确")
    @PostMapping("/verify-code")
    public Result<VerifyCodeResponse> verifyCode(@Valid @RequestBody VerifyCodeRequest request) {
        VerifyCodeType type = VerifyCodeType.getByCode(request.getType());
        if (type == null) {
            type = VerifyCodeType.REGISTER;
        }

        verifyCodeService.verifyCode(request.getPhone(), request.getCode(), type);

        VerifyCodeResponse response = VerifyCodeResponse.builder()
                .success(true)
                .message("验证成功")
                .build();

        return Result.success(Collections.singletonList(response));
    }

    @Operation(summary = "手机号注册", description = "使用手机号和验证码注册新用户")
    @PostMapping("/register-by-phone")
    public Result<LoginResponse> registerByPhone(@Valid @RequestBody PhoneRegisterRequest request,
                                                  HttpServletRequest httpRequest) {
        String ip = getClientIp(httpRequest);
        LoginResponse response = authService.registerByPhone(request, ip);
        return Result.success(Collections.singletonList(response));
    }

    /**
     * 获取客户端IP地址
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多个代理时，取第一个IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
