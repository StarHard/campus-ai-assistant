package com.campus.ai.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Excel导入导出工具类
 * 基于Apache POI 5.2.5，支持.xlsx格式
 */
public class ExcelUtil {

    private static final String HEADER_BG_COLOR = "1F4E79";

    /**
     * 导出数据到Excel
     *
     * @param headers  表头列表
     * @param data     数据行列表（每行为一个Object列表）
     * @param sheetName 工作表名称
     * @return Excel字节数组
     */
    public static byte[] exportToExcel(List<String> headers, List<List<Object>> data, String sheetName) {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            XSSFSheet sheet = workbook.createSheet(sheetName != null ? sheetName : "Sheet1");

            // 创建表头样式
            CellStyle headerStyle = createHeaderStyle(workbook);

            // 创建数据行样式（带边框）
            CellStyle dataStyle = createDataStyle(workbook);

            // 写入表头
            XSSFRow headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.size(); i++) {
                XSSFCell cell = headerRow.createCell(i);
                cell.setCellValue(headers.get(i));
                cell.setCellStyle(headerStyle);
            }

            // 写入数据行
            for (int rowNum = 0; rowNum < data.size(); rowNum++) {
                XSSFRow row = sheet.createRow(rowNum + 1);
                List<Object> rowData = data.get(rowNum);
                for (int colNum = 0; colNum < rowData.size(); colNum++) {
                    XSSFCell cell = row.createCell(colNum);
                    Object value = rowData.get(colNum);
                    setCellValue(cell, value);
                    cell.setCellStyle(dataStyle);
                }
            }

            // 自动调整列宽
            autoSizeColumns(sheet, headers.size());

            workbook.write(out);
            return out.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("导出Excel失败", e);
        }
    }

    /**
     * 从Excel导入数据
     *
     * @param inputStream Excel文件输入流
     * @return 每行数据的Map列表，key为列索引(Integer)，value为单元格字符串值
     */
    public static List<Map<Integer, String>> importFromExcel(InputStream inputStream) {
        try (XSSFWorkbook workbook = new XSSFWorkbook(inputStream)) {
            XSSFSheet sheet = workbook.getSheetAt(0);
            List<Map<Integer, String>> result = new ArrayList<>();

            int firstRowNum = sheet.getFirstRowNum();
            int lastRowNum = sheet.getLastRowNum();

            for (int rowNum = firstRowNum; rowNum <= lastRowNum; rowNum++) {
                XSSFRow row = sheet.getRow(rowNum);
                if (row == null) {
                    continue;
                }
                Map<Integer, String> rowData = new java.util.LinkedHashMap<>();
                short firstCellNum = row.getFirstCellNum();
                short lastCellNum = row.getLastCellNum();
                for (int colNum = firstCellNum; colNum < lastCellNum; colNum++) {
                    XSSFCell cell = row.getCell(colNum);
                    String cellValue = getCellStringValue(cell);
                    rowData.put(colNum, cellValue);
                }
                result.add(rowData);
            }
            return result;

        } catch (IOException e) {
            throw new RuntimeException("读取Excel文件失败", e);
        }
    }

    /**
     * 创建深蓝色表头样式：背景#1F4E79、白色字体、边框
     */
    private static CellStyle createHeaderStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();

        // 深蓝色背景 #1F4E79
        XSSFColor bgColor = new XSSFColor();
        bgColor.setRGB(new byte[]{0x1F, 0x4E, 0x79});
        style.setFillForegroundColor(bgColor);
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // 白色字体
        XSSFFont font = workbook.createFont();
        font.setColor(IndexedColors.WHITE.getIndex());
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);

        // 边框
        setBorder(style);

        // 居中
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        return style;
    }

    /**
     * 创建数据行样式：带边框
     */
    private static CellStyle createDataStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        setBorder(style);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    /**
     * 设置边框
     */
    private static void setBorder(CellStyle style) {
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setTopBorderColor(IndexedColors.GREY_80_PERCENT.getIndex());
        style.setBottomBorderColor(IndexedColors.GREY_80_PERCENT.getIndex());
        style.setLeftBorderColor(IndexedColors.GREY_80_PERCENT.getIndex());
        style.setRightBorderColor(IndexedColors.GREY_80_PERCENT.getIndex());
    }

    /**
     * 自动调整列宽
     */
    private static void autoSizeColumns(XSSFSheet sheet, int columnCount) {
        for (int i = 0; i < columnCount; i++) {
            try {
                sheet.autoSizeColumn(i);
                // 自动列宽后增加一点余量，避免显示截断
                int width = sheet.getColumnWidth(i);
                sheet.setColumnWidth(i, width + 512);
            } catch (Exception e) {
                // 如果自动列宽失败，设置默认宽度
                sheet.setColumnWidth(i, 4000);
            }
        }
    }

    /**
     * 设置单元格值（支持多种类型）
     */
    private static void setCellValue(XSSFCell cell, Object value) {
        if (value == null) {
            cell.setCellValue("");
        } else if (value instanceof Number) {
            cell.setCellValue(((Number) value).doubleValue());
        } else if (value instanceof Boolean) {
            cell.setCellValue((Boolean) value);
        } else if (value instanceof java.time.LocalDateTime) {
            cell.setCellValue(((java.time.LocalDateTime) value).format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        } else if (value instanceof java.time.LocalDate) {
            cell.setCellValue(((java.time.LocalDate) value).format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        } else {
            cell.setCellValue(value.toString());
        }
    }

    /**
     * 获取单元格的字符串值
     */
    private static String getCellStringValue(XSSFCell cell) {
        if (cell == null) {
            return "";
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                }
                double numValue = cell.getNumericCellValue();
                if (numValue == Math.floor(numValue) && !Double.isInfinite(numValue)) {
                    return String.valueOf((long) numValue);
                }
                return String.valueOf(numValue);
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return cell.getStringCellValue();
                } catch (Exception e) {
                    return String.valueOf(cell.getNumericCellValue());
                }
            case BLANK:
                return "";
            default:
                return "";
        }
    }
}
