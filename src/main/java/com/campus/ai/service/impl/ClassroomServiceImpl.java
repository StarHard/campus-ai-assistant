package com.campus.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.ai.dao.ClassroomMapper;
import com.campus.ai.dao.CourseMapper;
import com.campus.ai.entity.Classroom;
import com.campus.ai.entity.Course;
import com.campus.ai.service.ClassroomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClassroomServiceImpl extends ServiceImpl<ClassroomMapper, Classroom> implements ClassroomService {

    @Autowired
    private CourseMapper courseMapper;

    /** 星期数 → 中文文本 */
    private static final String[] WEEKDAY_NAMES = {"", "周一", "周二", "周三", "周四", "周五", "周六", "周日"};

    @Override
    public List<Classroom> findEmptyClassrooms(Integer weekday, Integer startSection, Integer endSection, String semester) {
        // 查询所有教室
        List<Classroom> allRooms = list();

        // 查询该时间段已被占用的教室名
        LambdaQueryWrapper<Course> courseWrapper = new LambdaQueryWrapper<>();
        courseWrapper.eq(Course::getSemester, semester)
                .like(Course::getScheduleTime, WEEKDAY_NAMES[weekday])
                .isNotNull(Course::getClassroomName)
                .ne(Course::getClassroomName, "");
        List<String> occupiedNames = courseMapper.selectList(courseWrapper).stream()
                .map(Course::getClassroomName)
                .distinct()
                .collect(Collectors.toList());

        return allRooms.stream()
                .filter(room -> !occupiedNames.contains(room.getRoomName()))
                .collect(Collectors.toList());
    }
}
