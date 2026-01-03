package app.intelligent.resume.repository;

import app.intelligent.resume.entity.JobApplication;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 投递记录数据访问层
 *
 * @author Intelligent Resume Team
 */
@Mapper
public interface JobApplicationRepository extends BaseMapper<JobApplication> {

    /**
     * 根据用户ID查询投递记录（带岗位信息）
     */
    @Select("SELECT ja.*, j.job_title, j.company_name, j.city, j.salary_range " +
            "FROM job_application ja " +
            "LEFT JOIN job j ON ja.job_id = j.id " +
            "WHERE ja.user_id = #{userId} " +
            "ORDER BY ja.create_time DESC")
    List<JobApplication> findByUserIdWithJob(@Param("userId") Long userId);

    /**
     * 根据岗位ID查询投递记录（带简历信息）
     */
    @Select("SELECT ja.*, r.title as resume_title, rd.name, rd.phone, rd.email, rd.skill_tags " +
            "FROM job_application ja " +
            "LEFT JOIN resume r ON ja.resume_id = r.id " +
            "LEFT JOIN resume_detail rd ON r.id = rd.resume_id " +
            "WHERE ja.job_id = #{jobId} " +
            "ORDER BY ja.create_time DESC")
    List<JobApplication> findByJobIdWithResume(@Param("jobId") Long jobId);

    /**
     * 检查是否已投递
     */
    @Select("SELECT COUNT(*) FROM job_application WHERE user_id = #{userId} AND job_id = #{jobId}")
    int countByUserIdAndJobId(@Param("userId") Long userId, @Param("jobId") Long jobId);

    /**
     * 统计岗位收到的投递数量
     */
    @Select("SELECT COUNT(*) FROM job_application WHERE job_id = #{jobId}")
    int countByJobId(@Param("jobId") Long jobId);

    /**
     * 统计用户的投递数量
     */
    @Select("SELECT COUNT(*) FROM job_application WHERE user_id = #{userId}")
    int countByUserId(@Param("userId") Long userId);
}
