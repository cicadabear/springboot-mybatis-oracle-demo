package com.huiyin.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFCell;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 利用开源组件POI导出EXCEL文档
 */
public class ExcelXUtils {

    protected static Logger logger = LoggerFactory.getLogger(ExcelXUtils.class);

    private static ExcelXUtils instance = null;

    public static final String DATE_FULL_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    private ExcelXUtils() {
    }

    public static ExcelXUtils getInstance() {
        if (instance == null) {
            synchronized (ExcelXUtils.class) {
                if (instance == null) {
                    instance = new ExcelXUtils();
                }
            }
        }
        return instance;
    }


    public SXSSFWorkbook exportExcelXWithCommonData(String title, String[] headers, String[] footers, List<List<String>> dataList, boolean createTitle) {
        return exportExcelXWithCommonData(title, headers, footers, dataList, createTitle, null);
    }

    public SXSSFWorkbook exportExcelXWithCommonData(String title, String[] headers, String[] footers, List<List<String>> dataList, boolean createTitle, Integer createTotal) {
        // 声明一个工作薄
        SXSSFWorkbook workbook = new SXSSFWorkbook(100);
        exportExcelXWithCommonDataCreateSheet(workbook, title, headers, footers, dataList, createTitle, createTotal);
        return workbook;
    }

    public void exportExcelXWithCommonDataCreateSheet(SXSSFWorkbook workbook, String title, String[] headers, String[] footers, List<List<String>> dataList, boolean createTitle, Integer createTotal) {
        // 生成一个表格
        SXSSFSheet sheet = workbook.createSheet(title);
        // 设置表格默认列宽度为15个字节
        sheet.setDefaultColumnWidth((short) 15);
        // 生成一个样式
        CellStyle style = workbook.createCellStyle();
        // 设置这些样式
        style.setFillForegroundColor(HSSFColor.SKY_BLUE.index);
        style.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
        style.setBorderBottom(HSSFCellStyle.BORDER_THIN);
        style.setBorderLeft(HSSFCellStyle.BORDER_THIN);
        style.setBorderRight(HSSFCellStyle.BORDER_THIN);
        style.setBorderTop(HSSFCellStyle.BORDER_THIN);
        style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        if (createTitle) {
            style.setWrapText(true); //设置自动换行
        }
        // 生成一个字体
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 12);
        font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        // 把字体应用到当前的样式
        style.setFont(font);
        // 生成并设置另一个样式
        CellStyle style2 = workbook.createCellStyle();
        style2.setWrapText(true);
        style2.setFillForegroundColor(HSSFColor.WHITE.index);
        style2.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
        style2.setBorderBottom(HSSFCellStyle.BORDER_THIN);
        style2.setBorderLeft(HSSFCellStyle.BORDER_THIN);
        style2.setBorderRight(HSSFCellStyle.BORDER_THIN);
        style2.setBorderTop(HSSFCellStyle.BORDER_THIN);
        style2.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        style2.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
        // 生成另一个字体
        Font font2 = workbook.createFont();
        font2.setBoldweight(HSSFFont.BOLDWEIGHT_NORMAL);
        // 把字体应用到当前的样式
        style2.setFont(font2);

        // 声明一个画图的顶级管理器
        Drawing patriarch = sheet.createDrawingPatriarch();
        // 产生表格标题行
        int index = 0;
        if (createTitle) {
            createTitleRow(workbook, sheet, style, index, title, headers.length - 1);
            index++;
        }
        SXSSFRow row = sheet.createRow(index);
        setRowValue(headers, row, style);

        // 遍历集合数据，产生数据行
        Iterator<List<String>> it = dataList.iterator();
        while (it.hasNext()) {
            index++;
            List<String> valueList = it.next();
            row = sheet.createRow(index);
            int i = -1;
            for (String value : valueList) {
                if (i >= (headers.length - 1)) {
                    continue;
                }
                i++;
                SXSSFCell cell = row.createCell(i);
                cell.setCellStyle(style2);
                try {
                    if (value == null) {
                        continue;
                    }
                    // 判断值的类型后进行强制类型转换
                    String textValue = null;
                    textValue = this.convertCellValue(workbook, sheet, patriarch, row, value, index, i);
                    // 如果不是图片数据，就利用正则表达式判断textValue是否全部由数字组成
                    if (textValue != null) {
//                        Pattern p = Pattern.compile("^[+-]?\\d*[.]?\\d*$");
//                        Matcher matcher = p.matcher(textValue);
//                        if (matcher.matches()) {
//                            // 是数字当作double处理
//                            if (StringUtils.isNotEmpty(textValue)) {
//                                cell.setCellValue(Double.parseDouble(textValue));
//                            }
//                        } else {
//                            if (StringUtils.startsWith(textValue, "@")) {
//                                textValue = StringUtils.substring(textValue, 1);
//                                //新增的四句话，设置CELL格式为文本格式  
//                                CellStyle cellStyle2 = cell.getCellStyle();
//                                DataFormat format = workbook.createDataFormat();
//                                cellStyle2.setDataFormat(format.getFormat("@"));
//                                cell.setCellStyle(cellStyle2);
//                            }
//                            cell.setCellValue(textValue);
//                        }
                        cell.setCellValue(textValue);
                        this.setColWidthByContent(sheet, textValue, cell.getColumnIndex(), 10);
                    }
                } catch (SecurityException e) {
                    logger.error("", e);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void exportExcelXWithTextDailyCreateSheet(SXSSFWorkbook workbook, String title, String textDaily) {
        // 生成一个表格
        SXSSFSheet sheet = workbook.createSheet(title);
        // 设置表格默认列宽度为15个字节
        sheet.setDefaultColumnWidth((short) 150);
        CellStyle style2 = workbook.createCellStyle();
        style2.setFillForegroundColor(HSSFColor.WHITE.index);
        style2.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
        style2.setBorderBottom(HSSFCellStyle.BORDER_THIN);
        style2.setBorderLeft(HSSFCellStyle.BORDER_THIN);
        style2.setBorderRight(HSSFCellStyle.BORDER_THIN);
        style2.setBorderTop(HSSFCellStyle.BORDER_THIN);
        style2.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        style2.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
        style2.setWrapText(true); //设置自动换行
        // 生成另一个字体
        Font font2 = workbook.createFont();
        font2.setBoldweight(HSSFFont.BOLDWEIGHT_NORMAL);
        // 把字体应用到当前的样式
        style2.setFont(font2);
        SXSSFRow row = sheet.createRow(0);
        SXSSFCell cell = row.createCell(0);
        cell.setCellStyle(style2);
        cell.setCellValue(textDaily);
        this.setColWidthByContent(sheet, textDaily, cell.getColumnIndex(), 10);
    }

    public void appendRows(SXSSFWorkbook workbook, String title, int headerLength, List<List<String>> dataList) {

        SXSSFSheet sheet = workbook.getSheet(title);

        CellStyle style2 = workbook.createCellStyle();
        style2.setFillForegroundColor(HSSFColor.WHITE.index);
        style2.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
        style2.setBorderBottom(HSSFCellStyle.BORDER_THIN);
        style2.setBorderLeft(HSSFCellStyle.BORDER_THIN);
        style2.setBorderRight(HSSFCellStyle.BORDER_THIN);
        style2.setBorderTop(HSSFCellStyle.BORDER_THIN);
        style2.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        style2.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
        // 生成另一个字体
        Font font2 = workbook.createFont();
        font2.setBoldweight(HSSFFont.BOLDWEIGHT_NORMAL);
        // 把字体应用到当前的样式
        style2.setFont(font2);

        // 声明一个画图的顶级管理器
        Drawing patriarch = sheet.getDrawingPatriarch();
        int index = sheet.getLastRowNum();
        // 遍历集合数据，产生数据行
        Iterator<List<String>> it = dataList.iterator();
        while (it.hasNext()) {
            index++;
            List<String> valueList = it.next();
            SXSSFRow row = sheet.createRow(index);
            int i = -1;
            for (String value : valueList) {
                if (i >= (headerLength - 1)) {
                    continue;
                }
                i++;
                SXSSFCell cell = row.createCell(i);
                cell.setCellStyle(style2);
                try {
                    if (value == null) {
                        continue;
                    }
                    // 判断值的类型后进行强制类型转换
                    String textValue = null;
                    textValue = this.convertCellValue(workbook, sheet, patriarch, row, value, index, i);
                    // 如果不是图片数据，就利用正则表达式判断textValue是否全部由数字组成
                    if (textValue != null) {
//                        Pattern p = Pattern.compile("^[+-]?\\d*[.]?\\d*$");
//                        Matcher matcher = p.matcher(textValue);
//                        if (matcher.matches()) {
//                            // 是数字当作double处理
//                            try {
//                                if (StringUtils.isNotEmpty(textValue)) {
//                                    cell.setCellValue(Double.parseDouble(textValue));
//                                }
//                            } catch (NumberFormatException e) {
//                                cell.setCellValue(textValue);
//                            }
//
//                        } else {
//                            if (StringUtils.startsWith(textValue, "@")) {
//                                textValue = StringUtils.substring(textValue, 1);
//                                //新增的四句话，设置CELL格式为文本格式  
//                                CellStyle cellStyle2 = cell.getCellStyle();
//                                DataFormat format = workbook.createDataFormat();
//                                cellStyle2.setDataFormat(format.getFormat("@"));
//                                cell.setCellStyle(cellStyle2);
//                            }
//                            cell.setCellValue(textValue);
//                        }
                        cell.setCellValue(textValue);
                        this.setColWidthByContent(sheet, textValue, cell.getColumnIndex(), 10);
                    }
                } catch (SecurityException e) {

                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    private void createTitleRow(SXSSFWorkbook workbook, SXSSFSheet sheet, CellStyle style, int index, String title, int columns) {
        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.cloneStyleFrom(style);
        cellStyle.setFillForegroundColor(HSSFColor.WHITE.index);
        Font titleFont = workbook.createFont();
        titleFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        titleFont.setFontHeightInPoints((short) 18);
        cellStyle.setFont(titleFont);

        SXSSFRow row = sheet.createRow(index);
        row.setHeight((short) 650);
        SXSSFCell cell = row.createCell(index);
        cell.setCellStyle(cellStyle);
        cell.setCellValue(title);
        CellRangeAddress cra = new CellRangeAddress(index, index, 0, columns);
        sheet.addMergedRegion(cra);
    }

    /**
     * 为Excel 表格转换值
     */
    private String convertCellValue(SXSSFWorkbook workbook, SXSSFSheet sheet,
                                    Drawing patriarch, SXSSFRow row, Object value, int rowIndex, int cellIndex) {
        // 判断值的类型后进行强制类型转换
        String cellValue = null;
        if (value instanceof BigDecimal) {
            cellValue = value.toString();
        } else if (value instanceof Boolean) {
            boolean bValue = (Boolean) value;
            if (bValue) {
                cellValue = "是";
            } else {
                cellValue = "否";
            }
        } else if (value instanceof Date) {
            Date date = (Date) value;
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_FULL_TIME_PATTERN);
            cellValue = sdf.format(date);
        } else if (value instanceof DateTime) {
            DateTime date = (DateTime) value;
            cellValue = date.toString(DATE_FULL_TIME_PATTERN);
        } else if (value instanceof byte[]) {
            // 有图片时，设置行高为60px;
            row.setHeightInPoints(60);
            // 设置图片所在列宽度为80px,注意这里单位的一个换算
            sheet.setColumnWidth(cellIndex, (short) (35.7 * 80));

            byte[] bsValue = (byte[]) value;
            HSSFClientAnchor anchor = new HSSFClientAnchor(0, 0,
                    1023, 255, (short) 6, rowIndex, (short) 6, rowIndex);
            anchor.setAnchorType(ClientAnchor.AnchorType.MOVE_DONT_RESIZE);
            patriarch.createPicture(anchor, workbook.addPicture(
                    bsValue, HSSFWorkbook.PICTURE_TYPE_JPEG));
        } else {
            cellValue = value.toString();// 其它数据类型都当作字符串简单处理
        }
        return cellValue;
    }

    /**
     * 批量生成某行所有单元格
     *
     * @param cellCount
     * @param rowindex
     * @param sheet
     * @param style
     * @param height    行高单位像素
     * @return
     */
    private HSSFRow createRow(int cellCount, int rowindex, HSSFSheet sheet, HSSFCellStyle style, float height) {
        HSSFRow row = sheet.createRow(rowindex);
        row.setHeightInPoints(height);
        boolean isSetStyle = style == null ? false : true;
        for (int i = 0; i < cellCount; i++) {
            HSSFCell cell = row.createCell(i);
            if (isSetStyle) cell.setCellStyle(style);
        }
        return row;
    }

    private void setRowValue(String[] arrays, SXSSFRow row, CellStyle style) {
        for (int i = 0; i < arrays.length; i++) {
            SXSSFCell cell = row.createCell(i);
            cell.setCellStyle(style);
            XSSFRichTextString text = new XSSFRichTextString(arrays[i]);
            cell.setCellValue(text);
            this.setColWidthByContent(cell.getSheet(), arrays[i], cell.getColumnIndex(), 10);
        }
    }

    /**
     * 生成表单头填充值兵自适应列宽
     *
     * @param arrays
     * @param row
     * @param style
     */
    private void createTitleAutoColWidth(String[] arrays, SXSSFRow row, SXSSFSheet sheet, CellStyle style) {
        for (int i = 0; i < arrays.length; i++) {
            SXSSFCell cell = row.createCell(i);
            cell.setCellStyle(style);
            HSSFRichTextString text = new HSSFRichTextString(arrays[i]);
            cell.setCellValue(text);
            this.setColWidthByContent(sheet, arrays[i], i, 10);
        }
    }


    /**
     * 根据内容设置列宽
     */
    private void setColWidthByContent(SXSSFSheet sheet, String cellValue, int colIndex, int size) {
        try {
            int wordCount = this.getWordCount(cellValue);
            float currentWidth = sheet.getColumnWidth(colIndex);
            int newWidth = (int) (wordCount * 35.7 * size);
            if (newWidth > currentWidth)
                sheet.setColumnWidth(colIndex, newWidth);
        } catch (Exception e) {
            logger.error("setColWidthByContent{}", e);
        }
    }


    /**
     * @param s 按字节获取字符长度
     */
    private int getWordCount(String s) {

        s = s.replaceAll("[^\\x00-\\xff]", "**");
        int length = s.length();
        return length;
    }

    /**
     * 解决火狐浏览器乱码的问题
     *
     * @param request
     * @param response
     * @param title
     * @param workbook
     */
    public void fireFoxEnCode(HttpServletRequest request, HttpServletResponse response, String title, SXSSFWorkbook workbook, boolean ignore) {
        try {
            if (!ignore && BrowserUtils.getBrowser(request).equals("FF")) {//如果是火狐,解决火狐中文名乱码问题
                title = new String(title.getBytes("UTF-8"), "iso-8859-1");
                response.addHeader("Content-Disposition", "attachment;filename="
                        + title + ".xlsx");
            } else {
                response.addHeader("Content-Disposition", "attachment;filename="
                        + URLEncoder.encode(title + ".xlsx", "utf-8"));
            }
//            response.setContentType("application/vnd.ms-excel;charset=utf-8");
            workbook.write(response.getOutputStream());
            response.getOutputStream().close();
            workbook.dispose();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 导出Excel
     *
     * @param sheetName sheet名称
     * @param title     标题
     * @param values    内容
     * @param wb        HSSFWorkbook对象
     * @return
     */
    public static HSSFWorkbook getHSSFWorkbook(String sheetName, String[] title, String[][] values, HSSFWorkbook wb) {

        // 第一步，创建一个HSSFWorkbook，对应一个Excel文件
        if (wb == null) {
            wb = new HSSFWorkbook();
        }

        // 第二步，在workbook中添加一个sheet,对应Excel文件中的sheet
        HSSFSheet sheet = wb.createSheet(sheetName);

        // 第三步，在sheet中添加表头第0行,注意老版本poi对Excel的行数列数有限制
        HSSFRow row = sheet.createRow(0);

        // 第四步，创建单元格，并设置值表头 设置表头居中
        HSSFCellStyle style = wb.createCellStyle();
        style.setAlignment(HSSFCellStyle.ALIGN_CENTER); // 创建一个居中格式

        //声明列对象
        HSSFCell cell = null;

        //创建标题
        for (int i = 0; i < title.length; i++) {
            cell = row.createCell(i);
            cell.setCellValue(title[i]);
            cell.setCellStyle(style);
        }

        //创建内容
        for (int i = 0; i < values.length; i++) {
            row = sheet.createRow(i + 1);
            for (int j = 0; j < values[i].length; j++) {
                //将内容按顺序赋给对应的列对象
                row.createCell(j).setCellValue(values[i][j]);
            }
        }
        return wb;
    }

}