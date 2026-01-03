package app.intelligent.resume.websocket;

import app.intelligent.resume.common.util.HtmlSanitizer;
import app.intelligent.resume.dto.response.ChatMessageResponse;
import app.intelligent.resume.entity.ChatMessage;
import app.intelligent.resume.entity.Conversation;
import app.intelligent.resume.service.IChatMessageService;
import app.intelligent.resume.service.IConversationService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * WebSocket消息处理器
 * 处理聊天消息的实时收发
 *
 * @author Intelligent Resume Team
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final WebSocketSessionManager sessionManager;
    private final IChatMessageService chatMessageService;
    private final IConversationService conversationService;
    private final ObjectMapper objectMapper;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Map<String, Object> attributes = session.getAttributes();
        Long userId = (Long) attributes.get("userId");
        
        if (userId != null) {
            sessionManager.addSession(userId, session);
            log.info("WebSocket连接建立成功, userId: {}", userId);
        } else {
            log.warn("WebSocket连接建立失败: 未找到用户ID");
            session.close(CloseStatus.NOT_ACCEPTABLE);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Long userId = sessionManager.getUserId(session);
        if (userId == null) {
            sendError(session, "用户未认证");
            return;
        }

        try {
            String payload = message.getPayload();
            JsonNode jsonNode = objectMapper.readTree(payload);
            String type = jsonNode.has("type") ? jsonNode.get("type").asText() : "";

            switch (type) {
                case "SEND_MESSAGE":
                    handleSendMessage(session, userId, jsonNode);
                    break;
                case "TYPING":
                    handleTyping(userId, jsonNode);
                    break;
                case "READ":
                    handleMarkRead(userId, jsonNode);
                    break;
                case "HEARTBEAT":
                    handleHeartbeat(session);
                    break;
                default:
                    sendError(session, "未知的消息类型: " + type);
            }
        } catch (Exception e) {
            log.error("处理WebSocket消息失败", e);
            sendError(session, "消息处理失败: " + e.getMessage());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Long userId = sessionManager.getUserId(session);
        sessionManager.removeSession(session);
        log.info("WebSocket连接关闭, userId: {}, status: {}", userId, status);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("WebSocket传输错误", exception);
        sessionManager.removeSession(session);
    }


    /**
     * 处理发送消息
     */
    private void handleSendMessage(WebSocketSession session, Long senderId, JsonNode jsonNode) throws IOException {
        Long conversationId = jsonNode.has("conversationId") ? jsonNode.get("conversationId").asLong() : null;
        String content = jsonNode.has("content") ? jsonNode.get("content").asText() : "";
        String messageType = jsonNode.has("messageType") ? jsonNode.get("messageType").asText() : "TEXT";
        String attachments = jsonNode.has("attachments") ? jsonNode.get("attachments").toString() : null;

        if (conversationId == null) {
            sendError(session, "会话ID不能为空");
            return;
        }

        // 验证会话权限
        Conversation conversation = conversationService.getById(conversationId);
        if (conversation == null) {
            sendError(session, "会话不存在");
            return;
        }

        // 检查用户是否是会话参与者
        if (!senderId.equals(conversation.getHrUserId()) && !senderId.equals(conversation.getSeekerUserId())) {
            sendError(session, "无权限发送消息到此会话");
            return;
        }

        // 检查会话状态
        if (conversation.getStatus() != 1) {
            sendError(session, "会话已关闭，无法发送消息");
            return;
        }

        // HTML防注入处理
        String sanitizedContent = HtmlSanitizer.sanitize(content);

        // 确定接收者
        Long receiverId = senderId.equals(conversation.getHrUserId()) 
                ? conversation.getSeekerUserId() 
                : conversation.getHrUserId();

        // 创建消息
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setConversationId(conversationId);
        chatMessage.setSenderId(senderId);
        chatMessage.setReceiverId(receiverId);
        chatMessage.setMessageType(messageType);
        chatMessage.setContent(sanitizedContent);
        chatMessage.setAttachments(attachments);
        chatMessage.setIsRead(0);
        chatMessage.setIsRecalled(0);
        chatMessage.setCreateTime(LocalDateTime.now());

        // 保存消息
        ChatMessageResponse savedMessage = chatMessageService.sendMessage(chatMessage);

        // 更新会话最后消息
        conversationService.updateLastMessage(conversationId, savedMessage.getId(), 
                sanitizedContent.length() > 100 ? sanitizedContent.substring(0, 100) + "..." : sanitizedContent);

        // 更新未读数
        if (senderId.equals(conversation.getHrUserId())) {
            conversationService.incrementSeekerUnreadCount(conversationId);
        } else {
            conversationService.incrementHrUnreadCount(conversationId);
        }

        // 发送消息给发送者（确认）
        sendMessage(session, WebSocketMessage.newMessage(savedMessage));

        // 发送消息给接收者（如果在线）
        WebSocketSession receiverSession = sessionManager.getSession(receiverId);
        if (receiverSession != null && receiverSession.isOpen()) {
            sendMessage(receiverSession, WebSocketMessage.newMessage(savedMessage));
        }

        log.info("消息发送成功: conversationId={}, senderId={}, receiverId={}", 
                conversationId, senderId, receiverId);
    }

    /**
     * 处理正在输入通知
     */
    private void handleTyping(Long userId, JsonNode jsonNode) throws IOException {
        Long conversationId = jsonNode.has("conversationId") ? jsonNode.get("conversationId").asLong() : null;
        if (conversationId == null) {
            return;
        }

        Conversation conversation = conversationService.getById(conversationId);
        if (conversation == null) {
            return;
        }

        // 确定接收者
        Long receiverId = userId.equals(conversation.getHrUserId()) 
                ? conversation.getSeekerUserId() 
                : conversation.getHrUserId();

        // 发送正在输入通知给对方
        WebSocketSession receiverSession = sessionManager.getSession(receiverId);
        if (receiverSession != null && receiverSession.isOpen()) {
            sendMessage(receiverSession, WebSocketMessage.typing(conversationId, userId));
        }
    }

    /**
     * 处理标记已读
     */
    private void handleMarkRead(Long userId, JsonNode jsonNode) throws IOException {
        Long conversationId = jsonNode.has("conversationId") ? jsonNode.get("conversationId").asLong() : null;
        if (conversationId == null) {
            return;
        }

        Conversation conversation = conversationService.getById(conversationId);
        if (conversation == null) {
            return;
        }

        // 标记消息已读
        chatMessageService.markAllAsRead(conversationId, userId);

        // 清空未读数
        if (userId.equals(conversation.getHrUserId())) {
            conversationService.clearHrUnreadCount(conversationId);
        } else {
            conversationService.clearSeekerUnreadCount(conversationId);
        }

        // 确定对方用户
        Long otherUserId = userId.equals(conversation.getHrUserId()) 
                ? conversation.getSeekerUserId() 
                : conversation.getHrUserId();

        // 发送已读回执给对方
        WebSocketSession otherSession = sessionManager.getSession(otherUserId);
        if (otherSession != null && otherSession.isOpen()) {
            sendMessage(otherSession, WebSocketMessage.messageRead(
                    Map.of("conversationId", conversationId, "readBy", userId)));
        }
    }

    /**
     * 处理心跳
     */
    private void handleHeartbeat(WebSocketSession session) throws IOException {
        sendMessage(session, WebSocketMessage.heartbeat());
    }

    /**
     * 发送消息
     */
    private void sendMessage(WebSocketSession session, WebSocketMessage message) throws IOException {
        if (session != null && session.isOpen()) {
            String json = objectMapper.writeValueAsString(message);
            session.sendMessage(new TextMessage(json));
        }
    }

    /**
     * 发送错误消息
     */
    private void sendError(WebSocketSession session, String errorMessage) throws IOException {
        sendMessage(session, WebSocketMessage.error(errorMessage));
    }

    /**
     * 向指定用户发送消息（供其他服务调用）
     */
    public void sendToUser(Long userId, WebSocketMessage message) {
        WebSocketSession session = sessionManager.getSession(userId);
        if (session != null && session.isOpen()) {
            try {
                sendMessage(session, message);
            } catch (IOException e) {
                log.error("发送消息给用户 {} 失败", userId, e);
            }
        }
    }

    /**
     * 检查用户是否在线
     */
    public boolean isUserOnline(Long userId) {
        return sessionManager.isOnline(userId);
    }
}
