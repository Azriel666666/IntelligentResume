package app.intelligent.resume.controller;

import app.intelligent.resume.common.result.Result;
import app.intelligent.resume.dto.request.JobCreateRequest;
import app.intelligent.resume.dto.request.JobQueryRequest;
import app.intelligent.resume.dto.request.JobStatusRequest;
import app.intelligent.resume.dto.response.JobDetailResponse;
import app.intelligent.resume.entity.Job;
import app.intelligent.resume.entity.User;
import app.intelligent.resume.security.SecurityUtils;
import app.intelligent.resume.service.IJobService;
import app.intelligent.resume.service.IUserService;
import cn.hutool.core.bean.BeanUtil;
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
import java.util.List;

/**
 * 岗位控制器
 * 对应需求文档4.6岗位模块
 *
 * @author Intelligent Resume Team
 */
@Tag(name = "岗位管理", description = "岗位CRUD操作接口")
@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
@Validated
public class JobController {

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
     * 4.6.1 发布岗位
     * POST /api/jobs
     */
    @Operation(summary = "发布岗位", description = "HR发布新岗位")
    @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
    @PostMapping
    public Result<Job> createJob(@Valid @RequestBody JobCreateRequest request) {
        Job job = BeanUtil.copyProperties(request, Job.class);
        Job created = jobService.createJob(job);
        return Result.success(Collections.singletonList(created));
    }

    /**
     * 4.6.2 获取岗位列表（带筛选条件）
     * GET /api/jobs
     */
    @Operation(summary = "获取岗位列表", description = "获取招聘中的岗位列表，支持多条件筛选")
    @GetMapping
    public Result<Job> listJobs(JobQueryRequest request) {
        Page<Job> jobPage = jobService.searchJobs(request);
        return Result.success(jobPage.getRecords(), jobPage.getTotal(), jobPage.getPages(), 
                jobPage.getCurrent(), jobPage.getSize());
    }

    /**
     * 4.6.3 获取岗位详情
     * GET /api/jobs/{id}
     */
    @Operation(summary = "获取岗位详情", description = "根据岗位ID获取岗位详情，自动增加浏览量")
    @GetMapping("/{id}")
    public Result<JobDetailResponse> getJobById(
            @Parameter(description = "岗位ID") @PathVariable Long id) {
        // 增加浏览次数
        jobService.incrementViewCount(id);
        
        JobDetailResponse detail = jobService.getJobDetail(id);
        return Result.success(Collections.singletonList(detail));
    }

    /**
     * 4.6.4 更新岗位
     * PUT /api/jobs/{id}
     */
    @Operation(summary = "更新岗位", description = "更新岗位信息，HR只能更新自己发布的岗位")
    @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
    @PutMapping("/{id}")
    public Result<Job> updateJob(
            @Parameter(description = "岗位ID") @PathVariable Long id,
            @Valid @RequestBody JobCreateRequest request) {
        
        // 权限校验：HR只能更新自己发布的岗位
        Long currentUserId = getCurrentUserId();
        if (!SecurityUtils.isAdmin() && !jobService.hasPermission(id, currentUserId)) {
            return Result.failed("无权限修改该岗位");
        }
        
        Job job = BeanUtil.copyProperties(request, Job.class);
        Job updated = jobService.updateJob(id, job);
        return Result.success(Collections.singletonList(updated));
    }

    /**
     * 4.6.5 删除岗位
     * DELETE /api/jobs/{id}
     */
    @Operation(summary = "删除岗位", description = "逻辑删除岗位，HR只能删除自己发布的岗位")
    @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
    @DeleteMapping("/{id}")
    public Result<Void> deleteJob(
            @Parameter(description = "岗位ID") @PathVariable Long id) {
        
        // 权限校验
        Long currentUserId = getCurrentUserId();
        if (!SecurityUtils.isAdmin() && !jobService.hasPermission(id, currentUserId)) {
            return Result.failed("无权限删除该岗位");
        }
        
        jobService.deleteJob(id);
        return Result.success("删除成功");
    }

    /**
     * 4.6.6 上架/下架岗位
     * PUT /api/jobs/{id}/status
     */
    @Operation(summary = "更新岗位状态", description = "上架/下架/暂停岗位")
    @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
    @PutMapping("/{id}/status")
    public Result<Job> updateJobStatus(
            @Parameter(description = "岗位ID") @PathVariable Long id,
            @Valid @RequestBody JobStatusRequest request) {
        
        // 权限校验
        Long currentUserId = getCurrentUserId();
        if (!SecurityUtils.isAdmin() && !jobService.hasPermission(id, currentUserId)) {
            return Result.failed("无权限修改该岗位状态");
        }
        
        Job updated = jobService.updateJobStatus(id, request.getStatus());
        return Result.success(Collections.singletonList(updated));
    }

    /**
     * 4.6.7 HR管理自己的岗位
     * GET /api/jobs/my
     */
    @Operation(summary = "获取我发布的岗位", description = "HR获取自己发布的所有岗位")
    @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
    @GetMapping("/my")
    public Result<Job> getMyJobs(
            @Parameter(description = "状态筛选：0-下架 1-招聘中 2-暂停") @RequestParam(required = false) Integer status,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int size) {
        
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            return Result.failed("用户未登录");
        }
        
        Page<Job> jobPage = jobService.getMyJobs(currentUserId, status, page, size);
        return Result.success(jobPage.getRecords(), jobPage.getTotal(), jobPage.getPages(), 
                jobPage.getCurrent(), jobPage.getSize());
    }

    /**
     * 4.6.8 岗位推荐（求职者）
     * GET /api/jobs/recommend
     */
    @Operation(summary = "推荐岗位", description = "根据求职者简历推荐匹配的岗位")
    @PreAuthorize("hasAnyRole('SEEKER', 'ADMIN')")
    @GetMapping("/recommend")
    public Result<JobDetailResponse> recommendJobs(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int size) {
        
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            return Result.failed("用户未登录");
        }
        
        Page<JobDetailResponse> jobPage = jobService.recommendJobs(currentUserId, page, size);
        return Result.success(jobPage.getRecords(), jobPage.getTotal(), jobPage.getPages(), 
                jobPage.getCurrent(), jobPage.getSize());
    }

    /**
     * 查询招聘中的岗位（简化接口）
     * GET /api/jobs/recruiting
     */
    @Operation(summary = "查询招聘中的岗位", description = "查询所有招聘中的岗位")
    @GetMapping("/recruiting")
    public Result<Job> listRecruitingJobs() {
        List<Job> jobs = jobService.listRecruitingJobs();
        return Result.success(jobs);
    }

    /**
     * 分页查询岗位（简化接口）
     * GET /api/jobs/page
     */
    @Operation(summary = "分页查询岗位", description = "分页查询岗位列表")
    @GetMapping("/page")
    public Result<Job> pageJobs(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int size) {
        Page<Job> jobPage = jobService.pageJobs(page, size);
        return Result.success(jobPage.getRecords(), jobPage.getTotal(), jobPage.getPages(), 
                jobPage.getCurrent(), jobPage.getSize());
    }
}
