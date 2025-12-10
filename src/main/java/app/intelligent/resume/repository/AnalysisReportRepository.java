package app.intelligent.resume.repository;

import app.intelligent.resume.entity.AnalysisReport;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 分析报告Repository接口
 *
 * @author Intelligent Resume Team
 */
@Mapper
public interface AnalysisReportRepository extends BaseMapper<AnalysisReport> {

    /**
     * 根据简历ID查询分析报告列表
     */
    @Select("SELECT * FROM analysis_report WHERE resume_id = #{resumeId} ORDER BY create_time DESC")
    List<AnalysisReport> selectByResumeId(@Param("resumeId") Long resumeId);

    /**
     * 查询简历的最新分析报告
     */
    @Select("SELECT * FROM analysis_report WHERE resume_id = #{resumeId} ORDER BY create_time DESC LIMIT 1")
    AnalysisReport selectLatestByResumeId(@Param("resumeId") Long resumeId);
}
