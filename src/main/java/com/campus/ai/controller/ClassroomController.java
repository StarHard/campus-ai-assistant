package com.campus.ai.controller;

import com.campus.ai.annotation.RequireRole;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.ai.dto.PageRequest;
import com.campus.ai.dto.Result;
import com.campus.ai.entity.Classroom;
import com.campus.ai.service.ClassroomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "教室管理", description = "教室信息查询与空教室查询接口")
@RestController
@RequestMapping("/classroom")
public class ClassroomController {

    @Autowired
    private ClassroomService classroomService;

    @Operation(summary = "教室列表")
    @RequireRole
    @GetMapping("/list")
    public Result<Page<Classroom>> listClassrooms(PageRequest pageRequest,
                                                  @RequestParam(required = false) String roomName) {
        Page<Classroom> page = new Page<>(pageRequest.getPageNum(), pageRequest.getPageSize());
        LambdaQueryWrapper<Classroom> wrapper = new LambdaQueryWrapper<>();
        if (roomName != null && !roomName.isEmpty()) wrapper.like(Classroom::getRoomName, roomName);
        wrapper.orderByAsc(Classroom::getRoomName);
        return Result.success(classroomService.page(page, wrapper));
    }

    @Operation(summary = "教室详情")
    @RequireRole
    @GetMapping("/{id}")
    public Result<Classroom> getClassroom(@PathVariable Long id) {
        return Result.success(classroomService.getById(id));
    }
}
