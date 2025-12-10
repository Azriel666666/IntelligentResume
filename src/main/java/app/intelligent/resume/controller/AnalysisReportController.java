package app.intelligent.resume.controller;

import app.intelligent.resume.common.result.Result;
import app.intelligent.resume.entity.AnalysisReport;
import app.intelligent.resume.service.IAnalysisReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

/**
 * 分析报告控制器
 *
 * @author Intelligent Resume Team
 */
@Tag(name = "分析报告管理", description = "简历分析报告接口")
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Validated
public class AnalysisReportController {

    private final IAnalysisReportService analysisReportService;

    @Operation(summary = "创建分析报告", description = "为简历创建分析报告")
    @PreAuthorize("hasAnyRole('ADMIN', 'SEEKER')")
    @PostMapping
    public Result<AnalysisReport> createReport(@Valid @RequestBody AnalysisReport report) {
        AnalysisReport created = analysisReportService.createReport(report);
        return Result.success(Collections.singletonList(created));
    }

    @Operation(summary = "查询简历的分析报告", description = "根据简历ID查询所有分析报告")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'SEEKER')")
    @GetMapping("/resume/{resumeId}")
    public Result<AnalysisReport> getReportsByResumeId(
            @Parameter(description = "简历ID") @PathVariable Long resumeId) {
        List<AnalysisReport> reports = analysisReportService.listByResumeId(resumeId);
        return Result.success(reports);
    }

    @Operation(summary = "查询简历的最新分析报告", description = "获取简历的最新一份分析报告")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'SEEKER')")
    @GetMapping("/resume/{resumeId}/latest")
    public Result<AnalysisReport> getLatestReportByResumeId(
            @Parameter(description = "简历ID") @PathVariable Long resumeId) {
        AnalysisReport report = analysisReportService.getLatestByResumeId(resumeId);
        return Result.success(Collections.singletonList(report));
    }
}
