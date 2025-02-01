package com.pinnacle.backend.controller;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pinnacle.backend.dto.PayloadRequest;
import com.pinnacle.backend.exceptions.UnAuthorizedException;
import com.pinnacle.backend.service.PayloadService;
import com.pinnacle.backend.service.RateLimiterService;
import com.pinnacle.backend.util.DecryptionUtil;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/client/payload")
@Slf4j
public class PayloadController {

    @Autowired
    private PayloadService payloadService;

    @Autowired
    private RateLimiterService rateLimiterService;

    // @PostMapping("/save")
    // public ResponseEntity<String> savePayload(
    // @RequestHeader("API-Key") String apiKey,
    // @RequestBody PayloadRequest payloadRequest) {

    // // What I have done in Packet Controller route I need to
    // // do same with the PayloadController

    // log.info("Incoming Payload Data: {}", payloadRequest);

    // if (!rateLimiterService.allowRequest(apiKey)) {
    // return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Too many
    // requests");
    // } else {
    // log.info("Request allowed for API Key: {}", apiKey);
    // }
    // try {
    // payloadService.processPayload(apiKey, payloadRequest);
    // return ResponseEntity.ok("Payload saved successfully");
    // } catch (UnAuthorizedException ex) {
    // return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
    // }
    // }

    @PostMapping("/save")
    public ResponseEntity<String> savePayload(
            @RequestHeader("API-Key") String encryptedAPIKey,
            @RequestBody String encryptedPayload) throws Exception {

        log.info("Incoming Encrypted Payload Data: {}", encryptedPayload);
        log.info("Incoming Encrypted API-Key: {}", encryptedAPIKey);

        String decryptedAPIKey = DecryptionUtil.decryptAPIKey(encryptedAPIKey);

        log.info("Decrypted API-Key: {}", decryptedAPIKey);

        // Decrypting Incoming encryptedPayload
        String decryptedJsonString = DecryptionUtil.decrypt(encryptedPayload);

        log.info("Decrypted Json String: {}", decryptedJsonString);

        if (!rateLimiterService.allowRequest(decryptedAPIKey)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Too many requests");
        } else {
            log.info("Request allowed for API Key: {}", decryptedAPIKey);
        }

        try {

            // Convert JSON string to PayloadRequest Object
            ObjectMapper objectMapper = new ObjectMapper();
            PayloadRequest payloadRequest = objectMapper.readValue(decryptedJsonString, PayloadRequest.class);
            log.info("Decrypted Payload Data: {}", payloadRequest);
            System.out.println(payloadRequest);

            // Process the payload
            payloadService.processPayload(decryptedAPIKey, payloadRequest);

            return ResponseEntity.ok("Payload saved successfully");
        } catch (UnAuthorizedException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
        } catch (Exception e) {
            log.error("Error Processing payload.", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred");
        }
    }

    @PostMapping("/accept")
    public ResponseEntity<String> acceptPayload(@RequestBody String encryptedPayload,
            @RequestHeader HttpHeaders headers) {
                
        try {
            log.info("Received request to accept payload");

            // Process the payload
            payloadService.processingPayload(encryptedPayload, headers);

            return ResponseEntity.ok("Payload received and stored successfully.");
        } catch (Exception e) {
            log.error("Error accepting payload: ", e);
            return ResponseEntity.status(500).body("Failed to process payload.");
        }

    }

}
