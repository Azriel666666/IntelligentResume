package app.intelligent.resume.controller;

import app.intelligent.resume.common.exception.BusinessException;
import app.intelligent.resume.common.result.Result;
import app.intelligent.resume.common.result.ResultCode;
import app.intelligent.resume.dto.request.ResumeOnlineCreateRequest;
import app.intelligent.resume.dto.request.ResumeSearchRequest;
import app.intelligent.resume.dto.request.ResumeUpdateRequest;
import app.intelligent.resume.dto.response.ResumeDetailResponse;
import app.intelligent.resume.dto.response.ResumeUploadResponse;
import app.intelligent.resume.entity.Resume;
import app.intelligent.resume.entity.User;
import app.intelligent.resume.security.SecurityUtils;
import app.intelligent.resume.service.IResumeService;
import app.intelligent.resume.service.IUserService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 简历控制器
 * 对应需求文档4.3简历模块
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
    private final IUserService userService;

    /**
     * 4.3.1 上传简历文件
     * 上传简历文件（PDF/Word）并自动解析
     */
    @Operation(summary = "上传简历文件", description = "上传简历文件（PDF/Word）并自动解析")
    @PreAuthorize("hasAnyRole('SEEKER', 'ADMIN')")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<ResumeUploadResponse> uploadResume(
            @Parameter(description = "简历文件（支持PDF、DOC、DOCX，最大10MB）")
            @RequestParam("file") MultipartFile file) {
        ResumeUploadResponse response = resumeService.uploadResume(file);
        return Result.success(Collections.singletonList(response));
    }

    /**
     * 4.3.2 在线创建简历
     * 在线填写创建简历
     */
    @Operation(summary = "在线创建简历", description = "在线填写创建简历")
    @PreAuthorize("hasAnyRole('SEEKER', 'ADMIN')")
    @PostMapping
    public Result<ResumeDetailResponse> createResumeOnline(
            @Valid @RequestBody ResumeOnlineCreateRequest request) {
        ResumeDetailResponse response = resumeService.createResumeOnline(request);
        return Result.success(Collections.singletonList(response));
    }

    /**
     * 4.3.3 获取简历列表
     * 获取当前用户的所有简历
     */
    @Operation(summary = "获取我的简历列表", description = "获取当前用户的所有简历")
    @PreAuthorize("hasRole('SEEKER')")
    @GetMapping("/my")
    public Result<Resume> listMyResumes() {
        List<Resume> resumes = resumeService.listMyResumes();
        return Result.success(resumes);
    }

    /**
     * 4.3.4 获取简历详情
     * 获取简历完整信息，包括详情和分析报告
     */
    @Operation(summary = "获取简历详情", description = "获取简历完整信息，包括详情和分析报告")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'SEEKER')")
    @GetMapping("/{id}")
    public Result<ResumeDetailResponse> getResumeDetail(
            @Parameter(description = "简历ID") @PathVariable Long id) {
        ResumeDetailResponse response = resumeService.getResumeDetail(id);
        // 增加查看次数
        resumeService.incrementViewCount(id);
        return Result.success(Collections.singletonList(response));
    }

    /**
     * 4.3.5 更新简历
     * 更新简历信息
     */
    @Operation(summary = "更新简历", description = "更新简历信息")
    @PreAuthorize("hasAnyRole('SEEKER', 'ADMIN')")
    @PutMapping("/{id}")
    public Result<ResumeDetailResponse> updateResume(
            @Parameter(description = "简历ID") @PathVariable Long id,
            @Valid @RequestBody ResumeUpdateRequest request) {
        ResumeDetailResponse response = resumeService.updateResume(id, request);
        return Result.success(Collections.singletonList(response));
    }

    /**
     * 4.3.6 删除简历
     * 逻辑删除简历
     */
    @Operation(summary = "删除简历", description = "逻辑删除简历")
    @PreAuthorize("hasAnyRole('SEEKER', 'ADMIN')")
    @DeleteMapping("/{id}")
    public Result<Void> deleteResume(
            @Parameter(description = "简历ID") @PathVariable Long id) {
        resumeService.deleteResume(id);
        return Result.success("删除成功");
    }

    /**
     * 4.3.7 设置默认简历
     * 将指定简历设为默认简历
     */
    @Operation(summary = "设置默认简历", description = "将指定简历设为默认简历")
    @PreAuthorize("hasRole('SEEKER')")
    @PutMapping("/{id}/default")
    public Result<Void> setDefaultResume(
            @Parameter(description = "简历ID") @PathVariable Long id) {
        resumeService.setDefaultResume(id);
        return Result.success("设置成功");
    }

    /**
     * 4.3.8 简历库搜索（HR）
     * 搜索简历库
     */
    @Operation(summary = "简历库搜索", description = "HR搜索简历库")
    @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
    @GetMapping("/search")
    public Result<Object> searchResumes(ResumeSearchRequest request) {
        Page<ResumeDetailResponse> page = resumeService.searchResumes(request);
        
        // 构建分页响应
        Map<String, Object> pageData = new HashMap<>();
        pageData.put("records", page.getRecords());
        pageData.put("total", page.getTotal());
        pageData.put("page", page.getCurrent());
        pageData.put("size", page.getSize());
        pageData.put("pages", page.getPages());
        
        return Result.success(Collections.singletonList(pageData));
    }

    /**
     * 4.3.9 下载简历
     * 下载简历原文件（通过后端代理流式下载）
     */
    @Operation(summary = "下载简历", description = "下载简历原文件")
    @PreAuthorize("hasAnyRole('HR', 'ADMIN', 'SEEKER')")
    @GetMapping("/{id}/download")
    public void downloadResume(
            @Parameter(description = "简历ID") @PathVariable Long id,
            HttpServletResponse response) {
        // 求职者只能下载自己的简历
        Resume resume = resumeService.getById(id);
        if (resume == null) {
            throw new BusinessException(ResultCode.RESUME_NOT_EXIST);
        }
        
        User currentUser = userService.getCurrentUser();
        if (currentUser != null && SecurityUtils.isSeeker()) {
            if (!resume.getUserId().equals(currentUser.getId())) {
                throw new BusinessException(ResultCode.RESUME_NO_PERMISSION);
            }
        }
        
        // 调用服务层进行文件下载
        resumeService.downloadResumeFile(id, response);
    }

    // ========== 以下为管理接口，保持兼容 ==========

    /**
     * 查询所有简历列表（管理员/HR）
     */
    @Operation(summary = "查询简历列表", description = "查询所有简历（管理员/HR专用）")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @GetMapping
    public Result<Resume> listResumes() {
        List<Resume> resumes = resumeService.listAllResumes();
        return Result.success(resumes);
    }

    /**
     * 根据用户ID查询简历
     */
    @Operation(summary = "查询用户的简历", description = "根据用户ID查询其所有简历")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'SEEKER')")
    @GetMapping("/user/{userId}")
    public Result<Resume> getResumesByUserId(
            @Parameter(description = "用户ID") @PathVariable Long userId) {
        List<Resume> resumes = resumeService.listByUserId(userId);
        return Result.success(resumes);
    }

    /**
     * 分页查询简历
     */
    @Operation(summary = "分页查询简历", description = "分页查询简历列表")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @GetMapping("/page")
    public Result<Object> pageResumes(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int size) {
        Page<Resume> resumePage = resumeService.pageResumes(page, size);
        
        // 构建分页响应
        Map<String, Object> pageData = new HashMap<>();
        pageData.put("records", resumePage.getRecords());
        pageData.put("total", resumePage.getTotal());
        pageData.put("page", resumePage.getCurrent());
        pageData.put("size", resumePage.getSize());
        pageData.put("pages", resumePage.getPages());
        
        return Result.success(Collections.singletonList(pageData));
    }
}
