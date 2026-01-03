package app.intelligent.resume.repository;

import app.intelligent.resume.entity.InterviewInvitation;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 面试邀请数据访问层
 *
 * @author Intelligent Resume Team
 */
@Mapper
public interface InterviewInvitationRepository extends BaseMapper<InterviewInvitation> {

    /**
     * 根据求职者ID查询面试邀请
     */
    @Select("SELECT * FROM interview_invitation WHERE seeker_user_id = #{seekerUserId} ORDER BY create_time DESC")
    List<InterviewInvitation> findBySeekerUserId(@Param("seekerUserId") Long seekerUserId);

    /**
     * 根据HR用户ID查询面试邀请
     */
    @Select("SELECT * FROM interview_invitation WHERE hr_user_id = #{hrUserId} ORDER BY create_time DESC")
    List<InterviewInvitation> findByHrUserId(@Param("hrUserId") Long hrUserId);

    /**
     * 统计求职者收到的面试邀请数
     */
    @Select("SELECT COUNT(*) FROM interview_invitation WHERE seeker_user_id = #{seekerUserId}")
    long countBySeekerUserId(@Param("seekerUserId") Long seekerUserId);

    /**
     * 统计HR发出的面试邀请数
     */
    @Select("SELECT COUNT(*) FROM interview_invitation WHERE hr_user_id = #{hrUserId}")
    long countByHrUserId(@Param("hrUserId") Long hrUserId);

    /**
     * 根据岗位ID查询面试邀请
     */
    @Select("SELECT * FROM interview_invitation WHERE job_id = #{jobId} ORDER BY interview_time DESC")
    List<InterviewInvitation> findByJobId(@Param("jobId") Long jobId);
}
