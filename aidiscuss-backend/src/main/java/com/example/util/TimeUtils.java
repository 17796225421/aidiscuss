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
        // 定义日期时间格式: yyyy-MM-dd HH:mm:ss.SSS
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        return now.format(formatter);
    }

}