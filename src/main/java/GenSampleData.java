import java.io.*;
import java.util.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;

public class GenSampleData {
    public static void main(String[] args) throws Exception {
        String outDir = args.length > 0 ? args[0] : "d:\\gongc";
        genStudents(outDir);
        genCourses(outDir);
        System.out.println("Done! Files generated in " + outDir);
    }

    static void genStudents(String dir) throws Exception {
        String[][] headers = {{"学号","姓名","性别","手机号","邮箱","院系","专业","年级"}};
        String[][] data = {
            {"2024001","张三","男","13800001001","zhangsan@qq.com","计算机学院","软件工程","2024"},
            {"2024002","李四","女","13800001002","lisi@qq.com","计算机学院","软件工程","2024"},
            {"2024003","王五","男","13800001003","wangwu@qq.com","计算机学院","大数据","2024"},
            {"2024004","赵六","女","13800001004","zhaoliu@qq.com","计算机学院","人工智能","2024"},
            {"2024005","孙七","男","13800001005","sunqi@qq.com","电子信息学院","通信工程","2024"},
            {"2024006","周八","女","13800001006","zhouba@qq.com","电子信息学院","电子信息","2024"},
            {"2024007","吴九","男","13800001007","wujiu@qq.com","数学学院","应用数学","2024"},
            {"2024008","郑十","女","13800001008","zhengshi@qq.com","数学学院","统计学","2024"},
            {"2024009","刘明","男","13800001009","lium@qq.com","外国语学院","英语","2024"},
            {"2024010","陈丽","女","13800001010","chenli@qq.com","外国语学院","日语","2024"},
        };
        writeExcel(dir + "\\学生导入模板_示例数据.xlsx", headers, data);
    }

    static void genCourses(String dir) throws Exception {
        String[][] headers = {{"课程编号","课程名称","教师工号","教师姓名","院系","学分","总学时","理论学时","实践学时","课程类型","考核方式","学期","课程描述"}};
        String[][] data = {
            {"CS101","数据结构","T1001","王教授","计算机学院","4","64","48","16","1","1","2024-2025-1","经典数据结构课程，涵盖线性表、树、图等"},
            {"CS102","操作系统","T1002","李教授","计算机学院","4","64","48","16","1","1","2024-2025-1","操作系统原理，进程管理、内存管理等"},
            {"CS103","计算机网络","T1003","张教授","计算机学院","3","48","36","12","1","2","2024-2025-1","TCP/IP协议、网络拓扑、路由等"},
            {"CS104","数据库原理","T1004","赵教授","计算机学院","3","48","36","12","2","1","2024-2025-1","关系数据库、SQL、范式理论"},
            {"CS105","Java程序设计","T1005","钱教授","计算机学院","4","64","32","32","2","3","2024-2025-1","面向对象编程、集合框架、多线程"},
            {"EE201","信号与系统","T2001","周教授","电子信息学院","3","48","40","8","1","1","2024-2025-1","连续与离散信号分析"},
            {"EE202","数字电路","T2002","吴教授","电子信息学院","3","48","36","12","2","1","2024-2025-1","组合逻辑与时序逻辑电路"},
            {"MA301","高等数学","T3001","郑教授","数学学院","5","80","80","0","1","2","2024-2025-1","微积分、级数、微分方程基础"},
            {"MA302","线性代数","T3002","孙教授","数学学院","3","48","48","0","2","1","2024-2025-1","矩阵理论、向量空间、特征值"},
            {"EN401","大学英语","T4001","刘教授","外国语学院","4","64","48","16","1","2","2024-2025-1","英语读写与口语训练"},
        };
        writeExcel(dir + "\\课程导入模板_示例数据.xlsx", headers, data);
    }

    static void writeExcel(String path, String[][] headers, String[][] rows) throws Exception {
        XSSFWorkbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet("Sheet1");
        CellStyle headerStyle = wb.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Font headerFont = wb.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);

        int rowIdx = 0;
        for (String[] hr : headers) {
            Row row = sheet.createRow(rowIdx++);
            for (int i = 0; i < hr.length; i++) {
                Cell cell = row.createCell(i);
                cell.setCellValue(hr[i]);
                cell.setCellStyle(headerStyle);
            }
        }
        for (String[] data : rows) {
            Row row = sheet.createRow(rowIdx++);
            for (int i = 0; i < data.length; i++) {
                row.createCell(i).setCellValue(data[i]);
            }
        }
        for (int i = 0; i < headers[0].length; i++) {
            sheet.autoSizeColumn(i);
        }
        try (FileOutputStream fos = new FileOutputStream(path)) {
            wb.write(fos);
        }
        wb.close();
    }
}
