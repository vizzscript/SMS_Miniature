package com.pinnacle.backend.service;

import org.springframework.http.HttpHeaders;

import com.pinnacle.backend.dto.PayloadRequest;

public interface PayloadService {
    void processPayload(String apiKey, PayloadRequest payloadRequest);
    void processingPayload(String payloadRequest, HttpHeaders headers);
}
