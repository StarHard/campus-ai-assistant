package com.campus.ai.controller;
import com.campus.ai.annotation.RequireRole;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.ai.dto.PageRequest;
import com.campus.ai.dto.Result;
import com.campus.ai.entity.Activity;
import com.campus.ai.service.ActivityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Tag(name = "活动管理", description = "校园活动发布与查询接口")
@RestController
@RequestMapping("/activity")
public class ActivityController {

    @Autowired
    private ActivityService activityService;

    @Operation(summary = "活动列表")
    @RequireRole
    @GetMapping("/list")
    public Result<Page<Activity>> listActivities(PageRequest pageRequest,
                                                 @RequestParam(required = false) Integer activityType,
                                                 @RequestParam(required = false) Integer status) {
        Page<Activity> page = new Page<>(pageRequest.getPageNum(), pageRequest.getPageSize());
        LambdaQueryWrapper<Activity> wrapper = new LambdaQueryWrapper<>();
        if (activityType != null) wrapper.eq(Activity::getActivityType, activityType);
        if (status != null) wrapper.eq(Activity::getStatus, status);
        else wrapper.ge(Activity::getStatus, 1);
        wrapper.orderByDesc(Activity::getStartTime);
        return Result.success(activityService.page(page, wrapper));
    }

    @Operation(summary = "活动详情")
    @RequireRole
    @GetMapping("/{id}")
    public Result<Activity> getActivity(@PathVariable Long id) {
        return Result.success(activityService.getById(id));
    }

    @Operation(summary = "发布活动（教师及以上）")
    @RequireRole({"ADMIN", "TEACHER"})
    @PostMapping
    public Result<Activity> addActivity(@RequestBody Activity activity) {
        activity.setStatus(1);
        activity.setCurrentCount(0);
        activityService.save(activity);
        return Result.success("发布成功", activity);
    }

    @Operation(summary = "修改活动")
    @RequireRole({"ADMIN", "TEACHER"})
    @PutMapping("/{id}")
    public Result<Activity> updateActivity(@PathVariable Long id, @RequestBody Activity activity) {
        activity.setId(id);
        activityService.updateById(activity);
        return Result.success("修改成功", activity);
    }

    @Operation(summary = "取消活动")
    @RequireRole({"ADMIN", "TEACHER"})
    @DeleteMapping("/{id}")
    public Result<Void> deleteActivity(@PathVariable Long id) {
        activityService.removeById(id);
        return Result.success("取消成功", null);
    }
}
