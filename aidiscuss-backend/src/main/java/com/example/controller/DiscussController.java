package com.example.controller;

import com.example.model.BackgroundRequest;
import com.example.model.DiscussInfo;
import com.example.model.QuestionAnswer;
import com.example.model.QuestionRequest;
import com.example.service.DiscussService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
public class DiscussController {

    @Autowired
    private DiscussService discussService;

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    // 根据讨论ID获取讨论信息的GET请求处理方法
    @GetMapping("getDiscuss/{discussId}")
    public ResponseEntity<DiscussInfo> getDiscussInfo(@PathVariable String discussId) {
        // 调用DiscussService的getDiscuss方法获取DiscussInfo
        DiscussInfo discussInfo = discussService.getDiscuss(discussId);
        // 返回状态码200和DiscussInfo对象
        return ResponseEntity.ok(discussInfo);
    }

    @MessageMapping("/externMicSentencesConnection/{discussId}")
    public void externMicSentencesConnection(@DestinationVariable String discussId) throws InterruptedException {
        String externMicSentences = discussService.getExternMicSentences(discussId);
        // 将externMicSentences传输到"/topic/externMicSentences/{discussId}"
        simpMessagingTemplate.convertAndSend("/topic/externMicSentencesConnection/" + discussId, externMicSentences);
    }
    @MessageMapping("/wireMicSentencesConnection/{discussId}")
    public void wireMicSentencesConnection(@DestinationVariable String discussId) throws InterruptedException {
        String wireMicSentences = discussService.getWireMicSentences(discussId);
        // 将wireMicSentences传输到"/topic/wireMicSentences/{discussId}"
        simpMessagingTemplate.convertAndSend("/topic/wireMicSentencesConnection/" + discussId, wireMicSentences);
    }
    @MessageMapping("/virtualMicSentencesConnection/{discussId}")
    public void virtualMicSentencesConnection(@DestinationVariable String discussId) throws InterruptedException {
        String virtualMicSentences = discussService.getVirtualMicSentences(discussId);
        // 将virtualMicSentences传输到"/topic/virtualMicSentences/{discussId}"
        simpMessagingTemplate.convertAndSend("/topic/virtualMicSentencesConnection/" + discussId, virtualMicSentences);
    }

    @PostMapping("/askQuestion")
    public ResponseEntity<Void> askQuestion(@RequestBody QuestionRequest questionRequest) throws IOException {
        discussService.askQuestion(questionRequest);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/updateBackground")
    public ResponseEntity<Void> updateBackground(@RequestBody BackgroundRequest backgroundRequest) throws IOException {
        discussService.updateBackground(backgroundRequest);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/addBackground")
    public ResponseEntity<Void> addBackground(@RequestBody BackgroundRequest backgroundRequest) throws IOException {
        discussService.addBackground(backgroundRequest);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/deleteBackground")
    public ResponseEntity<Void> deleteBackground(@RequestBody BackgroundRequest backgroundRequest) throws IOException {
        discussService.deleteBackground(backgroundRequest);
        return ResponseEntity.ok().build();
    }

    @GetMapping("getBackground/{discussId}")
    public ResponseEntity<List<String>> getBackground(@PathVariable String discussId) {
        List<String> backgroundList = discussService.getBackground(discussId);
        return ResponseEntity.ok(backgroundList);
    }
}