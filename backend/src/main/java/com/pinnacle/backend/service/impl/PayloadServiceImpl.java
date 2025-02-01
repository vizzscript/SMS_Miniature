package com.pinnacle.backend.service.impl;

import java.time.Instant;
// import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import com.fasterxml.jackson.databind.ObjectMapper;
// import com.fasterxml.jackson.databind.ObjectMapper;
import com.pinnacle.backend.dto.PayloadRequest;
import com.pinnacle.backend.exceptions.UnAuthorizedException;
import com.pinnacle.backend.model.ClientModel;
import com.pinnacle.backend.model.FinalModel;
import com.pinnacle.backend.repository.ClientRepository;
import com.pinnacle.backend.repository.FinalRepository;
import com.pinnacle.backend.service.PayloadService;
import com.pinnacle.backend.util.DecryptionUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class PayloadServiceImpl implements PayloadService {
    @Autowired
    private final ClientRepository clientRepository;
    @Autowired
    private final FinalRepository finalRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    // @Autowired
    // private ObjectMapper objectMapper; // For JSON conversion

    @Override
    public void processPayload(String apiKey, PayloadRequest payloadRequest) {
        // Authenticate via API Key
        ClientModel client = clientRepository.findByApiKey(apiKey)
                .orElseThrow(() -> new UnAuthorizedException("Invalid API Key"));

        // Validate username and password
        if (!client.getUserName().equals(payloadRequest.getUserName())) {
            throw new UnAuthorizedException("Invalid username");
        }

        if (!passwordEncoder.matches(payloadRequest.getPassword(), client.getPwd())) {
            throw new UnAuthorizedException("Invalid password");
        }

        // Process and save payload
        payloadRequest.getPayload().forEach(dto -> {
            FinalModel entity = new FinalModel();
            entity.setClient(client); // Associate with client
            entity.setMobileNo(dto.getMobileNo());
            entity.setMessage(dto.getMessage());
            entity.setSender(dto.getSender());
            entity.setRespId(UUID.randomUUID().toString());
            entity.setReqDateTime(Instant.now());
            finalRepository.save(entity);
        });
    }

    @Override
    public void processingPayload(String encryptedPayload, HttpHeaders headers) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start(); // Start stopwatch

        try {
            log.info("Received encrypted payload...");
            log.debug("Raw headers: {}", headers);
            log.debug("Raw Encrypted Payload: {}", encryptedPayload);
            // Decrypt the payload
            String decryptedPayload = DecryptionUtil.decrypt(encryptedPayload);
            log.debug("Decrypted Payload: {}", decryptedPayload);

            // Retrieve and decrypt API Key
            String encryptedApiKey = headers.getFirst("apiKey");
            String decryptedApiKey = DecryptionUtil.decryptAPIKey(encryptedApiKey);
            log.debug("Decrypted API Key: {}", decryptedApiKey);

            // Authenticate via API Key
            ClientModel client = clientRepository.findByApiKey(decryptedApiKey)
                    .orElseThrow(() -> new UnAuthorizedException("Invalid API Key"));

            // Parse decrypted payload into a PayloadRequest object
            ObjectMapper objectMapper = new ObjectMapper();
            PayloadRequest payloadRequest = objectMapper.readValue(decryptedPayload, PayloadRequest.class);
            log.debug("Parsed decrypted payload: {}", payloadRequest);
            System.out.println(payloadRequest);

            // Save each FinalModel into the database
            for (PayloadRequest.FinalModel finalModel : payloadRequest.getPayload()) {
                FinalModel entity = new FinalModel();
                entity.setClient(client);
                entity.setSender(finalModel.getSender());
                entity.setMobileNo(finalModel.getMobileNo());
                entity.setMessage(finalModel.getMessage());
                entity.setRespId(UUID.randomUUID().toString());
                entity.setReqDateTime(Instant.now());
                finalRepository.save(entity);
            }

            log.info("Payload successfully stored in database");

        } catch (Exception e) {
            log.error("Error processing payload: ", e);
            throw new RuntimeException("Failed to process payload", e);
        } finally {
            stopWatch.stop(); // Stop stopwatch
            log.info("Processing time: {} ms", stopWatch.getTotalTimeMillis());
        }
    }

}
