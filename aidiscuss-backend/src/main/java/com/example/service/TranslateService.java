package com.example.service;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

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

        String requestJson = "{\"text\":\"" + text + "\", \"target_lang\":\"" + targetLang + "\"}";

        HttpEntity<String> entity = new HttpEntity<>(requestJson, headers);

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
        System.out.println(service.translateToChinese("Hello"));
        System.out.println(service.translateToEnglish("早上吃什么"));
    }
}