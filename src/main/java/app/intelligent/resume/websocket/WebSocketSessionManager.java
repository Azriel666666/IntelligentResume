package app.intelligent.resume.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket会话管理器
 * 管理所有在线用户的WebSocket连接
 *
 * @author Intelligent Resume Team
 */
@Slf4j
@Component
public class WebSocketSessionManager {

    /**
     * 用户ID -> WebSocket会话映射
     */
    private final Map<Long, WebSocketSession> userSessions = new ConcurrentHashMap<>();

    /**
     * 会话ID -> 用户ID映射（用于快速查找）
     */
    private final Map<String, Long> sessionUserMap = new ConcurrentHashMap<>();

    /**
     * 添加用户会话
     */
    public void addSession(Long userId, WebSocketSession session) {
        // 如果用户已有连接，先关闭旧连接
        WebSocketSession oldSession = userSessions.get(userId);
        if (oldSession != null && oldSession.isOpen()) {
            try {
                oldSession.close();
                log.info("关闭用户 {} 的旧WebSocket连接", userId);
            } catch (Exception e) {
                log.error("关闭旧WebSocket连接失败", e);
            }
        }

        userSessions.put(userId, session);
        sessionUserMap.put(session.getId(), userId);
        log.info("用户 {} 建立WebSocket连接, sessionId: {}, 当前在线人数: {}", 
                userId, session.getId(), userSessions.size());
    }

    /**
     * 移除用户会话
     */
    public void removeSession(WebSocketSession session) {
        Long userId = sessionUserMap.remove(session.getId());
        if (userId != null) {
            userSessions.remove(userId);
            log.info("用户 {} 断开WebSocket连接, 当前在线人数: {}", userId, userSessions.size());
        }
    }

    /**
     * 获取用户会话
     */
    public WebSocketSession getSession(Long userId) {
        return userSessions.get(userId);
    }

    /**
     * 检查用户是否在线
     */
    public boolean isOnline(Long userId) {
        WebSocketSession session = userSessions.get(userId);
        return session != null && session.isOpen();
    }

    /**
     * 获取在线用户数
     */
    public int getOnlineCount() {
        return userSessions.size();
    }

    /**
     * 根据会话获取用户ID
     */
    public Long getUserId(WebSocketSession session) {
        return sessionUserMap.get(session.getId());
    }
}
