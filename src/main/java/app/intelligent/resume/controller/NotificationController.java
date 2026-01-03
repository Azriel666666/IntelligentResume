package app.intelligent.resume.controller;

import app.intelligent.resume.common.result.Result;
import app.intelligent.resume.dto.response.NotificationResponse;
import app.intelligent.resume.entity.User;
import app.intelligent.resume.service.INotificationService;
import app.intelligent.resume.service.IUserService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

/**
 * 通知控制器
 * 对应需求文档4.10.4系统通知
 *
 * @author Intelligent Resume Team
 */
@Tag(name = "系统通知", description = "通知相关接口")
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Validated
public class NotificationController {

    private final INotificationService notificationService;
    private final IUserService userService;

    /**
     * 获取当前用户
     */
    private User getCurrentUser() {
        return userService.getCurrentUser();
    }

    /**
     * 4.10.4 获取系统通知列表
     * GET /api/notifications
     */
    @Operation(summary = "获取通知列表", description = "获取当前用户的通知列表")
    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public Result<NotificationResponse> listNotifications(
            @Parameter(description = "通知类型") @RequestParam(required = false) String type,
            @Parameter(description = "是否已读") @RequestParam(required = false) Boolean isRead,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "20") int size) {
        
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return Result.failed("用户未登录");
        }
        
        Page<NotificationResponse> notificationPage = notificationService.getUserNotifications(
                currentUser.getId(), type, isRead, page, size);
        
        return Result.success(notificationPage.getRecords(), notificationPage.getTotal(), 
                notificationPage.getPages(), notificationPage.getCurrent(), notificationPage.getSize());
    }

    /**
     * 4.10.4 获取未读通知数量
     * GET /api/notifications/unread-count
     */
    @Operation(summary = "获取未读通知数量", description = "获取未读通知总数及按类型统计")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/unread-count")
    public Result<Map<String, Object>> getUnreadCount() {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return Result.failed("用户未登录");
        }
        
        Map<String, Object> unreadCount = notificationService.getUnreadCount(currentUser.getId());
        return Result.success(Collections.singletonList(unreadCount));
    }

    /**
     * 4.10.4 标记通知已读
     * PUT /api/notifications/{id}/read
     */
    @Operation(summary = "标记通知已读", description = "将指定通知标记为已读")
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/{id}/read")
    public Result<Void> markAsRead(
            @Parameter(description = "通知ID") @PathVariable Long id) {
        
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return Result.failed("用户未登录");
        }
        
        notificationService.markAsRead(id, currentUser.getId());
        return Result.success("已标记为已读");
    }

    /**
     * 4.10.4 全部标记已读
     * PUT /api/notifications/read-all
     */
    @Operation(summary = "全部标记已读", description = "将所有通知标记为已读")
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/read-all")
    public Result<Void> markAllAsRead(
            @Parameter(description = "通知类型（可选）") @RequestParam(required = false) String type) {
        
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return Result.failed("用户未登录");
        }
        
        notificationService.markAllAsRead(currentUser.getId(), type);
        return Result.success("已全部标记为已读");
    }

    /**
     * 4.10.4 删除通知
     * DELETE /api/notifications/{id}
     */
    @Operation(summary = "删除通知", description = "删除指定通知")
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{id}")
    public Result<Void> deleteNotification(
            @Parameter(description = "通知ID") @PathVariable Long id) {
        
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return Result.failed("用户未登录");
        }
        
        notificationService.deleteNotification(id, currentUser.getId());
        return Result.success("删除成功");
    }
}
