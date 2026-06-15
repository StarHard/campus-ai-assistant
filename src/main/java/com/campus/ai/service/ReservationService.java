package com.campus.ai.service;
import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.ai.entity.Reservation;

public interface ReservationService extends IService<Reservation> {
    Reservation reserve(Long userId, Long activityId);
    void cancelReservation(Long userId, Long activityId);
    void checkIn(Long reservationId);
}
