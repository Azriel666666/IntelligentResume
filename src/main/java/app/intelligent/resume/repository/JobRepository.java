package app.intelligent.resume.repository;

import app.intelligent.resume.entity.Job;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 岗位Repository接口
 *
 * @author Intelligent Resume Team
 */
@Mapper
public interface JobRepository extends BaseMapper<Job> {

    /**
     * 根据发布者ID查询岗位列表
     */
    @Select("SELECT * FROM job WHERE publisher_id = #{publisherId} AND deleted = 0 ORDER BY create_time DESC")
    List<Job> selectByPublisherId(@Param("publisherId") Long publisherId);

    /**
     * 查询招聘中的岗位
     */
    @Select("SELECT * FROM job WHERE status = 1 AND deleted = 0 ORDER BY is_top DESC, create_time DESC")
    List<Job> selectRecruitingJobs();
}
