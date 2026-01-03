package app.intelligent.resume.repository;

import app.intelligent.resume.entity.Conversation;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 会话数据访问层
 *
 * @author Intelligent Resume Team
 */
@Mapper
public interface ConversationRepository extends BaseMapper<Conversation> {

    /**
     * 根据HR和求职者及岗位查找会话
     */
    @Select("SELECT * FROM conversation WHERE hr_user_id = #{hrUserId} AND seeker_user_id = #{seekerUserId} AND job_id = #{jobId}")
    Conversation findByHrAndSeekerAndJob(@Param("hrUserId") Long hrUserId, 
                                          @Param("seekerUserId") Long seekerUserId, 
                                          @Param("jobId") Long jobId);

    /**
     * 获取用户的所有会话（HR）
     */
    @Select("SELECT * FROM conversation WHERE hr_user_id = #{userId} AND status = 1 ORDER BY last_message_time DESC")
    List<Conversation> findByHrUserId(@Param("userId") Long userId);

    /**
     * 获取用户的所有会话（求职者）
     */
    @Select("SELECT * FROM conversation WHERE seeker_user_id = #{userId} AND status = 1 ORDER BY last_message_time DESC")
    List<Conversation> findBySeekerUserId(@Param("userId") Long userId);

    /**
     * 增加HR未读数
     */
    @Update("UPDATE conversation SET hr_unread_count = hr_unread_count + 1 WHERE id = #{conversationId}")
    int incrementHrUnreadCount(@Param("conversationId") Long conversationId);

    /**
     * 增加求职者未读数
     */
    @Update("UPDATE conversation SET seeker_unread_count = seeker_unread_count + 1 WHERE id = #{conversationId}")
    int incrementSeekerUnreadCount(@Param("conversationId") Long conversationId);

    /**
     * 清空HR未读数
     */
    @Update("UPDATE conversation SET hr_unread_count = 0 WHERE id = #{conversationId}")
    int clearHrUnreadCount(@Param("conversationId") Long conversationId);

    /**
     * 清空求职者未读数
     */
    @Update("UPDATE conversation SET seeker_unread_count = 0 WHERE id = #{conversationId}")
    int clearSeekerUnreadCount(@Param("conversationId") Long conversationId);
}
