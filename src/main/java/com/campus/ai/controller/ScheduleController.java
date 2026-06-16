package com.campus.ai.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.ai.annotation.RequireRole;
import com.campus.ai.dao.CourseMapper;
import com.campus.ai.dao.UserMapper;
import com.campus.ai.dto.Result;
import com.campus.ai.entity.Course;
import com.campus.ai.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.*;

@Tag(name = "课表查询")
@RestController
@RequestMapping("/schedule")
public class ScheduleController {

    @Autowired
    private CourseMapper courseMapper;

    @Autowired
    private UserMapper userMapper;

    @Operation(summary = "我的课表（需登录，根据学生院系和年级查询课程）")
    @RequireRole
    @GetMapping("/my")
    public Result<List<Map<String, Object>>> mySchedule(HttpServletRequest request,
                                                         @RequestParam(required = false) Long userId) {
        if (userId == null) {
            userId = (Long) request.getAttribute("userId");
        }
        if (userId == null) {
            return Result.error(400, "无法识别用户");
        }
        User user = userMapper.selectById(userId);
        if (user == null) return Result.success(Collections.emptyList());

        // 学生：查询该院系的课程（模糊匹配学期）
        LambdaQueryWrapper<Course> courseWrapper = new LambdaQueryWrapper<>();
        courseWrapper.eq(Course::getDepartment, user.getDepartment())
                .eq(Course::getStatus, 1);
        if (user.getGrade() != null && !user.getGrade().isEmpty()) {
            courseWrapper.likeRight(Course::getSemester, user.getGrade());
        }
        List<Course> courses = courseMapper.selectList(courseWrapper);

        List<Map<String, Object>> result = new ArrayList<>();
        for (Course c : courses) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("courseId", c.getId());
            item.put("courseCode", c.getCourseCode());
            item.put("courseName", c.getCourseName());
            item.put("teacherName", c.getTeacherName());
            item.put("department", c.getDepartment());
            item.put("credit", c.getCredit());
            item.put("courseHours", c.getCourseHours());
            item.put("courseType", c.getCourseType());
            item.put("semester", c.getSemester());
            item.put("scheduleTime", c.getScheduleTime());
            item.put("description", c.getDescription());
            result.add(item);
        }
        return Result.success(result);
    }
}
