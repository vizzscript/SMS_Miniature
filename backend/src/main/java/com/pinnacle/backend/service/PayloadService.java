package com.pinnacle.backend.service;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import com.pinnacle.backend.dto.PayloadRequest;

public interface PayloadService {
    ResponseEntity<?> processPayload(String apiKey, PayloadRequest payloadRequest);
    ResponseEntity<?> processingPayload(String payloadRequest, HttpHeaders headers);
}
