package com.pinnacle.backend.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pinnacle.backend.dto.PayloadRequest;
import com.pinnacle.backend.exceptions.UnAuthorizedException;
import com.pinnacle.backend.service.PayloadService;
import com.pinnacle.backend.service.RateLimiterService;

@RestController
@RequestMapping("/client/payload")
public class PayloadController {
    private static final Logger logger = LoggerFactory.getLogger(PayloadController.class);
    
    @Autowired
    private PayloadService payloadService;

    @Autowired
    private RateLimiterService rateLimiterService;

    @PostMapping("/save")
    public ResponseEntity<String> savePayload(
            @RequestHeader("API-Key") String apiKey,
            @RequestBody PayloadRequest payloadRequest) {
        logger.info("Incoming Payload Data: {}", payloadRequest);
        if(!rateLimiterService.allowRequest(apiKey)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Too many requests");
        }
        else{
            logger.info("Request allowed for API Key: {}", apiKey);
        }
        try {
            payloadService.processPayload(apiKey, payloadRequest);
            return ResponseEntity.ok("Payload saved successfully");
        } catch (UnAuthorizedException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
        }
    }
}
