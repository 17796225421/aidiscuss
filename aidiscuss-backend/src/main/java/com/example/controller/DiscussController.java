package com.example.controller;

import com.example.model.DiscussInfo;
import com.example.model.MicSwitchInfo;
import com.example.service.DiscussService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class DiscussController {

    @Autowired
    private DiscussService discussService;

    // 根据讨论ID获取讨论信息的GET请求处理方法
    @GetMapping("getDiscuss/{discussId}")
    public ResponseEntity<DiscussInfo> getDiscussInfo(@PathVariable String discussId) {
        // 调用DiscussService的getDiscuss方法获取DiscussInfo
        DiscussInfo discussInfo = discussService.getDiscuss(discussId);
        // 返回状态码200和DiscussInfo对象
        return ResponseEntity.ok(discussInfo);
    }

    // 处理麦克风开关的POST请求
    @PostMapping("/micSwitch")
    public ResponseEntity<Void> handleMicSwitch(@RequestBody DiscussInfo discussInfo) {
        // 调用DiscussService的micSwitch方法处理麦克风开关逻辑
        discussService.micSwitch(discussInfo);
        // 返回状态码200
        return ResponseEntity.ok().build();
    }
}