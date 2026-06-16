package com.campus.ai.controller;

import com.campus.ai.annotation.RequireRole;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.ai.dto.Result;
import com.campus.ai.entity.LoginLog;
import com.campus.ai.service.LoginLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Tag(name = "登录日志")
@RestController
@RequestMapping("/log/login")
public class LoginLogController {

    @Autowired
    private LoginLogService loginLogService;

    @Operation(summary = "查询登录日志（分页）")
    @RequireRole("ADMIN")
    @GetMapping("/list")
    public Result<IPage<LoginLog>> list(@RequestParam(required = false) String userId,
                                        @RequestParam(defaultValue = "1") Integer page,
                                        @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(loginLogService.queryLogs(userId, page, size));
    }
}
