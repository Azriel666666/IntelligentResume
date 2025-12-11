package app.intelligent.resume.controller;

import app.intelligent.resume.common.result.Result;
import app.intelligent.resume.dto.request.*;
import app.intelligent.resume.dto.response.UserResponse;
import app.intelligent.resume.entity.User;
import app.intelligent.resume.service.IFileStorageService;
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
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户控制器
 *
 * @author Intelligent Resume Team
 */
@Tag(name = "用户管理", description = "用户CRUD操作接口")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final IUserService userService;
    private final IFileStorageService fileStorageService;

    // ==================== 当前用户相关接口 ====================

    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的详细信息")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'SEEKER')")
    @GetMapping("/me")
    public Result<UserResponse> getCurrentUser() {
        User user = userService.getCurrentUser();
        UserResponse response = BeanUtil.copyProperties(user, UserResponse.class);
        return Result.success(Collections.singletonList(response));
    }

    @Operation(summary = "更新当前用户信息", description = "更新当前登录用户的个人信息")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'SEEKER')")
    @PutMapping("/me")
    public Result<UserResponse> updateCurrentUser(@Valid @RequestBody UserUpdateRequest request) {
        User updateData = BeanUtil.copyProperties(request, User.class);
        User updated = userService.updateCurrentUser(updateData);
        UserResponse response = BeanUtil.copyProperties(updated, UserResponse.class);
        return Result.success(Collections.singletonList(response));
    }

    @Operation(summary = "修改密码", description = "修改当前用户的登录密码")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'SEEKER')")
    @PutMapping("/me/password")
    public Result<Void> updatePassword(@Valid @RequestBody PasswordUpdateRequest request) {
        userService.updatePassword(request);
        return Result.success("密码修改成功");
    }

    @Operation(summary = "修改手机号", description = "修改当前用户的手机号（需先验证验证码）")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'SEEKER')")
    @PutMapping("/me/phone")
    public Result<Void> updatePhone(@Valid @RequestBody PhoneUpdateRequest request) {
        userService.updatePhone(request);
        return Result.success("手机号修改成功");
    }

    @Operation(summary = "上传头像", description = "上传并更新当前用户的头像")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'SEEKER')")
    @PostMapping("/me/avatar")
    public Result<Map<String, String>> uploadAvatar(
            @Parameter(description = "头像文件") @RequestParam("file") MultipartFile file) {
        User currentUser = userService.getCurrentUser();

        // 验证文件类型
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null ||
            (!originalFilename.toLowerCase().endsWith(".jpg") &&
             !originalFilename.toLowerCase().endsWith(".jpeg") &&
             !originalFilename.toLowerCase().endsWith(".png") &&
             !originalFilename.toLowerCase().endsWith(".gif"))) {
            return Result.failed("只支持 jpg、jpeg、png、gif 格式的图片");
        }

        // 验证文件大小（2MB）
        if (file.getSize() > 2 * 1024 * 1024) {
            return Result.failed("图片大小不能超过2MB");
        }

        // 上传文件到MinIO
        String avatarUrl = fileStorageService.uploadFile(file, "avatar");

        // 更新用户头像
        String savedUrl = userService.updateAvatar(currentUser.getId(), avatarUrl);

        Map<String, String> result = new HashMap<>();
        result.put("avatarUrl", savedUrl);
        return Result.success(Collections.singletonList(result));
    }

    // ==================== 管理员用户管理接口 ====================

    @Operation(summary = "创建用户", description = "管理员创建新用户")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public Result<User> createUser(@Valid @RequestBody UserCreateRequest request) {
        User user = BeanUtil.copyProperties(request, User.class);
        User created = userService.createUser(user);
        return Result.success(Collections.singletonList(created));
    }

    @Operation(summary = "查询用户列表", description = "查询所有用户")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public Result<User> listUsers() {
        List<User> users = userService.listAllUsers();
        return Result.success(users);
    }

    @Operation(summary = "根据ID查询用户", description = "根据用户ID查询用户详情")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'SEEKER')")
    @GetMapping("/{id}")
    public Result<User> getUserById(@Parameter(description = "用户ID") @PathVariable Long id) {
        User user = userService.getById(id);
        return Result.success(Collections.singletonList(user));
    }

    @Operation(summary = "更新用户", description = "管理员更新用户信息")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public Result<User> updateUser(
            @Parameter(description = "用户ID") @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest request) {
        User user = BeanUtil.copyProperties(request, User.class);
        User updated = userService.updateUser(id, user);
        return Result.success(Collections.singletonList(updated));
    }

    @Operation(summary = "删除用户", description = "逻辑删除用户")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public Result<Void> deleteUser(@Parameter(description = "用户ID") @PathVariable Long id) {
        userService.deleteUser(id);
        return Result.success("删除成功");
    }

    @Operation(summary = "禁用/启用用户", description = "更新用户状态")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/status")
    public Result<Void> updateUserStatus(
            @Parameter(description = "用户ID") @PathVariable Long id,
            @Valid @RequestBody UserStatusUpdateRequest request) {
        userService.updateUserStatus(id, request.getStatus());
        String statusText = request.getStatus() == 1 ? "启用" : "禁用";
        return Result.success("用户已" + statusText);
    }

    @Operation(summary = "分页查询用户", description = "分页查询用户列表")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/page")
    public Result<User> pageUsers(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int size) {
        Page<User> userPage = userService.pageUsers(page, size);
        return Result.success(userPage.getRecords());
    }

    @Operation(summary = "条件分页查询用户", description = "根据条件分页查询用户列表")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/search")
    public Result<User> searchUsers(UserQueryRequest request) {
        Page<User> userPage = userService.pageUsers(request);
        return Result.success(userPage.getRecords());
    }
}