package app.intelligent.resume.service.impl;

import app.intelligent.resume.entity.AnalysisReport;
import app.intelligent.resume.repository.AnalysisReportRepository;
import app.intelligent.resume.service.IAnalysisReportService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 分析报告Service实现类
 *
 * @author Intelligent Resume Team
 */
@Service
@RequiredArgsConstructor
public class AnalysisReportServiceImpl extends ServiceImpl<AnalysisReportRepository, AnalysisReport> implements IAnalysisReportService {

    private final AnalysisReportRepository analysisReportRepository;

    @Override
    public List<AnalysisReport> listByResumeId(Long resumeId) {
        return analysisReportRepository.selectByResumeId(resumeId);
    }

    @Override
    public AnalysisReport getLatestByResumeId(Long resumeId) {
        return analysisReportRepository.selectLatestByResumeId(resumeId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AnalysisReport createReport(AnalysisReport report) {
        save(report);
        return report;
    }
}
