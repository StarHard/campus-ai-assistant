package com.campus.ai.controller;
import com.campus.ai.annotation.RequireRole;
import com.campus.ai.dto.LoginRequest;
import com.campus.ai.dto.LoginResponse;
import com.campus.ai.dto.RegisterRequest;
import com.campus.ai.dto.Result;
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
    public Result<User> getUserInfo(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user != null) { user.setPassword(null); user.setSalt(null); }
        return Result.success(user);
    }

    @Operation(summary = "修改密码")
    @RequireRole
    @PutMapping("/{id}/password")
    public Result<Void> changePassword(@PathVariable Long id, @RequestParam String oldPassword, @RequestParam String newPassword) {
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
}
