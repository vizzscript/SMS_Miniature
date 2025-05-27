package com.pinnacle.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pinnacle.backend.service.MultithreadService;

@RestController
@RequestMapping("/multithread")
public class MultithreadController {
    @Autowired
    private MultithreadService multithreadService;

    @GetMapping("/process")
    public String processFinalModels() {
        multithreadService.processFinalModelsConcurrently();
        return "Processing started!";
    }
}
