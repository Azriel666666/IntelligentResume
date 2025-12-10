package app.intelligent.resume.controller;

import app.intelligent.resume.common.result.Result;
import app.intelligent.resume.dto.request.JobCreateRequest;
import app.intelligent.resume.entity.Job;
import app.intelligent.resume.service.IJobService;
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

    @Operation(summary = "创建岗位", description = "HR创建新岗位")
    @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
    @PostMapping
    public Result<Job> createJob(@Valid @RequestBody JobCreateRequest request) {
        Job job = BeanUtil.copyProperties(request, Job.class);
        Job created = jobService.createJob(job);
        return Result.success(Collections.singletonList(created));
    }

    @Operation(summary = "查询岗位列表", description = "查询所有岗位")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'SEEKER')")
    @GetMapping
    public Result<Job> listJobs() {
        List<Job> jobs = jobService.listAllJobs();
        return Result.success(jobs);
    }

    @Operation(summary = "根据ID查询岗位", description = "根据岗位ID查询岗位详情")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'SEEKER')")
    @GetMapping("/{id}")
    public Result<Job> getJobById(@Parameter(description = "岗位ID") @PathVariable Long id) {
        Job job = jobService.getById(id);
        // 增加浏览次数
        jobService.incrementViewCount(id);
        return Result.success(Collections.singletonList(job));
    }

    @Operation(summary = "更新岗位", description = "更新岗位信息")
    @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
    @PutMapping("/{id}")
    public Result<Job> updateJob(
            @Parameter(description = "岗位ID") @PathVariable Long id,
            @Valid @RequestBody JobCreateRequest request) {
        Job job = BeanUtil.copyProperties(request, Job.class);
        Job updated = jobService.updateJob(id, job);
        return Result.success(Collections.singletonList(updated));
    }

    @Operation(summary = "删除岗位", description = "逻辑删除岗位")
    @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
    @DeleteMapping("/{id}")
    public Result<Void> deleteJob(@Parameter(description = "岗位ID") @PathVariable Long id) {
        jobService.deleteJob(id);
        return Result.success("删除成功");
    }

    @Operation(summary = "查询招聘中的岗位", description = "查询所有招聘中的岗位")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'SEEKER')")
    @GetMapping("/recruiting")
    public Result<Job> listRecruitingJobs() {
        List<Job> jobs = jobService.listRecruitingJobs();
        return Result.success(jobs);
    }

    @Operation(summary = "分页查询岗位", description = "分页查询岗位列表")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'SEEKER')")
    @GetMapping("/page")
    public Result<Job> pageJobs(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int size) {
        Page<Job> jobPage = jobService.pageJobs(page, size);
        return Result.success(jobPage.getRecords());
    }
}
