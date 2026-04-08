package com.sky.service.impl;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WorkspaceService workspaceService;

    @Override
    public TurnoverReportVO turnoverStatistics(LocalDate begin, LocalDate end) {

        List<LocalDate> dateList = new ArrayList<>();
        LocalDate current = begin;
        while (!current.isAfter(end)) {
            dateList.add(current);
            current = current.plusDays(1);
        }
        String dateListStr = dateList.stream()
                .map(date -> date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                .collect(Collectors.joining(","));

        List<String> turnoverList = new ArrayList<>();
        for (LocalDate date : dateList) {
            LocalDateTime dayStart = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime dayEnd = LocalDateTime.of(date, LocalTime.MAX);
            Map<String, Object> map = new HashMap<>();
            map.put("begin", dayStart);
            map.put("end", dayEnd);
            map.put("status", Orders.COMPLETED);
            Double turnover = orderMapper.sumByMap(map);
            turnoverList.add(turnover == null ? "0.0" : turnover.toString());
        }
        String turnoverListStr = String.join(",", turnoverList);

        return TurnoverReportVO.builder()
                .dateList(dateListStr)
                .turnoverList(turnoverListStr)
                .build();
    }

    @Override
    public UserReportVO userStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();
        LocalDate current = begin;
        while (!current.isAfter(end)) {
            dateList.add(current);
            current = current.plusDays(1);
        }
        String dateListStr = dateList.stream()
                .map(date -> date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                .collect(Collectors.joining(","));

        List<Integer> userList = new ArrayList<>();
        List<Integer> totalUserList = new ArrayList<>();
        for (LocalDate date : dateList) {
            LocalDateTime dayStart = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime dayEnd = LocalDateTime.of(date, LocalTime.MAX);
            Map<String, LocalDateTime> map = new HashMap<>();
            map.put("begin", dayStart);
            Integer newUser = userMapper.countbyStatus(map);
            map.put("end", dayEnd);
            Integer totalUser = userMapper.countbyStatus(map);
            userList.add(newUser == null ? 0 : newUser);
            totalUserList.add(totalUser == null ? 0 : totalUser);
        }
        String userListStr = String.join(",", userList.stream().map(String::valueOf).collect(Collectors.toList()));
        String totalUserListStr = String.join(",", totalUserList.stream().map(String::valueOf).collect(Collectors.toList()));
        return UserReportVO.builder()
                .dateList(dateListStr)
                .newUserList(userListStr)
                .totalUserList(totalUserListStr)
                .build();
    }

    @Override
    public OrderReportVO ordersStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();
        LocalDate current = begin;
        while (!current.isAfter(end)) {
            dateList.add(current);
            current = current.plusDays(1);
        }
        String dateListStr = dateList.stream()
                .map(date -> date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                .collect(Collectors.joining(","));
        int validOrderCount = 0;
        int orderCount = 0;
        List<Integer> dayOrderCountList = new ArrayList<>();
        List<Integer> dayvalidOrderCountList = new ArrayList<>();
        for (LocalDate date : dateList) {
            LocalDateTime dayStart = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime dayEnd = LocalDateTime.of(date, LocalTime.MAX);
            Map<String, Object> map = new HashMap<>();
            map.put("begin", dayStart);
            map.put("end", dayEnd);
            Integer dayOrderCount = orderMapper.dayorder(map);
            dayOrderCountList.add(dayOrderCount == null ? 0 : dayOrderCount);
            map.put("status", Orders.COMPLETED);
            Integer dayValidOrderCount = orderMapper.dayorder(map);
            dayvalidOrderCountList.add(dayValidOrderCount == null ? 0 : dayValidOrderCount);
        }
        String orderCountListStr = String.join(",", dayOrderCountList.stream().map(String::valueOf).collect(Collectors.toList()));
        String validOrderCountListStr = String.join(",", dayvalidOrderCountList.stream().map(String::valueOf).collect(Collectors.toList()));
        for (int i = 0; i < dayOrderCountList.size(); i++) {
            validOrderCount += dayvalidOrderCountList.get(i);
            orderCount += dayOrderCountList.get(i);
        }

        Double orderCompletionRate = orderCount == 0 ? 0.0 : (double) validOrderCount / orderCount;
        return OrderReportVO.builder()
                .dateList(dateListStr)
                .orderCountList(orderCountListStr)
                .validOrderCountList(validOrderCountListStr)
                .totalOrderCount(orderCount)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .build();
    }

    @Override
    public SalesTop10ReportVO top10Statistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();
        LocalDate current = begin;
        while (!current.isAfter(end)) {
            dateList.add(current);
            current = current.plusDays(1);
        }
        List<String> nameList = new ArrayList<>();
        List<Integer> numberList = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        map.put("begin", begin);
        map.put("end", end);
        List<Map<String, Object>> list = orderMapper.top10(map);
        if (list != null && !list.isEmpty()) {
            for (Map<String, Object> item : list) {
                nameList.add(item.get("name").toString());
                numberList.add(Integer.parseInt(item.get("number").toString()));
            }
        }
        return SalesTop10ReportVO.builder()
                .nameList(String.join(",", nameList))
                .numberList(String.join(",", numberList.stream().map(String::valueOf).collect(Collectors.toList())))
                .build();
    }

    @Override
    public void export(HttpServletResponse response) {
        LocalDate begindate = LocalDate.now().minusDays(30);
        LocalDate enddate = LocalDate.now().minusDays(1);
        LocalDateTime begin = LocalDateTime.of(begindate, LocalTime.MIN);
        LocalDateTime end = LocalDateTime.of(enddate, LocalTime.MAX);
        BusinessDataVO businessDataVO = workspaceService.getBusinessData(begin, end);


        //写入excel
        InputStream in = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");
        try {
            XSSFWorkbook excel = new XSSFWorkbook(in);
            XSSFSheet sheet = excel.getSheet("Sheet1");
            sheet.getRow(1).getCell(1).setCellValue("时间："+begindate+"至"+enddate);
            XSSFRow row=sheet.getRow(3);
            row.getCell(2).setCellValue(businessDataVO.getTurnover());
            row.getCell(4).setCellValue(businessDataVO.getOrderCompletionRate());
            row.getCell(6).setCellValue(businessDataVO.getNewUsers());
            row=sheet.getRow(4);
            row.getCell(2).setCellValue(businessDataVO.getValidOrderCount() == null ? 0 : businessDataVO.getValidOrderCount());
            row.getCell(4).setCellValue(businessDataVO.getUnitPrice() == null ? 0 : businessDataVO.getUnitPrice());
            for (int i = 0; i < 30; i++) {
                LocalDate date= begindate.plusDays(i);
                BusinessDataVO businessDatavo=workspaceService.getBusinessData(LocalDateTime.of(date,LocalTime.MIN),LocalDateTime.of(date,LocalTime.MAX));
                row=sheet.getRow(i+7);
                row.getCell(1).setCellValue(date.toString());
                row.getCell(2).setCellValue(businessDatavo.getTurnover());
                row.getCell(3).setCellValue(businessDatavo.getValidOrderCount());
                row.getCell(4).setCellValue(businessDatavo.getOrderCompletionRate());
                row.getCell(5).setCellValue(businessDatavo.getUnitPrice());
                row.getCell(6).setCellValue(businessDatavo.getNewUsers());
            }
            ServletOutputStream out = response.getOutputStream();
            excel.write(out);
            out.close();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }




    }
}

