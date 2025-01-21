package com.pinnacle.backend.controller;

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

@RestController
@RequestMapping("/client/payload")
public class PayloadController {
    @Autowired
    private PayloadService payloadService;

    @PostMapping("/save")
    public ResponseEntity<String> savePayload(
            @RequestHeader("X-API-Key") String apiKey,
            @RequestBody PayloadRequest payloadRequest) {
        try {
            payloadService.processPayload(apiKey, payloadRequest);
            return ResponseEntity.ok("Payload saved successfully");
        } catch (UnAuthorizedException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
        }
    }
}
