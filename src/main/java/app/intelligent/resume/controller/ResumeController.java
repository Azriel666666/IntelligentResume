package app.intelligent.resume.controller;

import app.intelligent.resume.common.result.Result;
import app.intelligent.resume.dto.request.ResumeCreateRequest;
import app.intelligent.resume.entity.Resume;
import app.intelligent.resume.service.IResumeService;
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
 * 简历控制器
 *
 * @author Intelligent Resume Team
 */
@Tag(name = "简历管理", description = "简历CRUD操作接口")
@RestController
@RequestMapping("/api/resumes")
@RequiredArgsConstructor
@Validated
public class ResumeController {

    private final IResumeService resumeService;

    @Operation(summary = "创建简历", description = "求职者创建新简历")
    @PreAuthorize("hasAnyRole('SEEKER', 'ADMIN')")
    @PostMapping
    public Result<Resume> createResume(@Valid @RequestBody ResumeCreateRequest request) {
        Resume resume = BeanUtil.copyProperties(request, Resume.class);
        Resume created = resumeService.createResume(resume);
        return Result.success(Collections.singletonList(created));
    }

    @Operation(summary = "查询简历列表", description = "查询所有简历")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @GetMapping
    public Result<Resume> listResumes() {
        List<Resume> resumes = resumeService.listAllResumes();
        return Result.success(resumes);
    }

    @Operation(summary = "根据ID查询简历", description = "根据简历ID查询简历详情")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'SEEKER')")
    @GetMapping("/{id}")
    public Result<Resume> getResumeById(@Parameter(description = "简历ID") @PathVariable Long id) {
        Resume resume = resumeService.getById(id);
        // 增加查看次数
        resumeService.incrementViewCount(id);
        return Result.success(Collections.singletonList(resume));
    }

    @Operation(summary = "更新简历", description = "更新简历信息")
    @PreAuthorize("hasAnyRole('SEEKER', 'ADMIN')")
    @PutMapping("/{id}")
    public Result<Resume> updateResume(
            @Parameter(description = "简历ID") @PathVariable Long id,
            @Valid @RequestBody ResumeCreateRequest request) {
        Resume resume = BeanUtil.copyProperties(request, Resume.class);
        Resume updated = resumeService.updateResume(id, resume);
        return Result.success(Collections.singletonList(updated));
    }

    @Operation(summary = "删除简历", description = "逻辑删除简历")
    @PreAuthorize("hasAnyRole('SEEKER', 'ADMIN')")
    @DeleteMapping("/{id}")
    public Result<Void> deleteResume(@Parameter(description = "简历ID") @PathVariable Long id) {
        resumeService.deleteResume(id);
        return Result.success("删除成功");
    }

    @Operation(summary = "查询用户的简历", description = "根据用户ID查询其所有简历")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'SEEKER')")
    @GetMapping("/user/{userId}")
    public Result<Resume> getResumesByUserId(@Parameter(description = "用户ID") @PathVariable Long userId) {
        List<Resume> resumes = resumeService.listByUserId(userId);
        return Result.success(resumes);
    }

    @Operation(summary = "分页查询简历", description = "分页查询简历列表")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @GetMapping("/page")
    public Result<Resume> pageResumes(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int size) {
        Page<Resume> resumePage = resumeService.pageResumes(page, size);
        return Result.success(resumePage.getRecords());
    }
}
