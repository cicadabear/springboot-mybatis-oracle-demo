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
            System.out.println("======== " + page * pageSize);
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

    @RequestMapping(value = "/testExcel3", method = RequestMethod.GET)
    @ResponseBody
    public void testExcel3(HttpServletRequest request, HttpServletResponse response) throws SQLException {
        System.out.println("start");
        DateTime startTime = DateTime.now();
        String sql = "select a.ResID as \"ResID\",a.BookingRef as \"BookingRef\",b.TicketID as \"TicketID\",a.TicketCorp as \"TicketCorp\",a.SendCorp as \"SendCorp\",a.OfficeID as \"OfficeID\",a.ticketOffice as \"ticketOffice\",c.FlightNo as \"FlightNo\",c.Cabin as \"Cabin\",b.OutPrice as \"OutPrice\",b.AgtPrice as \"AgtPrice\",b.InPrice as \"InPrice\",b.YQFee as \"YQFee\",b.TaxFee as \"TaxFee\",b.ReturnPrice as \"ReturnPrice\",a.CustFfp as \"CustFfp\",b.AirDiscount as \"AirDiscount\",b.reward as \"reward\",b.serviceCharge as \"serviceCharge\"  FROM T_Airbook a,T_AIRBOOKDETAIL b,T_AirBookLines c  WHERE a.resid = b.resid  AND b.resserial = c.resserial  AND a.resid = c.resid  and ( (a.AlertStatus=1 and a.SUBMITSUPPLYTIME >= to_date('2019-03-02','yyyy/mm/dd') and a.SUBMITSUPPLYTIME < to_date('2019-03-21','yyyy/mm/dd')) or  (a.AlertStatus in (0,2,3,4) and a.tickettime >= to_date('2019-03-02','yyyy/mm/dd') and a.tickettime < to_date('2019-03-21','yyyy/mm/dd')))  AND a.AlertStatus in (3,0)  AND a.SendCorp in ('100003','100021','100022') AND a.BookStatus in (25,30,40,50)";
        SqlSession sqlSession = sqlSessionFactory.openSession();
        Statement stmt = sqlSession.getConnection().createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        stmt.setFetchSize(10000);
        ResultSet rors = stmt.executeQuery(sql);
        System.out.println("query time consuming = " + Seconds.secondsBetween(startTime, DateTime.now()).getSeconds() + "s");
        //excel标题
        String[] headers = {"ResID", "BookingRef", "TicketID", "TicketCorp", "SendCorp"};
        //excel文件名
        String fileName = "测试导表";
        List<List<String>> dataList = new ArrayList<>();
        SXSSFWorkbook workbook = ExcelXUtils.getInstance().exportExcelXWithCommonData(fileName, headers, null, dataList, false);
        int i = 0;
        int batch = 0;
        int batchSize = 5000;
        while (rors.next()) {
            i++;
            List<String> row = new ArrayList<>();
            row.add(rors.getString("ResID"));
            row.add(rors.getString("BookingRef"));
            row.add(rors.getString("TicketID"));
            row.add(rors.getString("TicketCorp"));
            row.add(rors.getString("SendCorp"));
            dataList.add(row);
            if (dataList.size() % batchSize == 0) {
                DateTime startWriteTime = DateTime.now();
                ExcelXUtils.getInstance().appendRows(workbook, fileName, headers.length, dataList);
                System.out.println("batch write time consuming = " + Seconds.secondsBetween(startWriteTime, DateTime.now()).getSeconds() + "s");
                dataList.clear();
                batch++;
                System.out.println("==========" + batch * batchSize);
            }
            if (dataList.size() % 1000 == 0) {
                System.out.println("========" + i);
            }
        }
        ExcelXUtils.getInstance().appendRows(workbook, fileName, headers.length, dataList);
//        sqlSession.getConnection().close();
        sqlSession.close();
        response.reset();
        //火狐浏览器乱码解决
        ExcelXUtils.getInstance().fireFoxEnCode(request, response, fileName, workbook, false);
        System.out.println("time consuming = " + Seconds.secondsBetween(startTime, DateTime.now()).getSeconds() + "s");
        return;
    }
}
