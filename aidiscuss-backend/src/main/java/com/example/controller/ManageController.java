package com.example.controller;

import com.example.model.DiscussBaseInfo;
import com.example.model.DiscussInfo;
import com.example.service.ManageService;
import com.example.model.ManageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ManageController {

    @Autowired
    private ManageService manageService;

    // 处理GET请求，返回ManageInfo信息
    @GetMapping("/getManageInfo")
    public ResponseEntity<ManageInfo> getManageInfo() {
        // 调用ManageService的getManageInfo方法获取信息
        ManageInfo manageInfo = manageService.getManageInfo();

        // 如果manageInfo不为null，说明有数据，返回200状态码和数据
        if (manageInfo != null) {
            return ResponseEntity.ok(manageInfo);
        } else {
            // 如果manageInfo为null，说明没有数据，返回204状态码（No Content）
            return ResponseEntity.noContent().build();
        }
    }
    @PostMapping("/createDiscuss")
    public ResponseEntity<DiscussBaseInfo> createDiscuss() {
        // 调用ManageService的createDiscuss方法创建新的讨论
        DiscussBaseInfo discussBaseInfo = manageService.createDiscuss();

        // 返回200状态码和创建的讨论名称
        return ResponseEntity.ok(discussBaseInfo);
    }

    @PostMapping("/closeDiscuss")
    public ResponseEntity<Void> closeDiscuss(@RequestBody String discussId) {
        // 调用DiscussService的micSwitch方法处理麦克风开关逻辑
        manageService.closeDiscuss(discussId);
        // 返回状态码200
        return ResponseEntity.ok().build();
    }
}