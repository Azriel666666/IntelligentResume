package app.intelligent.resume.controller;

import app.intelligent.resume.common.result.Result;
import app.intelligent.resume.entity.MatchRecord;
import app.intelligent.resume.service.IMatchRecordService;
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
 * 匹配记录控制器
 *
 * @author Intelligent Resume Team
 */
@Tag(name = "匹配记录管理", description = "简历与岗位匹配记录接口")
@RestController
@RequestMapping("/api/matches")
@RequiredArgsConstructor
@Validated
public class MatchRecordController {

    private final IMatchRecordService matchRecordService;

    @Operation(summary = "创建匹配记录", description = "创建简历与岗位的匹配记录")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'SEEKER')")
    @PostMapping
    public Result<MatchRecord> createMatch(@Valid @RequestBody MatchRecord matchRecord) {
        MatchRecord created = matchRecordService.createMatch(matchRecord);
        return Result.success(Collections.singletonList(created));
    }

    @Operation(summary = "查询简历的匹配记录", description = "根据简历ID查询所有匹配记录")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'SEEKER')")
    @GetMapping("/resume/{resumeId}")
    public Result<MatchRecord> getMatchesByResumeId(
            @Parameter(description = "简历ID") @PathVariable Long resumeId) {
        List<MatchRecord> matches = matchRecordService.listByResumeId(resumeId);
        return Result.success(matches);
    }

    @Operation(summary = "查询岗位的匹配记录", description = "根据岗位ID查询所有匹配记录")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @GetMapping("/job/{jobId}")
    public Result<MatchRecord> getMatchesByJobId(
            @Parameter(description = "岗位ID") @PathVariable Long jobId) {
        List<MatchRecord> matches = matchRecordService.listByJobId(jobId);
        return Result.success(matches);
    }

    @Operation(summary = "查询指定简历和岗位的匹配记录", description = "查询简历与岗位的匹配详情")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'SEEKER')")
    @GetMapping("/resume/{resumeId}/job/{jobId}")
    public Result<MatchRecord> getMatchByResumeAndJob(
            @Parameter(description = "简历ID") @PathVariable Long resumeId,
            @Parameter(description = "岗位ID") @PathVariable Long jobId) {
        MatchRecord match = matchRecordService.getByResumeAndJob(resumeId, jobId);
        return Result.success(Collections.singletonList(match));
    }
}
