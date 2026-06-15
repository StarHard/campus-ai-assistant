package com.campus.ai.controller;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.ai.dto.PageRequest;
import com.campus.ai.dto.Result;
import com.campus.ai.entity.Course;
import com.campus.ai.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Tag(name = "课程管理", description = "课程信息查询与管理接口")
@RestController
@RequestMapping("/course")
public class CourseController {

    @Autowired
    private CourseService courseService;

    @Operation(summary = "分页查询课程")
    @GetMapping("/list")
    public Result<Page<Course>> listCourses(PageRequest pageRequest,
                                           @RequestParam(required = false) String department,
                                           @RequestParam(required = false) String semester,
                                           @RequestParam(required = false) Integer courseType) {
        Page<Course> page = new Page<>(pageRequest.getPageNum(), pageRequest.getPageSize());
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();
        if (department != null) wrapper.eq(Course::getDepartment, department);
        if (semester != null) wrapper.eq(Course::getSemester, semester);
        if (courseType != null) wrapper.eq(Course::getCourseType, courseType);
        wrapper.orderByDesc(Course::getCreateTime);
        return Result.success(courseService.page(page, wrapper));
    }

    @Operation(summary = "课程详情")
    @GetMapping("/{id}")
    public Result<Course> getCourse(@PathVariable Long id) {
        return Result.success(courseService.getById(id));
    }

    @Operation(summary = "新增课程")
    @PostMapping
    public Result<Course> addCourse(@RequestBody Course course) {
        courseService.save(course);
        return Result.success("添加成功", course);
    }

    @Operation(summary = "修改课程")
    @PutMapping("/{id}")
    public Result<Course> updateCourse(@PathVariable Long id, @RequestBody Course course) {
        course.setId(id);
        courseService.updateById(course);
        return Result.success("修改成功", course);
    }

    @Operation(summary = "删除课程")
    @DeleteMapping("/{id}")
    public Result<Void> deleteCourse(@PathVariable Long id) {
        courseService.removeById(id);
        return Result.success("删除成功", null);
    }
}
