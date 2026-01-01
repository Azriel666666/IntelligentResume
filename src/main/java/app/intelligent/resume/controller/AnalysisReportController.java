package app.intelligent.resume.controller;

import app.intelligent.resume.common.result.Result;
import app.intelligent.resume.entity.AnalysisReport;
import app.intelligent.resume.entity.Resume;
import app.intelligent.resume.entity.User;
import app.intelligent.resume.security.SecurityUtils;
import app.intelligent.resume.service.IAnalysisReportService;
import app.intelligent.resume.service.IResumeAnalysisService;
import app.intelligent.resume.service.IResumeService;
import app.intelligent.resume.service.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

/**
 * 简历分析控制器
 * 对应需求文档4.5简历分析模块
 *
 * @author Intelligent Resume Team
 */
@Tag(name = "简历分析管理", description = "简历AI分析接口")
@RestController
@RequestMapping("/api/analysis")
@RequiredArgsConstructor
@Validated
public class AnalysisReportController {

    private final IAnalysisReportService analysisReportService;
    private final IResumeAnalysisService resumeAnalysisService;
    private final IResumeService resumeService;
    private final IUserService userService;

    /**
     * 获取当前用户ID
     */
    private Long getCurrentUserId() {
        User currentUser = userService.getCurrentUser();
        return currentUser != null ? currentUser.getId() : null;
    }

    /**
     * 4.5.1 触发简历分析
     * POST /api/analysis/resume/{resumeId}
     */
    @Operation(summary = "触发简历分析", description = "对简历进行AI智能分析，生成分析报告")
    @PreAuthorize("hasAnyRole('ADMIN', 'SEEKER')")
    @PostMapping("/resume/{resumeId}")
    public Result<AnalysisReport> analyzeResume(
            @Parameter(description = "简历ID") @PathVariable Long resumeId) {
        
        // 验证权限：求职者只能分析自己的简历
        Long currentUserId = getCurrentUserId();
        Resume resume = resumeService.getById(resumeId);
        
        if (resume == null) {
            return Result.failed("简历不存在");
        }
        
        // 非管理员只能分析自己的简历
        if (!SecurityUtils.isAdmin() && !resume.getUserId().equals(currentUserId)) {
            return Result.failed("无权限分析该简历");
        }
        
        // 执行分析
        AnalysisReport report = resumeAnalysisService.analyzeResume(resumeId);
        return Result.success(Collections.singletonList(report));
    }

    /**
     * 4.5.3 获取分析报告
     * GET /api/analysis/resume/{resumeId}
     */
    @Operation(summary = "获取简历分析报告", description = "获取简历的最新分析报告")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'SEEKER')")
    @GetMapping("/resume/{resumeId}")
    public Result<AnalysisReport> getAnalysisReport(
            @Parameter(description = "简历ID") @PathVariable Long resumeId) {
        
        // 验证权限
        Long currentUserId = getCurrentUserId();
        Resume resume = resumeService.getById(resumeId);
        
        if (resume == null) {
            return Result.failed("简历不存在");
        }
        
        // 求职者只能查看自己的简历分析报告
        if (SecurityUtils.isSeeker() && !resume.getUserId().equals(currentUserId)) {
            return Result.failed("无权限查看该简历分析报告");
        }
        
        AnalysisReport report = resumeAnalysisService.getLatestReport(resumeId);
        if (report == null) {
            return Result.failed("该简历暂无分析报告，请先触发分析");
        }
        
        return Result.success(Collections.singletonList(report));
    }

    /**
     * 4.5.4 获取历史分析记录
     * GET /api/analysis/resume/{resumeId}/history
     */
    @Operation(summary = "获取历史分析记录", description = "获取简历的所有历史分析记录")
    @PreAuthorize("hasAnyRole('ADMIN', 'SEEKER')")
    @GetMapping("/resume/{resumeId}/history")
    public Result<AnalysisReport> getAnalysisHistory(
            @Parameter(description = "简历ID") @PathVariable Long resumeId) {
        
        // 验证权限
        Long currentUserId = getCurrentUserId();
        Resume resume = resumeService.getById(resumeId);
        
        if (resume == null) {
            return Result.failed("简历不存在");
        }
        
        // 非管理员只能查看自己的简历分析历史
        if (!SecurityUtils.isAdmin() && !resume.getUserId().equals(currentUserId)) {
            return Result.failed("无权限查看该简历分析历史");
        }
        
        List<AnalysisReport> reports = analysisReportService.listByResumeId(resumeId);
        return Result.success(reports);
    }

    /**
     * 获取分析报告详情
     * GET /api/analysis/{id}
     */
    @Operation(summary = "获取分析报告详情", description = "根据报告ID获取分析报告详情")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'SEEKER')")
    @GetMapping("/{id}")
    public Result<AnalysisReport> getReportById(
            @Parameter(description = "报告ID") @PathVariable Long id) {
        
        AnalysisReport report = analysisReportService.getById(id);
        if (report == null) {
            return Result.failed("分析报告不存在");
        }
        
        // 验证权限
        Resume resume = resumeService.getById(report.getResumeId());
        if (resume != null && SecurityUtils.isSeeker()) {
            Long currentUserId = getCurrentUserId();
            if (!resume.getUserId().equals(currentUserId)) {
                return Result.failed("无权限查看该分析报告");
            }
        }
        
        return Result.success(Collections.singletonList(report));
    }
}
