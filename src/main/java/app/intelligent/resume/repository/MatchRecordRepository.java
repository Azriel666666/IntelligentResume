package app.intelligent.resume.repository;

import app.intelligent.resume.entity.MatchRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 匹配记录Repository接口
 *
 * @author Intelligent Resume Team
 */
@Mapper
public interface MatchRecordRepository extends BaseMapper<MatchRecord> {

    /**
     * 根据简历ID查询匹配记录
     */
    @Select("SELECT * FROM match_record WHERE resume_id = #{resumeId} ORDER BY match_score DESC, create_time DESC")
    List<MatchRecord> selectByResumeId(@Param("resumeId") Long resumeId);

    /**
     * 根据岗位ID查询匹配记录
     */
    @Select("SELECT * FROM match_record WHERE job_id = #{jobId} ORDER BY match_score DESC, create_time DESC")
    List<MatchRecord> selectByJobId(@Param("jobId") Long jobId);

    /**
     * 查询简历和岗位的匹配记录
     */
    @Select("SELECT * FROM match_record WHERE resume_id = #{resumeId} AND job_id = #{jobId}")
    MatchRecord selectByResumeAndJob(@Param("resumeId") Long resumeId, @Param("jobId") Long jobId);
}
