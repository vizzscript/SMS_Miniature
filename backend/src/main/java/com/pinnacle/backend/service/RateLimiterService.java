package com.pinnacle.backend.service;

import io.github.bucket4j.Bucket;

public interface RateLimiterService {
    public Bucket resolveBucket(String apiKey);
    public boolean allowRequest(String apiKey);
}
