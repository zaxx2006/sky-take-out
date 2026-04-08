package com.sky.service.impl;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;

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
}

