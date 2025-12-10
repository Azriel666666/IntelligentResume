package app.intelligent.resume.service;

import app.intelligent.resume.entity.AnalysisReport;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 分析报告Service接口
 *
 * @author Intelligent Resume Team
 */
public interface IAnalysisReportService extends IService<AnalysisReport> {

    /**
     * 根据简历ID查询分析报告列表
     */
    List<AnalysisReport> listByResumeId(Long resumeId);

    /**
     * 查询简历的最新分析报告
     */
    AnalysisReport getLatestByResumeId(Long resumeId);

    /**
     * 创建分析报告
     */
    AnalysisReport createReport(AnalysisReport report);
}
