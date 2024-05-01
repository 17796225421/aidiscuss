package com.example.controller;

import com.example.model.BackgroundRequest;
import com.example.model.DiscussInfo;
import com.example.model.QuestionRequest;
import com.example.model.Sentence;
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
import java.nio.file.Path;
import java.nio.file.Paths;
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

    @GetMapping("/audio")
    public ResponseEntity<Resource> audio(HttpServletRequest request) throws IOException {
        System.out.println(111);
        Path path = Paths.get("C:\\Users\\zhouzihong\\Desktop\\aidiscuss\\aidiscuss-backend\\test.wav");
        Resource resource = new UrlResource(path.toUri());

        // 获取文件总长度
        long fileLength = resource.contentLength();
        // 获取请求中的Range头部
        String range = request.getHeader("Range");
        long start = 0, end = fileLength - 1;

        // 解析Range头部
        if (range != null) {
            String[] ranges = range.replace("bytes=", "").split("-");
            start = Long.parseLong(ranges[0]);
            if (ranges.length > 1) {
                end = Long.parseLong(ranges[1]);
            }
        }

        // 计算内容长度和新的Content-Range头部
        long contentLength = end - start + 1;
        String contentRange = "bytes " + start + "-" + end + "/" + fileLength;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentLength(contentLength);
        headers.set("Content-Range", contentRange);
        headers.setContentType(MediaType.parseMediaType("audio/wav"));

        // 以部分内容状态码返回请求的音频数据块
        return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                .headers(headers)
                .body(new InputStreamResource(resource.getInputStream()));
    }

}