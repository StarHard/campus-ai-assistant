package com.campus.ai.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * Excel导入导出服务接口
 */
public interface ExcelService {

    /**
     * 导出课程列表
     *
     * @return Excel字节数组
     */
    byte[] exportCourses();

    /**
     * 下载学生导入模板（仅表头）
     *
     * @return Excel字节数组
     */
    byte[] downloadStudentTemplate();

    /**
     * 下载课程导入模板（仅表头）
     *
     * @return Excel字节数组
     */
    byte[] downloadCourseTemplate();

    /**
     * 批量导入学生
     *
     * @param file Excel文件
     * @return 导入结果信息
     */
    String importStudents(MultipartFile file);

    /**
     * 批量导入课程
     *
     * @param file Excel文件
     * @return 导入结果信息
     */
    String importCourses(MultipartFile file);

    /**
     * 下载教师导入模板（仅表头）
     *
     * @return Excel字节数组
     */
    byte[] downloadTeacherTemplate();

    /**
     * 批量导入教师
     *
     * @param file Excel文件
     * @return 导入结果信息
     */
    String importTeachers(MultipartFile file);
}
