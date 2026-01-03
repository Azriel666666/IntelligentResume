package app.intelligent.resume.repository;

import app.intelligent.resume.entity.Notification;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

/**
 * 系统通知数据访问层
 *
 * @author Intelligent Resume Team
 */
@Mapper
public interface NotificationRepository extends BaseMapper<Notification> {

    /**
     * 获取用户的通知列表
     */
    @Select("SELECT * FROM notification WHERE user_id = #{userId} ORDER BY create_time DESC LIMIT #{offset}, #{limit}")
    List<Notification> findByUserId(@Param("userId") Long userId, @Param("offset") int offset, @Param("limit") int limit);

    /**
     * 统计用户未读通知总数
     */
    @Select("SELECT COUNT(*) FROM notification WHERE user_id = #{userId} AND is_read = 0")
    int countUnreadByUserId(@Param("userId") Long userId);

    /**
     * 按类型统计未读通知数
     */
    @Select("SELECT type, COUNT(*) as count FROM notification WHERE user_id = #{userId} AND is_read = 0 GROUP BY type")
    List<Map<String, Object>> countUnreadByType(@Param("userId") Long userId);

    /**
     * 标记所有通知为已读
     */
    @Update("UPDATE notification SET is_read = 1, read_time = NOW() WHERE user_id = #{userId} AND is_read = 0")
    int markAllAsRead(@Param("userId") Long userId);

    /**
     * 按类型标记通知为已读
     */
    @Update("UPDATE notification SET is_read = 1, read_time = NOW() WHERE user_id = #{userId} AND type = #{type} AND is_read = 0")
    int markAsReadByType(@Param("userId") Long userId, @Param("type") String type);
}
