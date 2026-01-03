package app.intelligent.resume.repository;

import app.intelligent.resume.entity.ChatMessage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 聊天消息数据访问层
 *
 * @author Intelligent Resume Team
 */
@Mapper
public interface ChatMessageRepository extends BaseMapper<ChatMessage> {

    /**
     * 获取会话的消息列表（分页）
     */
    @Select("SELECT * FROM chat_message WHERE conversation_id = #{conversationId} " +
            "ORDER BY create_time DESC LIMIT #{offset}, #{limit}")
    List<ChatMessage> findByConversationId(@Param("conversationId") Long conversationId,
                                           @Param("offset") int offset,
                                           @Param("limit") int limit);

    /**
     * 获取指定ID之前的消息（用于上拉加载更多）
     */
    @Select("SELECT * FROM chat_message WHERE conversation_id = #{conversationId} AND id < #{beforeId} " +
            "ORDER BY create_time DESC LIMIT #{limit}")
    List<ChatMessage> findBeforeId(@Param("conversationId") Long conversationId,
                                   @Param("beforeId") Long beforeId,
                                   @Param("limit") int limit);

    /**
     * 标记会话中所有消息为已读
     */
    @Update("UPDATE chat_message SET is_read = 1, read_time = NOW() " +
            "WHERE conversation_id = #{conversationId} AND receiver_id = #{receiverId} AND is_read = 0")
    int markAllAsRead(@Param("conversationId") Long conversationId, @Param("receiverId") Long receiverId);

    /**
     * 统计会话未读消息数
     */
    @Select("SELECT COUNT(*) FROM chat_message WHERE conversation_id = #{conversationId} " +
            "AND receiver_id = #{receiverId} AND is_read = 0")
    int countUnread(@Param("conversationId") Long conversationId, @Param("receiverId") Long receiverId);
}
