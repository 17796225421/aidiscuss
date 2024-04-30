package com.example.util;

import org.json.JSONObject;

public class GptResUtils {
    public static JSONObject StringToJson(String s) {
        // 去除开头的非JSON字符
        int startIndex = s.indexOf("{");
        if (startIndex != -1) {
            s = s.substring(startIndex);
        }

        // 去除结尾的非JSON字符
        int endIndex = s.lastIndexOf("}");
        if (endIndex != -1) {
            s = s.substring(0, endIndex + 1);
        }

        return new JSONObject(s);
    }

    public static String getPlantUml(String s) {
        int startIndex = s.indexOf("@");
        if (startIndex != -1) {
            s = s.substring(startIndex);
        }

        int endIndex = s.lastIndexOf("@");
        if (endIndex != -1) {
            endIndex += 6;
            s = s.substring(0, endIndex + 1);
        }

        return s;
    }
}
