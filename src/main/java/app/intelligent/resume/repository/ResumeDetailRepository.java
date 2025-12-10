package app.intelligent.resume.repository;

import app.intelligent.resume.entity.ResumeDetail;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 简历详情Repository接口
 *
 * @author Intelligent Resume Team
 */
@Mapper
public interface ResumeDetailRepository extends BaseMapper<ResumeDetail> {

    /**
     * 根据简历ID查询详情
     */
    @Select("SELECT * FROM resume_detail WHERE resume_id = #{resumeId}")
    ResumeDetail selectByResumeId(@Param("resumeId") Long resumeId);
}
