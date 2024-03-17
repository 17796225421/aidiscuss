package com.example.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DiscussController {

    @PostMapping("/")
    public String processVideo(@RequestBody String videoUrl) {
        return "";
    }
}
