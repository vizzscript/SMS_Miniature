package com.pinnacle.backend.service.impl;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.lang.reflect.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.ObjectMapper;
// import com.fasterxml.jackson.databind.ObjectMapper;
import com.pinnacle.backend.dto.PayloadRequest;
import com.pinnacle.backend.exceptions.UnAuthorizedException;
import com.pinnacle.backend.model.ClientModel;
import com.pinnacle.backend.model.FinalModel;
import com.pinnacle.backend.repository.ClientRepository;
import com.pinnacle.backend.repository.FinalRepository;
import com.pinnacle.backend.service.PayloadService;
import com.pinnacle.backend.service.RateLimiterService;
import com.pinnacle.backend.util.DecryptionUtil;
import com.pinnacle.backend.util.HeaderValidationUtil;
import com.pinnacle.backend.util.MessageValidationUtil;
import com.pinnacle.backend.util.MobileValidationUtil;
import com.pinnacle.backend.util.PasswordValidationUtil;
import com.pinnacle.backend.util.SenderValidationUtil;
import com.pinnacle.backend.util.UsernameValidationUtil;

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
    @Autowired
    private RateLimiterService rateLimiterService;

    @Autowired
    private ObjectMapper objectMapper; // For JSON conversion

    // Basic way to process payload
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

    // Efficient way to process payload
    @Override
    public void processingPayload(String encryptedPayload, HttpHeaders headers) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start(); // Start stopwatch

        try {
            log.info("Received encrypted payload...");
            log.info("Raw headers: {}", headers);
            log.info("Raw Encrypted Payload: {}", encryptedPayload);

            // Validate Headers
            HeaderValidationUtil.validateHeaders(headers);

            String isEncrypted = headers.getFirst("isencrypted");
            String decryptedPayload;

            // Check if data is encrypted or not based on header isencrypted
            if ("1".equals(isEncrypted)) {
                // Decrypt the payload
                decryptedPayload = DecryptionUtil.decrypt(encryptedPayload);
                log.info("Decrypted Payload: {}", decryptedPayload);
            } else {
                decryptedPayload = encryptedPayload;
            }
            // Parse decrypted payload into a PayloadRequest object
            objectMapper = new ObjectMapper();
            PayloadRequest payloadRequest = objectMapper.readValue(decryptedPayload, PayloadRequest.class);
            log.info("Parsed decrypted payload: {}", payloadRequest);
            System.out.println(payloadRequest);

            // Converting data packet keys in lowercase
            // @SuppressWarnings("rawtypes")
            // Class packetKeys = payloadRequest.getClass();
            // List<Field> allFields = Arrays.asList(packetKeys.getDeclaredFields());
            // allFields.forEach(field -> field.getName().toLowerCase());

            // log.info("All fields case: ", allFields.get(0));

            // Get all fields
            Class<?> packetKeys = payloadRequest.getClass();
            List<Field> allFields = Arrays.asList(packetKeys.getDeclaredFields());

            // Convert field names to lowercase and store in a Map
            Map<String, String> fieldNameMap = allFields.stream()
                    .collect(Collectors.toMap(
                            field -> field.getName(), // Original field name
                            field -> field.getName().toLowerCase() // Lowercase field name
                    ));

            // Log transformed field names
            fieldNameMap.forEach((original, lowercase) -> System.out
                    .println("Original: " + original + " -> Lowercase: " + lowercase));

            // Retrieve and decrypt API Key
            String encryptedApiKey = headers.getFirst("apikey");
            String decryptedApiKey = DecryptionUtil.decryptAPIKey(encryptedApiKey);
            log.info("Decrypted API Key: {}", decryptedApiKey);

            String userName = payloadRequest.getUserName();
            String password = payloadRequest.getPassword();

            // Check rate limit
            if (!rateLimiterService.allowRequest(userName)) {
                log.warn("Rate limit exceeded for UserName: {}", userName);
                throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                        "Rate limit exceeded. Try again later.");
            }

            ClientModel client = clientRepository.findByUserName(userName)
                    .orElseThrow(() -> new UnAuthorizedException("Invalid Username"));

            // Ensure API Key & Username belong to the same record
            if ((decryptedApiKey != null && decryptedApiKey.equals(client.getApiKey())
                    && userName.equals(client.getUserName()))
                    || (userName.equals(client.getUserName()) && password.equals(client.getPwd()))) {
                log.info("User authentication successful.");
                payloadRequest.getPayload().forEach(finalModel -> {
                    try {
                        // Validate username and password in the payloadRequest
                        UsernameValidationUtil.validate(payloadRequest.getUserName());
                        PasswordValidationUtil.validate(payloadRequest.getPassword());
                    } catch (IllegalArgumentException e) {
                        log.error("Validation failed for username or password in model: {}", payloadRequest, e);
                        throw new IllegalArgumentException(
                                "Invalid username or password for payload: " + payloadRequest);
                    }
                });
                // Approach 3: Using stream for List of FinalModel dto
                List<FinalModel> entities = payloadRequest.getPayload().stream()
                        .map(finalModel -> {
                            try {
                                // Validate sender, mobile number, and message for each finalModel data
                                SenderValidationUtil.validate(finalModel.getSender());
                                MobileValidationUtil.validate(finalModel.getMobileNo());
                                MessageValidationUtil.validate(finalModel.getMessage());
                            } catch (IllegalArgumentException e) {
                                log.error("Validation failed for sender, mobile, or message in finalModel: {}",
                                        finalModel, e);
                                throw new IllegalArgumentException("Invalid data in finalModel: " + finalModel);
                            }
                            FinalModel entity = new FinalModel();
                            entity.setClient(client);
                            entity.setSender(finalModel.getSender());
                            entity.setMobileNo(finalModel.getMobileNo());
                            entity.setMessage(finalModel.getMessage());
                            entity.setRespId(UUID.randomUUID().toString());
                            entity.setReqDateTime(Instant.now());
                            return entity;
                        })
                        .collect(Collectors.toList());

                finalRepository.saveAll(entities); // Batch save instead of multiple insert queries

                log.info("Payload successfully stored in database");

            } else {
                throw new UnAuthorizedException("Invalid API Key or Credentials.");
            }

            // if(decryptedApiKey.equals(client.getApiKey())){
            // System.out.println("Authenticated using apiKey");
            // }
            // else if()

            // To do:
            // Validations on username, password, sender, message, mobileno.
            // validations on headers

            // First, validate username and password in payloadRequest before processing

            // Save each FinalModel into the database
            // Approach 1: Using For each
            // for (PayloadRequest.FinalModel finalModel : payloadRequest.getPayload()) {
            // FinalModel entity = new FinalModel();
            // entity.setClient(client);
            // entity.setSender(finalModel.getSender());
            // entity.setMobileNo(finalModel.getMobileNo());
            // entity.setMessage(finalModel.getMessage());
            // entity.setRespId(UUID.randomUUID().toString());
            // entity.setReqDateTime(Instant.now());
            // finalRepository.save(entity);
            // }

            // Approach 2: Using stream for whole payloadRequest object
            // payloadRequest.getPayload().stream()
            // .map(finalModel -> {
            // FinalModel entity = new FinalModel();
            // entity.setClient(client);
            // entity.setSender(finalModel.getSender());
            // entity.setMobileNo(finalModel.getMobileNo());
            // entity.setMessage(finalModel.getMessage());
            // entity.setRespId(UUID.randomUUID().toString());
            // entity.setReqDateTime(Instant.now());
            // return entity;
            // })
            // .forEach(finalRepository::save);

        } catch (Exception e) {
            log.error("Error processing payload: ", e);
            throw new RuntimeException("Failed to process payload", e);
        } finally {
            stopWatch.stop(); // Stop stopwatch
            log.info("Processing time: {} ms", stopWatch.getTotalTimeMillis());
        }
    }

}
