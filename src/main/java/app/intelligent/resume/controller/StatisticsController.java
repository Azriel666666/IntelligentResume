package app.intelligent.resume.controller;

import app.intelligent.resume.common.result.Result;
import app.intelligent.resume.dto.response.AdminStatisticsResponse;
import app.intelligent.resume.dto.response.HrStatisticsResponse;
import app.intelligent.resume.dto.response.SeekerStatisticsResponse;
import app.intelligent.resume.entity.User;
import app.intelligent.resume.service.IStatisticsService;
import app.intelligent.resume.service.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;

/**
 * 数据统计控制器
 * 对应需求文档4.11数据统计模块
 *
 * @author Intelligent Resume Team
 */
@Tag(name = "数据统计", description = "数据统计相关接口")
@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
@Validated
public class StatisticsController {

    private final IStatisticsService statisticsService;
    private final IUserService userService;

    /**
     * 获取当前用户
     */
    private User getCurrentUser() {
        return userService.getCurrentUser();
    }

    /**
     * 4.11.1 求职者统计
     * GET /api/statistics/seeker
     */
    @Operation(summary = "求职者统计", description = "获取求职者的统计数据")
    @PreAuthorize("hasAnyRole('SEEKER', 'ADMIN')")
    @GetMapping("/seeker")
    public Result<SeekerStatisticsResponse> getSeekerStatistics() {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return Result.failed("用户未登录");
        }
        
        SeekerStatisticsResponse statistics = statisticsService.getSeekerStatistics(currentUser.getId());
        return Result.success(Collections.singletonList(statistics));
    }

    /**
     * 4.11.2 HR统计
     * GET /api/statistics/hr
     */
    @Operation(summary = "HR统计", description = "获取HR的统计数据")
    @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
    @GetMapping("/hr")
    public Result<HrStatisticsResponse> getHrStatistics() {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return Result.failed("用户未登录");
        }
        
        HrStatisticsResponse statistics = statisticsService.getHrStatistics(currentUser.getId());
        return Result.success(Collections.singletonList(statistics));
    }

    /**
     * 4.11.3 管理员统计
     * GET /api/statistics/admin
     */
    @Operation(summary = "管理员统计", description = "获取系统整体统计数据")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin")
    public Result<AdminStatisticsResponse> getAdminStatistics() {
        AdminStatisticsResponse statistics = statisticsService.getAdminStatistics();
        return Result.success(Collections.singletonList(statistics));
    }
}
