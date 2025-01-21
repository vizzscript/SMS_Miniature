package com.pinnacle.backend.service;

import com.pinnacle.backend.dto.PayloadRequest;

public interface PayloadService {
    void processPayload(String apiKey, PayloadRequest payloadRequest);
}
