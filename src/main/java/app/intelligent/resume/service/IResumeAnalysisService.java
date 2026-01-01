package app.intelligent.resume.service;

import app.intelligent.resume.entity.AnalysisReport;
import app.intelligent.resume.entity.ResumeDetail;

/**
 * 简历分析服务接口
 *
 * @author Intelligent Resume Team
 */
public interface IResumeAnalysisService {

    /**
     * 分析简历并生成报告
     * 
     * @param resumeId 简历ID
     * @return 分析报告
     */
    AnalysisReport analyzeResume(Long resumeId);

    /**
     * 分析简历详情并生成报告
     * 
     * @param resumeId 简历ID
     * @param resumeDetail 简历详情
     * @return 分析报告
     */
    AnalysisReport analyzeResumeDetail(Long resumeId, ResumeDetail resumeDetail);

    /**
     * 计算完整度评分（规则引擎）
     * 
     * @param resumeDetail 简历详情
     * @return 完整度评分（0-100）
     */
    int calculateCompletenessScore(ResumeDetail resumeDetail);

    /**
     * 分析完整度详情（规则引擎）
     * 
     * @param resumeDetail 简历详情
     * @return 完整度分析JSON
     */
    String analyzeCompleteness(ResumeDetail resumeDetail);

    /**
     * 分析技能（规则引擎）
     * 
     * @param resumeDetail 简历详情
     * @return 技能分析JSON
     */
    String analyzeSkills(ResumeDetail resumeDetail);

    /**
     * 计算格式评分（规则引擎）
     * 
     * @param resumeDetail 简历详情
     * @return 格式评分（0-100）
     */
    int calculateFormatScore(ResumeDetail resumeDetail);

    /**
     * 获取简历的最新分析报告
     * 
     * @param resumeId 简历ID
     * @return 分析报告
     */
    AnalysisReport getLatestReport(Long resumeId);

    /**
     * 将简历详情转换为文本内容（用于AI分析）
     * 
     * @param resumeDetail 简历详情
     * @return 简历文本内容
     */
    String convertToText(ResumeDetail resumeDetail);
}