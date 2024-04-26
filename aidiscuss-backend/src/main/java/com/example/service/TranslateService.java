package com.example.service;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Collections;

@Service
public class TranslateService {

    private final String url = "https://api.deeplx.org/translate";

    public String translateToChinese(String text) {
        return translate(text, "ZH");
    }

    public String translateToEnglish(String text) {
        return translate(text, "EN");
    }

    private String translate(String text, String targetLang) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode requestJson = objectMapper.createObjectNode();
        requestJson.put("text", text);
        requestJson.put("target_lang", targetLang);
        requestJson.put("source_lang", "auto");

        String requestBody = requestJson.toString();

        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<TranslateResponse> response = restTemplate.postForEntity(url, entity, TranslateResponse.class);

        TranslateResponse translateResponse = response.getBody();
        if (translateResponse != null && translateResponse.getCode() == 200) {
            return translateResponse.getData();
        } else {
            return "";
        }
    }
    public static class TranslateResponse {
        private int code;
        private long id;
        private String data;

        // 省略 getter 和 setter 方法
        public int getCode() {
            return code;
        }

        public String getData() {
            return data;
        }
    }

    public static void main(String[] args) {
        TranslateService service = new TranslateService();
        System.out.println(service.translateToChinese("Hello\nhi"));
        System.out.println(service.translateToEnglish("早上吃什么"));
    }
}