package app.intelligent.resume.websocket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * WebSocket消息封装类
 *
 * @author Intelligent Resume Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebSocketMessage {

    /**
     * 消息类型
     */
    private String type;

    /**
     * 消息数据
     */
    private Object data;

    /**
     * 时间戳
     */
    private Long timestamp;

    // ========== 消息类型常量 ==========

    /**
     * 新消息
     */
    public static final String TYPE_NEW_MESSAGE = "NEW_MESSAGE";

    /**
     * 消息已读回执
     */
    public static final String TYPE_MESSAGE_READ = "MESSAGE_READ";

    /**
     * 新通知
     */
    public static final String TYPE_NEW_NOTIFICATION = "NEW_NOTIFICATION";

    /**
     * 对方正在输入
     */
    public static final String TYPE_TYPING = "TYPING";

    /**
     * 用户上线
     */
    public static final String TYPE_USER_ONLINE = "USER_ONLINE";

    /**
     * 用户下线
     */
    public static final String TYPE_USER_OFFLINE = "USER_OFFLINE";

    /**
     * 心跳
     */
    public static final String TYPE_HEARTBEAT = "HEARTBEAT";

    /**
     * 错误
     */
    public static final String TYPE_ERROR = "ERROR";

    /**
     * 创建新消息通知
     */
    public static WebSocketMessage newMessage(Object data) {
        return WebSocketMessage.builder()
                .type(TYPE_NEW_MESSAGE)
                .data(data)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 创建已读回执
     */
    public static WebSocketMessage messageRead(Object data) {
        return WebSocketMessage.builder()
                .type(TYPE_MESSAGE_READ)
                .data(data)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 创建新通知
     */
    public static WebSocketMessage newNotification(Object data) {
        return WebSocketMessage.builder()
                .type(TYPE_NEW_NOTIFICATION)
                .data(data)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 创建正在输入通知
     */
    public static WebSocketMessage typing(Long conversationId, Long userId) {
        return WebSocketMessage.builder()
                .type(TYPE_TYPING)
                .data(new TypingData(conversationId, userId))
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 创建心跳响应
     */
    public static WebSocketMessage heartbeat() {
        return WebSocketMessage.builder()
                .type(TYPE_HEARTBEAT)
                .data("pong")
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 创建错误消息
     */
    public static WebSocketMessage error(String message) {
        return WebSocketMessage.builder()
                .type(TYPE_ERROR)
                .data(message)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 正在输入数据
     */
    @Data
    @AllArgsConstructor
    public static class TypingData {
        private Long conversationId;
        private Long userId;
    }
}
