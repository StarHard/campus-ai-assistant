package com.campus.ai.service.impl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.ai.dao.ClassroomMapper;
import com.campus.ai.dao.ScheduleMapper;
import com.campus.ai.entity.Classroom;
import com.campus.ai.entity.Schedule;
import com.campus.ai.service.ClassroomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClassroomServiceImpl extends ServiceImpl<ClassroomMapper, Classroom> implements ClassroomService {

    @Autowired
    private ScheduleMapper scheduleMapper;

    @Override
    public List<Classroom> findEmptyClassrooms(Integer weekday, Integer startSection, Integer endSection, String semester) {
        LambdaQueryWrapper<Classroom> roomWrapper = new LambdaQueryWrapper<>();
        roomWrapper.eq(Classroom::getStatus, 1);
        List<Classroom> allRooms = list(roomWrapper);

        LambdaQueryWrapper<Schedule> scheduleWrapper = new LambdaQueryWrapper<>();
        scheduleWrapper.eq(Schedule::getWeekday, weekday)
                .eq(Schedule::getSemester, semester)
                .and(w -> w.le(Schedule::getStartSection, endSection).ge(Schedule::getEndSection, startSection));
        List<Long> occupiedRoomIds = scheduleMapper.selectList(scheduleWrapper).stream()
                .map(Schedule::getClassroomId).distinct().collect(Collectors.toList());

        return allRooms.stream().filter(room -> !occupiedRoomIds.contains(room.getId())).collect(Collectors.toList());
    }
}
