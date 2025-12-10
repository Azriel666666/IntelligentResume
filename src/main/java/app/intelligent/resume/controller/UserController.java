package app.intelligent.resume.controller;

import app.intelligent.resume.common.result.Result;
import app.intelligent.resume.dto.request.UserCreateRequest;
import app.intelligent.resume.dto.request.UserUpdateRequest;
import app.intelligent.resume.entity.User;
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

    @Operation(summary = "更新用户", description = "更新用户信息")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'SEEKER')")
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

    @Operation(summary = "分页查询用户", description = "分页查询用户列表")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/page")
    public Result<User> pageUsers(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int size) {
        Page<User> userPage = userService.pageUsers(page, size);
        return Result.success(userPage.getRecords());
    }
}
