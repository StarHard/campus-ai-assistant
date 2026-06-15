package com.campus.ai.controller;
import com.campus.ai.annotation.RequireRole;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.ai.dto.PageRequest;
import com.campus.ai.dto.Result;
import com.campus.ai.entity.Reservation;
import com.campus.ai.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Tag(name = "预约管理", description = "第二课堂活动预约与签到接口")
@RestController
@RequestMapping("/reservation")
public class ReservationController {

    @Autowired
    private ReservationService reservationService;

    @Operation(summary = "预约活动")
    @RequireRole
    @PostMapping
    public Result<Reservation> reserve(@RequestParam Long userId, @RequestParam Long activityId) {
        return Result.success("预约成功", reservationService.reserve(userId, activityId));
    }

    @Operation(summary = "取消预约")
    @RequireRole
    @DeleteMapping
    public Result<Void> cancel(@RequestParam Long userId, @RequestParam Long activityId) {
        reservationService.cancelReservation(userId, activityId);
        return Result.success("取消成功", null);
    }

    @Operation(summary = "签到")
    @RequireRole({"ADMIN", "TEACHER"})
    @PutMapping("/{id}/checkin")
    public Result<Void> checkIn(@PathVariable Long id) {
        reservationService.checkIn(id);
        return Result.success("签到成功", null);
    }

    @Operation(summary = "我的预约")
    @RequireRole
    @GetMapping("/my")
    public Result<Page<Reservation>> myReservations(PageRequest pageRequest, @RequestParam Long userId) {
        Page<Reservation> page = new Page<>(pageRequest.getPageNum(), pageRequest.getPageSize());
        LambdaQueryWrapper<Reservation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Reservation::getUserId, userId).orderByDesc(Reservation::getReserveTime);
        return Result.success(reservationService.page(page, wrapper));
    }
}
