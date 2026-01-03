package app.intelligent.resume.service;

import app.intelligent.resume.dto.request.ConversationCreateRequest;
import app.intelligent.resume.dto.response.ConversationResponse;
import app.intelligent.resume.entity.Conversation;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 会话服务接口
 *
 * @author Intelligent Resume Team
 */
public interface IConversationService extends IService<Conversation> {

    /**
     * 创建或获取会话
     */
    ConversationResponse createOrGetConversation(Long currentUserId, Integer userType, ConversationCreateRequest request);

    /**
     * 获取用户的会话列表
     */
    Page<ConversationResponse> getUserConversations(Long userId, Integer userType, Integer status, int page, int size);

    /**
     * 获取会话详情
     */
    ConversationResponse getConversationDetail(Long conversationId, Long userId);

    /**
     * 关闭会话
     */
    void closeConversation(Long conversationId, Long hrUserId);

    /**
     * 更新最后消息
     */
    void updateLastMessage(Long conversationId, Long messageId, String content);

    /**
     * 增加HR未读数
     */
    void incrementHrUnreadCount(Long conversationId);

    /**
     * 增加求职者未读数
     */
    void incrementSeekerUnreadCount(Long conversationId);

    /**
     * 清空HR未读数
     */
    void clearHrUnreadCount(Long conversationId);

    /**
     * 清空求职者未读数
     */
    void clearSeekerUnreadCount(Long conversationId);

    /**
     * 检查用户是否是会话参与者
     */
    boolean isParticipant(Long conversationId, Long userId);
}
