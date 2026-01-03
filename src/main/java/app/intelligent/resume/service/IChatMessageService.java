package app.intelligent.resume.service;

import app.intelligent.resume.dto.response.ChatMessageResponse;
import app.intelligent.resume.entity.ChatMessage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 聊天消息服务接口
 *
 * @author Intelligent Resume Team
 */
public interface IChatMessageService extends IService<ChatMessage> {

    /**
     * 发送消息
     */
    ChatMessageResponse sendMessage(ChatMessage message);

    /**
     * 获取会话消息列表
     */
    Page<ChatMessageResponse> getConversationMessages(Long conversationId, Long userId, int page, int size);

    /**
     * 获取指定ID之前的消息（上拉加载更多）
     */
    Page<ChatMessageResponse> getMessagesBefore(Long conversationId, Long beforeId, Long userId, int limit);

    /**
     * 标记会话所有消息为已读
     */
    void markAllAsRead(Long conversationId, Long userId);

    /**
     * 撤回消息
     */
    void recallMessage(Long messageId, Long userId);

    /**
     * 统计未读消息数
     */
    int countUnread(Long conversationId, Long userId);
}
