package com.example.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TimeUtils {

    /**
     * 获取当前格式化时间
     *
     * @return 格式化后的时间字符串
     */
    public static String getCurrentFormattedTime() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M月d日H时m分s秒S");
        return now.format(formatter);
    }

}