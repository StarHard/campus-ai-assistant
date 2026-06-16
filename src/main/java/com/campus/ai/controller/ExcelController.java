package com.campus.ai.controller;

import com.campus.ai.annotation.RequireRole;
import com.campus.ai.dto.Result;
import com.campus.ai.service.ExcelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Tag(name = "Excel导入导出")
@RestController
@RequestMapping("/excel")
@RequireRole({"ADMIN", "TEACHER"})
public class ExcelController {

    @Autowired
    private ExcelService excelService;

    @Operation(summary = "导出课程列表")
    @GetMapping("/export/courses")
    public ResponseEntity<byte[]> exportCourses() {
        byte[] data = excelService.exportCourses();
        return buildExcelResponse(data, "课程列表.xlsx");
    }

    @Operation(summary = "下载学生导入模板")
    @GetMapping("/template/student")
    public ResponseEntity<byte[]> downloadStudentTemplate() {
        byte[] data = excelService.downloadStudentTemplate();
        return buildExcelResponse(data, "学生导入模板.xlsx");
    }

    @Operation(summary = "下载课程导入模板")
    @GetMapping("/template/course")
    public ResponseEntity<byte[]> downloadCourseTemplate() {
        byte[] data = excelService.downloadCourseTemplate();
        return buildExcelResponse(data, "课程导入模板.xlsx");
    }

    @Operation(summary = "下载教师导入模板")
    @GetMapping("/template/teacher")
    public ResponseEntity<byte[]> downloadTeacherTemplate() {
        byte[] data = excelService.downloadTeacherTemplate();
        return buildExcelResponse(data, "教师导入模板.xlsx");
    }

    @Operation(summary = "批量导入学生")
    @PostMapping("/import/students")
    public Result<String> importStudents(@RequestParam("file") MultipartFile file) {
        String result = excelService.importStudents(file);
        if (result.startsWith("错误") || result.contains("失败:")) {
            return Result.error(result);
        }
        return Result.success(result);
    }

    @Operation(summary = "批量导入课程")
    @PostMapping("/import/courses")
    public Result<String> importCourses(@RequestParam("file") MultipartFile file) {
        String result = excelService.importCourses(file);
        if (result.startsWith("错误") || result.contains("失败:")) {
            return Result.error(result);
        }
        return Result.success(result);
    }

    @Operation(summary = "批量导入教师")
    @PostMapping("/import/teachers")
    public Result<String> importTeachers(@RequestParam("file") MultipartFile file) {
        String result = excelService.importTeachers(file);
        if (result.startsWith("错误") || result.contains("失败:")) {
            return Result.error(result);
        }
        return Result.success(result);
    }

    private ResponseEntity<byte[]> buildExcelResponse(byte[] data, String filename) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");
        headers.setContentDispositionFormData("attachment", encodedFilename);
        headers.setContentLength(data.length);
        return ResponseEntity.ok().headers(headers).body(data);
    }
}
