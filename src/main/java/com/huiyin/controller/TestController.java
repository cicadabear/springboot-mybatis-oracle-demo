package com.huiyin.controller;

import com.huiyin.utils.ExcelXUtils;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.joda.time.DateTime;
import org.joda.time.Seconds;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/test")
public class TestController {

    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;
    @Autowired
    private SqlSessionFactory sqlSessionFactory;

    @RequestMapping(value = "/test", method = RequestMethod.GET)
    @ResponseBody
    public String test() {
        return "test";
    }

    @RequestMapping(value = "/testExcel", method = RequestMethod.GET)
    @ResponseBody
    public void testExcel(HttpServletRequest request, HttpServletResponse response) throws SQLException {
        DateTime startTime = DateTime.now();
        Statement stmt = sqlSessionFactory.openSession().getConnection().createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        ResultSet rors = stmt.executeQuery("SELECT * FROM KH_KHDD");
        //excel标题
        String[] headers = {"DDBH", "PNR_NO"};
        //excel文件名
        String fileName = "测试导表";
        List<List<String>> dataList = new ArrayList<>();
        SXSSFWorkbook workbook = ExcelXUtils.getInstance().exportExcelXWithCommonData(fileName, headers, null, dataList, false);
        int nk = 0;
        while (rors.next()) {
            String ddbh = rors.getString("DDBH");
            String cjr = rors.getString("PNR_NO");
            List<String> row = new ArrayList<>();
            row.add(ddbh);
            row.add(cjr);
            dataList.add(row);
            if (dataList.size() % 1000 == 0) {
                ExcelXUtils.getInstance().appendRows(workbook, fileName, headers.length, dataList);
                dataList.clear();
                nk++;
                System.out.println("========" + nk + "K");
            }
        }
        ExcelXUtils.getInstance().appendRows(workbook, fileName, headers.length, dataList);
        response.reset();
        //火狐浏览器乱码解决
        ExcelXUtils.getInstance().fireFoxEnCode(request, response, fileName, workbook, false);
        System.out.println("time consuming = " + Seconds.secondsBetween(startTime, DateTime.now()).getSeconds() + "s");
        return;
    }

    @RequestMapping(value = "/testExcel2", method = RequestMethod.GET)
    @ResponseBody
    public void testExcel2(HttpServletRequest request, HttpServletResponse response) throws SQLException {
        DateTime startTime = DateTime.now();
        Statement stmt = sqlSessionFactory.openSession().getConnection().createStatement();
        //excel标题
        String[] headers = {"DDBH", "PNR_NO"};
        //excel文件名
        String fileName = "测试导表";
        List<List<String>> dataList = new ArrayList<>();
        SXSSFWorkbook workbook = ExcelXUtils.getInstance().exportExcelXWithCommonData(fileName, headers, null, dataList, false);
        int page = 1;
        DateTime lastTime = startTime;
        Long lastProcessingTime = 0L;
        Integer pageSize = 5000;
        while (true) {
            ResultSet rs = stmt.executeQuery(pageSql(page, pageSize, "SELECT * FROM KH_KHDD"));
            while (rs.next()) {
                String ddbh = rs.getString("DDBH");
                String cjr = rs.getString("PNR_NO");
                List<String> row = new ArrayList<>();
                row.add(ddbh);
                row.add(cjr);
                dataList.add(row);
            }
            if (dataList.size() < pageSize) {
                break;
            }
            ExcelXUtils.getInstance().appendRows(workbook, fileName, headers.length, dataList);
            dataList.clear();
            System.out.println("======== " + page *pageSize);
            if (page == 1) {
                lastProcessingTime = DateTime.now().getMillis() - lastTime.getMillis();
            } else {
                Long thisProcessingTime = DateTime.now().getMillis() - lastTime.getMillis();
                Double ratio = Double.valueOf(thisProcessingTime) / lastProcessingTime * 100;
                DecimalFormat df = new DecimalFormat("#.00");
                System.out.println("========= " + df.format(ratio) + "%");
                lastProcessingTime = thisProcessingTime;
            }
            lastTime = DateTime.now();
            page++;
        }
        ExcelXUtils.getInstance().appendRows(workbook, fileName, headers.length, dataList);

        response.reset();
        //火狐浏览器乱码解决
        ExcelXUtils.getInstance().fireFoxEnCode(request, response, fileName, workbook, false);
        System.out.println("time consuming = " + Seconds.secondsBetween(startTime, DateTime.now()).getSeconds() + "s");
        return;
    }

    private String pageSql(Integer page, Integer pageSize, String sql) {
        Integer num1 = ((page * pageSize) + 1);
        Integer num2 = (((page - 1) * pageSize) + 1);
        String sqlStr = "SELECT * FROM\n" +
                "(\n" +
                "    SELECT a.*, rownum r__\n" +
                "    FROM\n" +
                "    (\n"
                + sql +
                "    ) a\n" +
                "    WHERE rownum < %d \n" +
                ")\n" +
                "WHERE r__ >= %d";
        return String.format(sqlStr, num1, num2);
    }
}
