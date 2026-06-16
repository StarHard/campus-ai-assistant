package com.campus.ai.controller;
import com.campus.ai.annotation.RequireRole;
import com.campus.ai.dto.LoginRequest;
import com.campus.ai.dto.LoginResponse;
import com.campus.ai.dto.RegisterRequest;
import com.campus.ai.dto.Result;
import com.campus.ai.dto.UpdateUserRequest;
import com.campus.ai.entity.User;
import com.campus.ai.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Tag(name = "用户中心", description = "用户登录、注册、信息管理接口")
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Operation(summary = "用户登录（返回Token）")
    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody LoginRequest request) {
        return Result.success("登录成功", userService.loginWithToken(request));
    }

    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public Result<User> register(@RequestBody RegisterRequest request) {
        return Result.success("注册成功", userService.register(request));
    }

    @Operation(summary = "获取用户信息")
    @RequireRole
    @GetMapping("/{id}")
    public Result<User> getUserInfo(@PathVariable String id) {
        User user = userService.getById(id);
        if (user != null) { user.setPassword(null); user.setSalt(null); }
        return Result.success(user);
    }

    @Operation(summary = "修改密码")
    @RequireRole
    @PutMapping("/{id}/password")
    public Result<Void> changePassword(@PathVariable String id, @RequestParam String oldPassword, @RequestParam String newPassword) {
        userService.changePassword(id, oldPassword, newPassword);
        return Result.success("密码修改成功", null);
    }

    @Operation(summary = "退出登录（使Token失效）")
    @RequireRole
    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader("X-Token") String token) {
        userService.invalidateToken(token);
        return Result.success("已退出登录", null);
    }

    @Operation(summary = "修改用户基本信息")
    @RequireRole
    @PutMapping("/{id}")
    public Result<User> updateUser(@PathVariable String id, @RequestBody UpdateUserRequest request) {
        userService.updateUser(id, request);
        User user = userService.getById(id);
        if (user != null) { user.setPassword(null); user.setSalt(null); }
        return Result.success("更新成功", user);
    }

    @Operation(summary = "注销用户（仅管理员）")
    @RequireRole("ADMIN")
    @DeleteMapping("/{id}")
    public Result<Void> deactivateUser(@PathVariable String id) {
        userService.deactivateUser(id);
        return Result.success("用户已注销", null);
    }
}
