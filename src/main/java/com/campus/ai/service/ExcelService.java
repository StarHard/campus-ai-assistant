package com.campus.ai.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * Excel导入导出服务接口
 */
public interface ExcelService {

    byte[] exportCourses();
    byte[] downloadStudentTemplate();
    byte[] downloadCourseTemplate();
    String importStudents(MultipartFile file);
    String importCourses(MultipartFile file);
    byte[] downloadTeacherTemplate();
    String importTeachers(MultipartFile file);
}
