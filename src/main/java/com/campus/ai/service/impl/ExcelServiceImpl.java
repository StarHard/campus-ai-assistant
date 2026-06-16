package com.campus.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.ai.dao.CourseMapper;
import com.campus.ai.dao.UserRoleMapper;
import com.campus.ai.dto.RegisterRequest;
import com.campus.ai.entity.Course;
import com.campus.ai.entity.User;
import com.campus.ai.service.ExcelService;
import com.campus.ai.service.UserService;
import com.campus.ai.util.ExcelUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ExcelServiceImpl implements ExcelService {

    @Autowired
    private CourseMapper courseMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRoleMapper userRoleMapper;

    /** 课程类型映射 */
    private static String getCourseTypeName(Integer type) {
        if (type == null) return "未知";
        switch (type) {
            case 1: return "必修课";
            case 2: return "选修课";
            case 3: return "公选课";
            default: return "未知";
        }
    }

    /** 考试类型映射 */
    private static String getExamTypeName(Integer type) {
        if (type == null) return "未知";
        switch (type) {
            case 1: return "闭卷考试";
            case 2: return "开卷考试";
            case 3: return "考查";
            default: return "未知";
        }
    }

    @Override
    public byte[] exportCourses() {
        try {
            LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();
            wrapper.orderByAsc(Course::getCourseCode);
            List<Course> courses = courseMapper.selectList(wrapper);

            // 表头
            List<String> headers = new ArrayList<>();
            headers.add("课程编号");
            headers.add("课程名称");
            headers.add("授课教师");
            headers.add("所属院系");
            headers.add("学分");
            headers.add("总学时");
            headers.add("理论学时");
            headers.add("实践学时");
            headers.add("课程类型");
            headers.add("考核方式");
            headers.add("学期");
            headers.add("课程描述");
            headers.add("上课时间");
            headers.add("状态");
            headers.add("创建时间");

            // 数据行
            List<List<Object>> data = new ArrayList<>();
            for (Course course : courses) {
                List<Object> row = new ArrayList<>();
                row.add(course.getCourseCode());
                row.add(course.getCourseName());
                row.add(course.getTeacherName());
                row.add(course.getDepartment());
                row.add(course.getCredit());
                row.add(course.getCourseHours());
                row.add(course.getTheoryHours());
                row.add(course.getPracticeHours());
                row.add(getCourseTypeName(course.getCourseType()));
                row.add(getExamTypeName(course.getExamType()));
                row.add(course.getSemester());
                row.add(course.getDescription());
                row.add(course.getScheduleTime());
                row.add(course.getStatus() != null && course.getStatus() == 1 ? "启用" : "禁用");
                row.add(course.getCreateTime());
                data.add(row);
            }

            return ExcelUtil.exportToExcel(headers, data, "课程列表");

        } catch (Exception e) {
            throw new RuntimeException("导出课程列表失败: " + e.getMessage(), e);
        }
    }

    @Override
    public byte[] downloadStudentTemplate() {
        try {
            List<String> headers = new ArrayList<>();
            headers.add("学号");
            headers.add("姓名");
            headers.add("性别");
            headers.add("手机号");
            headers.add("邮箱");
            headers.add("院系");
            headers.add("专业");
            headers.add("年级");

            return ExcelUtil.exportToExcel(headers, new ArrayList<>(), "学生导入模板");

        } catch (Exception e) {
            throw new RuntimeException("生成学生模板失败: " + e.getMessage(), e);
        }
    }

    @Override
    public byte[] downloadCourseTemplate() {
        try {
            List<String> headers = new ArrayList<>();
            headers.add("课程编号");
            headers.add("课程名称");
            headers.add("教师工号");
            headers.add("教师姓名");
            headers.add("院系");
            headers.add("学分");
            headers.add("总学时");
            headers.add("理论学时");
            headers.add("实践学时");
            headers.add("课程类型(1必修/2选修/3公选)");
            headers.add("考核方式(1闭卷/2开卷/3考查)");
            headers.add("学期");
            headers.add("课程描述");
            headers.add("上课时间");

            return ExcelUtil.exportToExcel(headers, new ArrayList<>(), "课程导入模板");

        } catch (Exception e) {
            throw new RuntimeException("生成课程模板失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String importStudents(MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                return "错误：上传文件不能为空";
            }

            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || (!originalFilename.endsWith(".xlsx") && !originalFilename.endsWith(".xls"))) {
                return "错误：仅支持.xlsx或.xls格式的Excel文件";
            }

            List<Map<Integer, String>> rows = ExcelUtil.importFromExcel(file.getInputStream());

            if (rows.isEmpty()) {
                return "错误：Excel文件中没有数据";
            }

            int successCount = 0;
            int failCount = 0;
            StringBuilder failReasons = new StringBuilder();

            // 跳过表头行，从第二行开始处理数据
            for (int i = 1; i < rows.size(); i++) {
                Map<Integer, String> rowData = rows.get(i);
                try {
                    String studentNo = rowData.getOrDefault(0, "").trim();
                    String realName = rowData.getOrDefault(1, "").trim();
                    String genderStr = rowData.getOrDefault(2, "").trim();
                    String phone = rowData.getOrDefault(3, "").trim();
                    String email = rowData.getOrDefault(4, "").trim();
                    String department = rowData.getOrDefault(5, "").trim();
                    String major = rowData.getOrDefault(6, "").trim();
                    String grade = rowData.getOrDefault(7, "").trim();

                    if (studentNo.isEmpty() || realName.isEmpty()) {
                        failCount++;
                        failReasons.append("第").append(i + 1).append("行: 学号或姓名为空; ");
                        continue;
                    }

                    RegisterRequest request = new RegisterRequest();
                    request.setUsername(studentNo);
                    request.setPassword("123456"); // 默认密码
                    request.setRealName(realName);
                    request.setUserType(2); // 学生
                    request.setGender("男".equals(genderStr) ? 1 : ("女".equals(genderStr) ? 2 : 0));
                    request.setPhone(phone.isEmpty() ? null : phone);
                    request.setEmail(email.isEmpty() ? null : email);
                    request.setDepartment(department.isEmpty() ? null : department);
                    request.setMajor(major.isEmpty() ? null : major);
                    request.setGrade(grade.isEmpty() ? null : grade);

                    userService.register(request);
                    successCount++;

                } catch (Exception ex) {
                    failCount++;
                    failReasons.append("第").append(i + 1).append("行: ").append(ex.getMessage()).append("; ");
                }
            }

            String result = "导入完成！成功: " + successCount + " 条, 失败: " + failCount + " 条。";
            if (failCount > 0) {
                result += " 失败原因: " + failReasons.toString();
            }
            return result;

        } catch (Exception e) {
            return "导入学生失败: " + e.getMessage();
        }
    }

    @Override
    public String importCourses(MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                return "错误：上传文件不能为空";
            }

            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || (!originalFilename.endsWith(".xlsx") && !originalFilename.endsWith(".xls"))) {
                return "错误：仅支持.xlsx或.xls格式的Excel文件";
            }

            List<Map<Integer, String>> rows = ExcelUtil.importFromExcel(file.getInputStream());

            if (rows.isEmpty()) {
                return "错误：Excel文件中没有数据";
            }

            int successCount = 0;
            int failCount = 0;
            StringBuilder failReasons = new StringBuilder();

            // 跳过表头行，从第二行开始处理数据
            for (int i = 1; i < rows.size(); i++) {
                Map<Integer, String> rowData = rows.get(i);
                try {
                    String courseCode = rowData.getOrDefault(0, "").trim();
                    String courseName = rowData.getOrDefault(1, "").trim();
                    String teacherNo = rowData.getOrDefault(2, "").trim();
                    String teacherName = rowData.getOrDefault(3, "").trim();
                    String department = rowData.getOrDefault(4, "").trim();
                    String creditStr = rowData.getOrDefault(5, "").trim();
                    String hoursStr = rowData.getOrDefault(6, "").trim();
                    String theoryHoursStr = rowData.getOrDefault(7, "").trim();
                    String practiceHoursStr = rowData.getOrDefault(8, "").trim();
                    String courseTypeStr = rowData.getOrDefault(9, "").trim();
                    String examTypeStr = rowData.getOrDefault(10, "").trim();
                    String semester = rowData.getOrDefault(11, "").trim();
                    String description = rowData.getOrDefault(12, "").trim();
                    String scheduleTime = rowData.size() > 13 ? rowData.getOrDefault(13, "").trim() : "";

                    if (courseCode.isEmpty() || courseName.isEmpty()) {
                        failCount++;
                        failReasons.append("第").append(i + 1).append("行: 课程编号或课程名称为空; ");
                        continue;
                    }

                    // 教师工号必填，且必须在数据库中存在
                    if (teacherNo.isEmpty()) {
                        failCount++;
                        failReasons.append("第").append(i + 1).append("行: 教师工号为空; ");
                        continue;
                    }
                    LambdaQueryWrapper<com.campus.ai.entity.User> teacherWrapper =
                            new LambdaQueryWrapper<>();
                    teacherWrapper.eq(com.campus.ai.entity.User::getUsername, teacherNo)
                            .eq(com.campus.ai.entity.User::getUserType, 1);
                    com.campus.ai.entity.User teacher = userService.getOne(teacherWrapper);
                    if (teacher == null) {
                        failCount++;
                        failReasons.append("第").append(i + 1).append("行: 教师工号").append(teacherNo).append("不存在; ");
                        continue;
                    }

                    Course course = new Course();
                    course.setCourseCode(courseCode);
                    course.setCourseName(courseName);
                    course.setTeacherName(teacherName.isEmpty() ? null : teacherName);
                    course.setTeacherId(teacher.getId());
                    course.setDepartment(department.isEmpty() ? null : department);
                    course.setCredit(creditStr.isEmpty() ? null : new java.math.BigDecimal(creditStr));
                    course.setCourseHours(hoursStr.isEmpty() ? null : Integer.parseInt(hoursStr));
                    course.setTheoryHours(theoryHoursStr.isEmpty() ? null : Integer.parseInt(theoryHoursStr));
                    course.setPracticeHours(practiceHoursStr.isEmpty() ? null : Integer.parseInt(practiceHoursStr));
                    course.setCourseType(courseTypeStr.isEmpty() ? null : Integer.parseInt(courseTypeStr));
                    course.setExamType(examTypeStr.isEmpty() ? null : Integer.parseInt(examTypeStr));
                    course.setSemester(semester.isEmpty() ? null : semester);
                    course.setDescription(description.isEmpty() ? null : description);
                    course.setScheduleTime(scheduleTime.isEmpty() ? null : scheduleTime);
                    course.setStatus(1);

                    courseMapper.insert(course);
                    successCount++;

                } catch (NumberFormatException ex) {
                    failCount++;
                    failReasons.append("第").append(i + 1).append("行: 数字格式错误 - ").append(ex.getMessage()).append("; ");
                } catch (Exception ex) {
                    failCount++;
                    failReasons.append("第").append(i + 1).append("行: ").append(ex.getMessage()).append("; ");
                }
            }

            String result = "导入完成！成功: " + successCount + " 条, 失败: " + failCount + " 条。";
            if (failCount > 0) {
                result += " 失败原因: " + failReasons.toString();
            }
            return result;

        } catch (Exception e) {
            return "导入课程失败: " + e.getMessage();
        }
    }

    @Override
    public byte[] downloadTeacherTemplate() {
        try {
            List<String> headers = new ArrayList<>();
            headers.add("工号");
            headers.add("姓名");
            headers.add("性别");
            headers.add("手机号");
            headers.add("邮箱");
            headers.add("院系");
            List<List<Object>> data = new ArrayList<>();
            return ExcelUtil.exportToExcel(headers, data, "教师导入模板");
        } catch (Exception e) {
            throw new RuntimeException("生成教师模板失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String importTeachers(MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                return "错误：上传文件不能为空";
            }

            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || (!originalFilename.endsWith(".xlsx") && !originalFilename.endsWith(".xls"))) {
                return "错误：仅支持.xlsx或.xls格式的Excel文件";
            }

            List<Map<Integer, String>> rows = ExcelUtil.importFromExcel(file.getInputStream());
            if (rows.isEmpty()) {
                return "错误：Excel文件中没有数据";
            }

            int successCount = 0;
            int failCount = 0;
            StringBuilder failReasons = new StringBuilder();

            for (int i = 1; i < rows.size(); i++) {
                Map<Integer, String> rowData = rows.get(i);
                try {
                    String teacherNo = rowData.getOrDefault(0, "").trim();
                    String realName = rowData.getOrDefault(1, "").trim();
                    String genderStr = rowData.getOrDefault(2, "").trim();
                    String phone = rowData.getOrDefault(3, "").trim();
                    String email = rowData.getOrDefault(4, "").trim();
                    String department = rowData.getOrDefault(5, "").trim();

                    if (teacherNo.isEmpty() || realName.isEmpty()) {
                        failCount++;
                        failReasons.append("第").append(i + 1).append("行: 工号或姓名为空; ");
                        continue;
                    }

                    // 检查工号是否已存在
                    LambdaQueryWrapper<User> checkWrapper = new LambdaQueryWrapper<>();
                    checkWrapper.eq(User::getUsername, teacherNo);
                    if (userService.count(checkWrapper) > 0) {
                        failCount++;
                        failReasons.append("第").append(i + 1).append("行: 工号").append(teacherNo).append("已存在; ");
                        continue;
                    }

                    // 创建教师账号
                    User teacher = new User();
                    teacher.setId(teacherNo);
                    teacher.setUsername(teacherNo);
                    teacher.setTeacherNo(teacherNo);
                    String randomSalt = com.campus.ai.util.Md5Util.generateSalt();
                    teacher.setSalt(randomSalt);
                    teacher.setPassword(com.campus.ai.util.Md5Util.encrypt("123456", randomSalt));
                    teacher.setRealName(realName);
                    teacher.setGender("男".equals(genderStr) ? 1 : 0);
                    teacher.setPhone(phone.isEmpty() ? null : phone);
                    teacher.setEmail(email.isEmpty() ? null : email);
                    teacher.setDepartment(department.isEmpty() ? null : department);
                    teacher.setUserType(1);
                    teacher.setStatus(1);

                    userService.save(teacher);

                    // 分配教师角色
                    com.campus.ai.entity.UserRole userRole = new com.campus.ai.entity.UserRole();
                    userRole.setUserId(teacher.getId());
                    userRole.setRoleId(2L); // TEACHER角色
                    userRoleMapper.insert(userRole);

                    successCount++;
                } catch (Exception ex) {
                    failCount++;
                    failReasons.append("第").append(i + 1).append("行: ").append(ex.getMessage()).append("; ");
                }
            }

            String result = "导入完成！成功: " + successCount + " 条, 失败: " + failCount + " 条。";
            if (failCount > 0) {
                result += " 失败原因: " + failReasons.toString();
            }
            return result;

        } catch (Exception e) {
            return "导入教师失败: " + e.getMessage();
        }
    }

}
