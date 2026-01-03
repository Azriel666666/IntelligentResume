package app.intelligent.resume.websocket;

import app.intelligent.resume.entity.User;
import app.intelligent.resume.security.JwtTokenProvider;
import app.intelligent.resume.service.IUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * WebSocket认证拦截器
 * 在握手阶段验证JWT Token
 *
 * @author Intelligent Resume Team
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements HandshakeInterceptor {

    private final JwtTokenProvider jwtTokenProvider;
    private final IUserService userService;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        try {
            if (request instanceof ServletServerHttpRequest servletRequest) {
                // 从URL参数获取token
                String token = servletRequest.getServletRequest().getParameter("token");
                
                if (token == null || token.isEmpty()) {
                    log.warn("WebSocket连接缺少token参数");
                    return false;
                }

                // 验证token
                if (!jwtTokenProvider.validateToken(token)) {
                    log.warn("WebSocket连接token无效");
                    return false;
                }

                // 从token中获取用户名（手机号）
                String username = jwtTokenProvider.getUsernameFromToken(token);
                
                // 通过用户名查询用户信息
                User user = userService.getByPhone(username);
                if (user == null) {
                    // 尝试通过用户名查询
                    user = userService.getByUsername(username);
                }
                
                if (user == null) {
                    log.warn("WebSocket连接用户不存在: {}", username);
                    return false;
                }

                // 将用户信息存入attributes，供后续使用
                attributes.put("userId", user.getId());
                attributes.put("phone", user.getPhone());
                attributes.put("userType", user.getUserType());

                log.info("WebSocket连接认证成功, userId: {}, phone: {}", user.getId(), user.getPhone());
                return true;
            }
        } catch (Exception e) {
            log.error("WebSocket认证失败", e);
        }
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // 握手完成后的处理（可选）
    }
}
