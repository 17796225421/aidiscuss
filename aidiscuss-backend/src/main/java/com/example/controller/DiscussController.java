package com.example.controller;

import com.example.model.*;
import com.example.service.DiscussService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
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

    @MessageMapping("/discussInfoConnection/{discussId}")
    public void sentenceListConnection(@DestinationVariable String discussId) {
        DiscussInfo discussInfo = discussService.getDiscussInfo(discussId);
        simpMessagingTemplate.convertAndSend("/topic/discussInfoConnection/" + discussId, discussInfo);
    }

    @PostMapping("/askQuestion")
    public ResponseEntity<Void> askQuestion(@RequestBody QuestionRequest questionRequest) throws IOException {
        discussService.askQuestion(questionRequest);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/deleteQuestion")
    public ResponseEntity<Void> deleteQuestion(@RequestBody QuestionRequest questionRequest) throws IOException {
        discussService.deleteQuestion(questionRequest);
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

    @GetMapping("/audio/virtual/{discussId}")
    public ResponseEntity<Resource> virtualAudio(@PathVariable String discussId) throws IOException {
        Resource audio = discussService.audio(discussId, MicTypeEnum.VIRTUAL);
        if (!audio.exists()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        Path path = Paths.get(audio.getURI());
        String contentType = Files.probeContentType(path);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + audio.getFilename() + "\"")
                .body(audio);
    }
    @GetMapping("/audio/wire/{discussId}")
    public ResponseEntity<Resource> wireAudio(@PathVariable String discussId) throws IOException {
        Resource audio = discussService.audio(discussId, MicTypeEnum.WIRE);
        if (!audio.exists()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        Path path = Paths.get(audio.getURI());
        String contentType = Files.probeContentType(path);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + audio.getFilename() + "\"")
                .body(audio);
    }
    @GetMapping("/audio/extern/{discussId}")
    public ResponseEntity<Resource> externAudio(@PathVariable String discussId) throws IOException {
        Resource audio = discussService.audio(discussId, MicTypeEnum.EXTERN);
        if (!audio.exists()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        Path path = Paths.get(audio.getURI());
        String contentType = Files.probeContentType(path);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + audio.getFilename() + "\"")
                .body(audio);
    }
    @PostMapping("/postNoteText")
    public ResponseEntity<Void> postNoteText(@RequestParam String discussId, @RequestParam String text) {
        discussService.postNoteText(discussId, text);
        return ResponseEntity.ok().build();
    }

}