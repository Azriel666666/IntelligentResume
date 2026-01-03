package app.intelligent.resume.controller;

import app.intelligent.resume.common.result.Result;
import app.intelligent.resume.dto.request.ApplicationCreateRequest;
import app.intelligent.resume.dto.request.ApplicationStatusRequest;
import app.intelligent.resume.dto.response.ApplicationResponse;
import app.intelligent.resume.entity.JobApplication;
import app.intelligent.resume.entity.User;
import app.intelligent.resume.security.SecurityUtils;
import app.intelligent.resume.service.IApplicationService;
import app.intelligent.resume.service.IJobService;
import app.intelligent.resume.service.IUserService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

/**
 * 投递控制器
 * 对应需求文档4.8投递模块
 *
 * @author Intelligent Resume Team
 */
@Tag(name = "投递管理", description = "简历投递相关接口")
@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
@Validated
public class ApplicationController {

    private final IApplicationService applicationService;
    private final IJobService jobService;
    private final IUserService userService;

    /**
     * 获取当前用户ID
     */
    private Long getCurrentUserId() {
        User currentUser = userService.getCurrentUser();
        return currentUser != null ? currentUser.getId() : null;
    }

    /**
     * 4.8.1 投递简历
     * POST /api/applications
     */
    @Operation(summary = "投递简历", description = "求职者向岗位投递简历")
    @PreAuthorize("hasAnyRole('SEEKER', 'ADMIN')")
    @PostMapping
    public Result<JobApplication> applyJob(@Valid @RequestBody ApplicationCreateRequest request) {
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            return Result.failed("用户未登录");
        }

        JobApplication application = applicationService.applyJob(currentUserId, request);
        return Result.success(Collections.singletonList(application));
    }

    /**
     * 4.8.2 获取我的投递记录
     * GET /api/applications/my
     */
    @Operation(summary = "获取我的投递记录", description = "求职者获取自己的所有投递记录")
    @PreAuthorize("hasAnyRole('SEEKER', 'ADMIN')")
    @GetMapping("/my")
    public Result<ApplicationResponse> getMyApplications(
            @Parameter(description = "状态筛选：0-待查看 1-已查看 2-通过 3-不合适 4-已offer") 
            @RequestParam(required = false) Integer status,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int size) {
        
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            return Result.failed("用户未登录");
        }

        Page<ApplicationResponse> applicationPage = applicationService.getMyApplications(
                currentUserId, status, page, size);
        
        return Result.success(applicationPage.getRecords(), applicationPage.getTotal(), 
                applicationPage.getPages(), applicationPage.getCurrent(), applicationPage.getSize());
    }

    /**
     * 4.8.3 获取岗位收到的投递
     * GET /api/applications/job/{jobId}
     */
    @Operation(summary = "获取岗位收到的投递", description = "HR获取指定岗位收到的所有投递")
    @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
    @GetMapping("/job/{jobId}")
    public Result<ApplicationResponse> getJobApplications(
            @Parameter(description = "岗位ID") @PathVariable Long jobId,
            @Parameter(description = "状态筛选") @RequestParam(required = false) Integer status,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int size) {
        
        // 权限校验：HR只能查看自己发布的岗位的投递
        Long currentUserId = getCurrentUserId();
        if (!SecurityUtils.isAdmin() && !jobService.hasPermission(jobId, currentUserId)) {
            return Result.failed("无权限查看该岗位的投递");
        }

        Page<ApplicationResponse> applicationPage = applicationService.getJobApplications(
                jobId, status, page, size);
        
        return Result.success(applicationPage.getRecords(), applicationPage.getTotal(), 
                applicationPage.getPages(), applicationPage.getCurrent(), applicationPage.getSize());
    }

    /**
     * 4.8.4 处理投递（更新状态）
     * PUT /api/applications/{id}/status
     */
    @Operation(summary = "处理投递", description = "HR更新投递状态和反馈")
    @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
    @PutMapping("/{id}/status")
    public Result<JobApplication> updateApplicationStatus(
            @Parameter(description = "投递ID") @PathVariable Long id,
            @Valid @RequestBody ApplicationStatusRequest request) {
        
        // 权限校验
        Long currentUserId = getCurrentUserId();
        if (!SecurityUtils.isAdmin() && !applicationService.hasPermission(id, currentUserId)) {
            return Result.failed("无权限处理该投递");
        }

        JobApplication application = applicationService.updateApplicationStatus(id, request);
        return Result.success(Collections.singletonList(application));
    }

    /**
     * 获取投递详情
     * GET /api/applications/{id}
     */
    @Operation(summary = "获取投递详情", description = "获取投递记录的详细信息")
    @PreAuthorize("hasAnyRole('SEEKER', 'HR', 'ADMIN')")
    @GetMapping("/{id}")
    public Result<ApplicationResponse> getApplicationDetail(
            @Parameter(description = "投递ID") @PathVariable Long id) {
        
        // 权限校验：求职者只能查看自己的投递，HR只能查看自己岗位的投递
        Long currentUserId = getCurrentUserId();
        ApplicationResponse detail = applicationService.getApplicationDetail(id);
        
        if (!SecurityUtils.isAdmin()) {
            // 求职者校验
            if (SecurityUtils.isSeeker() && !detail.getUserId().equals(currentUserId)) {
                return Result.failed("无权限查看该投递");
            }
            // HR校验
            if (SecurityUtils.isHR() && !applicationService.hasPermission(id, currentUserId)) {
                return Result.failed("无权限查看该投递");
            }
        }

        return Result.success(Collections.singletonList(detail));
    }

    /**
     * 检查是否已投递
     * GET /api/applications/check
     */
    @Operation(summary = "检查是否已投递", description = "检查当前用户是否已投递指定岗位")
    @PreAuthorize("hasAnyRole('SEEKER', 'ADMIN')")
    @GetMapping("/check")
    public Result<Boolean> checkApplied(
            @Parameter(description = "岗位ID") @RequestParam Long jobId) {
        
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            return Result.failed("用户未登录");
        }

        boolean hasApplied = applicationService.hasApplied(currentUserId, jobId);
        return Result.success(Collections.singletonList(hasApplied));
    }

    /**
     * 撤回投递
     * DELETE /api/applications/{id}
     */
    @Operation(summary = "撤回投递", description = "求职者撤回待查看状态的投递")
    @PreAuthorize("hasAnyRole('SEEKER', 'ADMIN')")
    @DeleteMapping("/{id}")
    public Result<Void> withdrawApplication(
            @Parameter(description = "投递ID") @PathVariable Long id) {
        
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            return Result.failed("用户未登录");
        }

        applicationService.withdrawApplication(id, currentUserId);
        return Result.success("撤回成功");
    }

    /**
     * 统计我的投递数据
     * GET /api/applications/my/stats
     */
    @Operation(summary = "统计我的投递数据", description = "获取当前用户的投递统计信息")
    @PreAuthorize("hasAnyRole('SEEKER', 'ADMIN')")
    @GetMapping("/my/stats")
    public Result<ApplicationStatsResponse> getMyApplicationStats() {
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            return Result.failed("用户未登录");
        }

        // 统计各状态的投递数量
        ApplicationStatsResponse stats = new ApplicationStatsResponse();
        stats.setTotalCount(applicationService.count(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<JobApplication>()
                        .eq(JobApplication::getUserId, currentUserId)));
        stats.setPendingCount(applicationService.count(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<JobApplication>()
                        .eq(JobApplication::getUserId, currentUserId)
                        .eq(JobApplication::getApplicationStatus, 0)));
        stats.setViewedCount(applicationService.count(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<JobApplication>()
                        .eq(JobApplication::getUserId, currentUserId)
                        .eq(JobApplication::getApplicationStatus, 1)));
        stats.setPassedCount(applicationService.count(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<JobApplication>()
                        .eq(JobApplication::getUserId, currentUserId)
                        .eq(JobApplication::getApplicationStatus, 2)));
        stats.setRejectedCount(applicationService.count(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<JobApplication>()
                        .eq(JobApplication::getUserId, currentUserId)
                        .eq(JobApplication::getApplicationStatus, 3)));
        stats.setOfferedCount(applicationService.count(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<JobApplication>()
                        .eq(JobApplication::getUserId, currentUserId)
                        .eq(JobApplication::getApplicationStatus, 4)));

        return Result.success(Collections.singletonList(stats));
    }

    /**
     * 投递统计响应内部类
     */
    @lombok.Data
    public static class ApplicationStatsResponse {
        private long totalCount;      // 总投递数
        private long pendingCount;    // 待查看
        private long viewedCount;     // 已查看
        private long passedCount;     // 通过筛选
        private long rejectedCount;   // 不合适
        private long offeredCount;    // 已发offer
    }
}
