package app.intelligent.resume.controller;

import app.intelligent.resume.common.result.Result;
import app.intelligent.resume.dto.request.BatchMatchRequest;
import app.intelligent.resume.dto.request.MatchRequest;
import app.intelligent.resume.dto.response.CandidateRecommendResponse;
import app.intelligent.resume.dto.response.MatchResponse;
import app.intelligent.resume.entity.User;
import app.intelligent.resume.security.SecurityUtils;
import app.intelligent.resume.service.IMatchService;
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
import java.util.List;

/**
 * 匹配控制器
 * 对应需求文档4.7匹配模块
 *
 * @author Intelligent Resume Team
 */
@Tag(name = "匹配管理", description = "简历与岗位匹配相关接口")
@RestController
@RequestMapping("/api/matches")
@RequiredArgsConstructor
@Validated
public class MatchController {

    private final IMatchService matchService;
    private final IUserService userService;

    /**
     * 获取当前用户ID
     */
    private Long getCurrentUserId() {
        User currentUser = userService.getCurrentUser();
        return currentUser != null ? currentUser.getId() : null;
    }

    /**
     * 4.7.1 简历-岗位匹配
     * POST /api/matches
     */
    @Operation(summary = "简历-岗位匹配", description = "计算简历与岗位的匹配度")
    @PreAuthorize("hasAnyRole('SEEKER', 'HR', 'ADMIN')")
    @PostMapping
    public Result<MatchResponse> match(@Valid @RequestBody MatchRequest request) {
        // 权限校验：求职者只能匹配自己的简历
        Long currentUserId = getCurrentUserId();
        if (!SecurityUtils.isAdmin() && !SecurityUtils.isHR()) {
            if (!matchService.hasPermissionForResume(request.getResumeId(), currentUserId)) {
                return Result.failed("无权限操作该简历");
            }
        }

        MatchResponse response = matchService.match(request);
        return Result.success(Collections.singletonList(response));
    }

    /**
     * 4.7.3 获取简历的匹配记录
     * GET /api/matches/resume/{resumeId}
     */
    @Operation(summary = "获取简历的匹配记录", description = "获取指定简历的所有匹配记录")
    @PreAuthorize("hasAnyRole('SEEKER', 'ADMIN')")
    @GetMapping("/resume/{resumeId}")
    public Result<MatchResponse> getMatchesByResume(
            @Parameter(description = "简历ID") @PathVariable Long resumeId) {
        
        // 权限校验
        Long currentUserId = getCurrentUserId();
        if (!SecurityUtils.isAdmin() && !matchService.hasPermissionForResume(resumeId, currentUserId)) {
            return Result.failed("无权限查看该简历的匹配记录");
        }

        List<MatchResponse> matches = matchService.getMatchesByResume(resumeId);
        return Result.success(matches);
    }

    /**
     * 4.7.4 获取岗位的匹配记录（候选人列表）
     * GET /api/matches/job/{jobId}
     */
    @Operation(summary = "获取岗位的匹配记录", description = "获取指定岗位的所有匹配记录（候选人列表）")
    @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
    @GetMapping("/job/{jobId}")
    public Result<MatchResponse> getMatchesByJob(
            @Parameter(description = "岗位ID") @PathVariable Long jobId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int size) {
        
        // 权限校验
        Long currentUserId = getCurrentUserId();
        if (!SecurityUtils.isAdmin() && !matchService.hasPermissionForJob(jobId, currentUserId)) {
            return Result.failed("无权限查看该岗位的匹配记录");
        }

        Page<MatchResponse> matchPage = matchService.getMatchesByJob(jobId, page, size);
        return Result.success(matchPage.getRecords(), matchPage.getTotal(), matchPage.getPages(),
                matchPage.getCurrent(), matchPage.getSize());
    }

    /**
     * 4.7.5 批量匹配
     * POST /api/matches/batch
     */
    @Operation(summary = "批量匹配", description = "将岗位与多份简历进行批量匹配")
    @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
    @PostMapping("/batch")
    public Result<MatchResponse> batchMatch(@Valid @RequestBody BatchMatchRequest request) {
        // 权限校验
        Long currentUserId = getCurrentUserId();
        if (!SecurityUtils.isAdmin() && !matchService.hasPermissionForJob(request.getJobId(), currentUserId)) {
            return Result.failed("无权限操作该岗位");
        }

        List<MatchResponse> results = matchService.batchMatch(request);
        return Result.success(results);
    }

    /**
     * 4.7.6 智能推荐候选人（AI增强）
     * GET /api/matches/job/{jobId}/recommend
     */
    @Operation(summary = "智能推荐候选人", description = "为岗位智能推荐匹配度高的候选人（AI增强）")
    @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
    @GetMapping("/job/{jobId}/recommend")
    public Result<CandidateRecommendResponse> recommendCandidates(
            @Parameter(description = "岗位ID") @PathVariable Long jobId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int size) {
        
        // 权限校验
        Long currentUserId = getCurrentUserId();
        if (!SecurityUtils.isAdmin() && !matchService.hasPermissionForJob(jobId, currentUserId)) {
            return Result.failed("无权限查看该岗位的推荐候选人");
        }

        Page<CandidateRecommendResponse> recommendPage = matchService.recommendCandidates(jobId, page, size);
        return Result.success(recommendPage.getRecords(), recommendPage.getTotal(), recommendPage.getPages(),
                recommendPage.getCurrent(), recommendPage.getSize());
    }
}
