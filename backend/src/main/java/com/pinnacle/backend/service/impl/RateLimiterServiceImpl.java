package com.pinnacle.backend.service.impl;

// import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.pinnacle.backend.service.RateLimiterService;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
// import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.Refill;

@Service
public class RateLimiterServiceImpl implements RateLimiterService {
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Override
    public boolean allowRequest(String apiKey) {
        Bucket bucket = resolveBucket(apiKey);
        return bucket.tryConsume(1);
    }

    @Override
    public Bucket resolveBucket(String apiKey) {
        return buckets.computeIfAbsent(apiKey, key -> {
            Bandwidth limit = Bandwidth.classic(5, Refill.intervally(5, java.time.Duration.ofMinutes(1)));
            // BucketConfiguration.builder()
            // .addLimit(limit)
            // .build();
            return Bucket.builder()
                    .addLimit(limit)
                    .build();
        });

    }

}
