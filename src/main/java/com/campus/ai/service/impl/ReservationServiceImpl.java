package com.campus.ai.service.impl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.ai.dao.ActivityMapper;
import com.campus.ai.dao.ReservationMapper;
import com.campus.ai.entity.Activity;
import com.campus.ai.entity.Reservation;
import com.campus.ai.service.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class ReservationServiceImpl extends ServiceImpl<ReservationMapper, Reservation> implements ReservationService {

    @Autowired
    private ActivityMapper activityMapper;

    @Override
    public Reservation reserve(Long userId, Long activityId) {
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null) throw new RuntimeException("活动不存在");
        if (activity.getStatus() != 1) throw new RuntimeException("该活动当前不可报名");

        LambdaQueryWrapper<Reservation> existWrapper = new LambdaQueryWrapper<>();
        existWrapper.eq(Reservation::getUserId, userId).eq(Reservation::getActivityId, activityId);
        Reservation exist = getOne(existWrapper);
        if (exist != null && exist.getStatus() != 2) throw new RuntimeException("您已预约该活动");

        if (activity.getMaxParticipants() != null && activity.getCurrentCount() >= activity.getMaxParticipants())
            throw new RuntimeException("活动名额已满");

        Reservation reservation = new Reservation();
        reservation.setUserId(userId);
        reservation.setActivityId(activityId);
        reservation.setStatus(0);
        reservation.setReserveTime(LocalDateTime.now());
        save(reservation);

        activity.setCurrentCount(activity.getCurrentCount() + 1);
        activityMapper.updateById(activity);
        return reservation;
    }

    @Override
    public void cancelReservation(Long userId, Long activityId) {
        LambdaQueryWrapper<Reservation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Reservation::getUserId, userId).eq(Reservation::getActivityId, activityId);
        Reservation reservation = getOne(wrapper);
        if (reservation == null) throw new RuntimeException("预约记录不存在");
        if (reservation.getStatus() != 0) throw new RuntimeException("当前状态不允许取消");
        reservation.setStatus(2);
        updateById(reservation);

        Activity activity = activityMapper.selectById(activityId);
        if (activity != null && activity.getCurrentCount() > 0) {
            activity.setCurrentCount(activity.getCurrentCount() - 1);
            activityMapper.updateById(activity);
        }
    }

    @Override
    public void checkIn(Long reservationId) {
        Reservation reservation = getById(reservationId);
        if (reservation == null) throw new RuntimeException("预约记录不存在");
        if (reservation.getStatus() != 0) throw new RuntimeException("当前状态不允许签到");
        reservation.setStatus(1);
        updateById(reservation);
    }
}
