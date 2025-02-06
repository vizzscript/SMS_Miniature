package com.pinnacle.backend.service.impl;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pinnacle.backend.dto.PayloadRequest;
import com.pinnacle.backend.exceptions.UnAuthorizedException;
import com.pinnacle.backend.model.ClientModel;
import com.pinnacle.backend.model.FinalModel;
import com.pinnacle.backend.model.PrivilegeModel;
import com.pinnacle.backend.repository.ClientRepository;
import com.pinnacle.backend.repository.FinalRepository;
import com.pinnacle.backend.repository.PrivilegeRepository;
import com.pinnacle.backend.service.PayloadService;
import com.pinnacle.backend.service.RateLimiterService;
import com.pinnacle.backend.util.DecryptionUtil;
import com.pinnacle.backend.util.HeaderValidationUtil;
import com.pinnacle.backend.util.MessageValidationUtil;
import com.pinnacle.backend.util.MobileValidationUtil;
import com.pinnacle.backend.util.SenderValidationUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class PayloadServiceImpl implements PayloadService {
    @Autowired
    private ClientRepository clientRepository;
    @Autowired
    private FinalRepository finalRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private RateLimiterService rateLimiterService;

    @Autowired
    private PrivilegeRepository privilegeRepository;

    // @Autowired
    // private ObjectMapper objectMapper; // For JSON conversion

    private Double smsBal;

    // Basic way to process payload
    @Override
    public ResponseEntity<?> processPayload(String apiKey, PayloadRequest payloadRequest) {
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
        return ResponseEntity.ok("Everything looks good!");
    }

    // Efficient way to process payload
    @Override
    public ResponseEntity<?> processingPayload(String encryptedPayload, HttpHeaders headers) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start(); // Start stopwatch
        try {
            log.info("Processing payload...", encryptedPayload);

            // Validate Headers
            HeaderValidationUtil.validateHeaders(headers);

            // Decrypt Payload if required
            String isEncrypted = headers.getFirst("isencrypted");
            String decryptedPayload = "1".equals(isEncrypted) ? DecryptionUtil.decrypt(encryptedPayload)
                    : encryptedPayload;

            // Parse JSON payload
            PayloadRequest payloadRequest = new ObjectMapper().readValue(decryptedPayload, PayloadRequest.class);
            log.info("Parsed decrypted payload: {}", payloadRequest);
            System.out.println(payloadRequest);

            // Validate API Key
            String decryptedApiKey = DecryptionUtil.decryptAPIKey(headers.getFirst("apiKey"));
            log.info("Decrypted API Key: {}", decryptedApiKey);

            // Fetch Client & Privileges
            ClientModel client = clientRepository.findByUserName(payloadRequest.getUserName())
                    .orElse(null);
            if (client == null) {
                log.warn("Invalid Username: {}", payloadRequest.getUserName());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(
                                Map.of(
                                        "status", HttpStatus.UNAUTHORIZED.value(),
                                        "message", "Invalid Username",
                                        "timestamp", Instant.now().toString()));
            }
            List<String> privilegeList = privilegeRepository.findByClient_MemId(client.getMemId())
                    .map(PrivilegeModel::getPrivileges)
                    .orElse(Collections.emptyList());

            // Authenticate User
            if (!(decryptedApiKey.equals(client.getApiKey()) ||
                    (payloadRequest.getUserName().equals(client.getUserName()) &&
                            payloadRequest.getPassword().equals(client.getPwd())))) {
                return ResponseEntity.status(401).body("Invalid Username & Password");
            }

            // Rate Limiting Check
            if (!rateLimiterService.allowRequest(payloadRequest.getUserName())) {
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Too Many Requests");
            }


            // Processing Payload
            List<FinalModel> entities = new ArrayList<>();
            try {
                smsBal = client.getSmsBalance() != null ? client.getSmsBalance() : 0.0;
                entities = payloadRequest.getPayload().stream()
                        .map(finalModel -> {
                            // Validate input data
                            SenderValidationUtil.validate(finalModel.getSender());
                            MobileValidationUtil.validate(finalModel.getMobileNo());
                            MessageValidationUtil.validate(finalModel.getMessage());
                            // Deduct balance if applicable
                            if (privilegeList != null && privilegeList.contains("DEDUCT_BALANCE")) {
                                if (smsBal == null) {
                                    log.error("SMS balance is null for client: {}", client.getUserName());
                                    throw new IllegalStateException("SMS balance is not set for this client.");
                                }
                                smsBal--;
                            }
                            // Create entity
                            FinalModel entity = new FinalModel();
                            entity.setClient(client);
                            entity.setSender(finalModel.getSender());
                            entity.setMobileNo(finalModel.getMobileNo());
                            entity.setMessage(finalModel.getMessage());
                            entity.setRespId(UUID.randomUUID().toString());
                            entity.setReqDateTime(Instant.now());
                            return entity;
                        }).collect(Collectors.toList());
                // Update and save client balance
                client.setSmsBalance(smsBal);
                clientRepository.save(client);
                // Save entities to database
                finalRepository.saveAll(entities);
                // If multiple records, write to file
                if (payloadRequest.getPayload().size() > 1) {
                    Path filePath = Paths.get("payload_data.txt");
                    try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardOpenOption.CREATE,
                            StandardOpenOption.APPEND)) {
                        writer.write("Sender || Mobile Number || Message");
                        writer.newLine();
                        for (FinalModel entity : entities) {
                            writer.write(entity.getSender() + " || " + entity.getMobileNo() + " || "
                                    + entity.getMessage());
                            writer.newLine();
                        }
                    }
                    log.info("File created and data processed successfully!");
                }

                log.info("Payload processed successfully!");

            } catch (IllegalArgumentException e) {
                log.error("Validation failed for sender, mobile, or message", e);
                ResponseEntity.status(401).body(Map.of(
                        "status", HttpStatus.UNAUTHORIZED.value(),
                        "message", "Invalid Data in final model.",
                        "timestamp", Instant.now().toString()));
            } catch (IOException e) {
                log.error("Error writing payload data to file.", e);
            }

            return ResponseEntity.status(200).body(Map.of(
                    "status", 200,
                    "message", "Payload successfully stored in database.",
                    "timestamp", Instant.now().toString()));
        } catch (Exception e) {
            log.error("Error processing payload: ", e);
            // throw new RuntimeException("Failed to process payload", e);
            return ResponseEntity.status(500).body("Failed to process payload: " + e);
        } finally {
            stopWatch.stop(); // Stop stopwatch
            log.info("Processing time: {} ms", stopWatch.getTotalTimeMillis());
        }
    }
}

// if(HeaderValidationUtil.validateContentLength(encryptedPayload) != null){
// log.error("Request rejected: {} ",
// HeaderValidationUtil.validateContentLength(encryptedPayload));
// throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid content
// length");
// }

// Check if data is encrypted or not based on header isencrypted
// if ("1".equals(isEncrypted)) {
// // Decrypt the payload
// decryptedPayload = DecryptionUtil.decrypt(encryptedPayload);
// log.info("Decrypted Payload: {}", decryptedPayload);
// } else {
// decryptedPayload = encryptedPayload;
// }
// Parse decrypted payload into a PayloadRequest object
// objectMapper = new ObjectMapper();
// PayloadRequest payloadRequest = objectMapper.readValue(decryptedPayload,
// PayloadRequest.class);
// log.info("Parsed decrypted payload: {}", payloadRequest);
// System.out.println(payloadRequest);

// Converting data packet keys in lowercase
// @SuppressWarnings("rawtypes")
// Class packetKeys = payloadRequest.getClass();
// List<Field> allFields = Arrays.asList(packetKeys.getDeclaredFields());
// allFields.forEach(field -> field.getName().toLowerCase());

// log.info("All fields case: ", allFields.get(0));

// // Get all fields
// Class<?> packetKeys = payloadRequest.getClass();
// List<Field> allFields = Arrays.asList(packetKeys.getDeclaredFields());

// // Convert field names to lowercase and store in a Map
// Map<String, String> fieldNameMap = allFields.stream()
// .collect(Collectors.toMap(
// field -> field.getName(), // Original field name
// field -> field.getName().toLowerCase() // Lowercase field name
// ));

// // Log transformed field names
// fieldNameMap.forEach((original, lowercase) -> System.out
// .println("Original: " + original + " -> Lowercase: " + lowercase));

// Retrieve and decrypt API Key
// String encryptedApiKey = headers.getFirst("apikey");
// String decryptedApiKey = DecryptionUtil.decryptAPIKey(encryptedApiKey);
// log.info("Decrypted API Key: {}", decryptedApiKey);

// String userName = payloadRequest.getUserName();
// String password = payloadRequest.getPassword();

// // Check rate limit
// if (!rateLimiterService.allowRequest(userName)) {
// log.warn("Rate limit exceeded for UserName: {}", userName);
// return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
// .body("Too Many Requests");
// }

// ClientModel client = clientRepository.findByUserName(userName)
// .orElse(null);

// if (client == null) {
// log.warn("Invalid Username: {}", userName);
// return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
// .body(
// Map.of(
// "status", HttpStatus.UNAUTHORIZED.value(),
// "message", "Invalid Username",
// "timestamp", Instant.now().toString()));
// }

// memId = client.getMemId();
// smsBal = client.getSmsBalance();

// Optional<PrivilegeModel> priv =
// privilegeRepository.findByClient_MemId(memId);

// PrivilegeModel privileges = priv.get();
// List<String> privilegeList = privileges.getPrivileges();

// // Ensure API Key & Username belong to the same record
// if ((decryptedApiKey != null && decryptedApiKey.equals(client.getApiKey())
// && userName.equals(client.getUserName()))
// || (userName.equals(client.getUserName()) &&
// password.equals(client.getPwd()))) {
// log.info("User authentication successful.");
// payloadRequest.getPayload().forEach(finalModel -> {
// try {
// // Validate username and password in the payloadRequest
// UsernameValidationUtil.validate(payloadRequest.getUserName());
// PasswordValidationUtil.validate(payloadRequest.getPassword());
// } catch (IllegalArgumentException e) {
// log.error("Validation failed for username or password in model: {}",
// payloadRequest, e);
// // throw new IllegalArgumentException(
// // "Invalid username or password for payload: " + payloadRequest);
// ResponseEntity.status(401).body(Map.of(
// "status", HttpStatus.UNAUTHORIZED.value(),
// "message", "Invalid Username & Password",
// "timestamp", Instant.now().toString()));
// }
// });

// Approach 3: Using stream for List of FinalModel dto
// List<FinalModel> entities = payloadRequest.getPayload().stream()
// .map(finalModel -> {
// try {
// // Validate sender, mobile number, and message for each finalModel data
// SenderValidationUtil.validate(finalModel.getSender());
// MobileValidationUtil.validate(finalModel.getMobileNo());
// MessageValidationUtil.validate(finalModel.getMessage());
// // Check if privilegeList is non-null and contains "DEDUCT_BALANCE"
// if (privilegeList != null && !privilegeList.isEmpty()
// && privilegeList.contains("DEDUCT_BALANCE")) {
// smsBal--; // Ensure safe decrement
// }
// client.setSmsBalance(smsBal);
// clientRepository.save(client);
// } catch (IllegalArgumentException e) {
// log.error("Validation failed for sender, mobile, or message in finalModel:
// {}",
// finalModel, e);
// // throw new IllegalArgumentException("Invalid data in finalModel: " +
// // finalModel);
// ResponseEntity.status(401).body(Map.of(
// "status", HttpStatus.UNAUTHORIZED.value(),
// "message", "Invalid Data in final model.",
// "timestamp", Instant.now().toString()));
// }
// FinalModel entity = new FinalModel();
// entity.setClient(client);
// entity.setSender(finalModel.getSender());
// entity.setMobileNo(finalModel.getMobileNo());
// entity.setMessage(finalModel.getMessage());
// entity.setRespId(UUID.randomUUID().toString());
// entity.setReqDateTime(Instant.now());
// return entity;
// })
// .collect(Collectors.toList());

// finalRepository.saveAll(entities); // Batch save instead of multiple insert
// queries

// List<FinalModel> entities;
// if (payloadRequest.getPayload().size() == 1) {
// // Directly process to the database if only one record exists
// PayloadRequest.FinalModel finalModel = payloadRequest.getPayload().get(0);
// try {
// SenderValidationUtil.validate(finalModel.getSender());
// MobileValidationUtil.validate(finalModel.getMobileNo());
// MessageValidationUtil.validate(finalModel.getMessage());

// if (privilegeList != null && !privilegeList.isEmpty()
// && privilegeList.contains("DEDUCT_BALANCE")) {
// smsBal--;
// }
// client.setSmsBalance(smsBal);
// clientRepository.save(client);
// FinalModel entity = new FinalModel();
// entity.setClient(client);
// entity.setSender(finalModel.getSender());
// entity.setMobileNo(finalModel.getMobileNo());
// entity.setMessage(finalModel.getMessage());
// entity.setRespId(UUID.randomUUID().toString());
// entity.setReqDateTime(Instant.now());

// finalRepository.save(entity);

// } catch (IllegalArgumentException e) {
// log.error("Validation failed for sender, mobile or message in finalModel:
// {}", finalModel, e);
// ResponseEntity.status(401).body(Map.of(
// "status", HttpStatus.UNAUTHORIZED.value(),
// "message", "Invalid Data in final model.",
// "timestamp", Instant.now().toString()));
// }
// log.info("Payload processed directly to database successfully!!");
// } else {
// try (BufferedWriter writer = new BufferedWriter(new
// FileWriter("payload_data.txt", true))) {
// writer.write("Sender || Mobile Number || Message");
// writer.newLine();
// for (PayloadRequest.FinalModel finalModel : payloadRequest.getPayload()) {
// writer.write(finalModel.getSender() + " || " + finalModel.getMobileNo() + "
// || "
// + finalModel.getMessage());
// writer.newLine();
// }
// log.info("File created and data processed successfully!!");
// } catch (IOException e) {
// log.error("Error writing payload data to file.", e);
// }
// }
// // Process and save to database
// entities = payloadRequest.getPayload().stream()
// .map(finalModel -> {
// try {
// SenderValidationUtil.validate(finalModel.getSender());
// MobileValidationUtil.validate(finalModel.getMobileNo());
// MessageValidationUtil.validate(finalModel.getMessage());

// if (privilegeList != null && !privilegeList.isEmpty()
// && privilegeList.contains("DEDUCT_BALANCE")) {
// smsBal--; // Ensure safe decrement
// }
// client.setSmsBalance(smsBal);
// clientRepository.save(client);
// } catch (IllegalArgumentException e) {
// log.error("Validation failed for sender, mobile, or message in finalModel:
// {}",
// finalModel, e);
// ResponseEntity.status(401).body(Map.of(
// "status", HttpStatus.UNAUTHORIZED.value(),
// "message", "Invalid Data in final model.",
// "timestamp", Instant.now().toString()));
// }
// FinalModel entity = new FinalModel();
// entity.setClient(client);
// entity.setSender(finalModel.getSender());
// entity.setMobileNo(finalModel.getMobileNo());
// entity.setMessage(finalModel.getMessage());
// entity.setRespId(UUID.randomUUID().toString());
// entity.setReqDateTime(Instant.now());
// return entity;
// })
// .collect(Collectors.toList());

// finalRepository.saveAll(entities);

// }

// log.info("Payload successfully stored in database");
// return ResponseEntity.status(200).body(Map.of(
// "status", 200,
// "message", "Payload successfully stored in database.",
// "timestamp", Instant.now().toString()));

// }
// else {
// // throw new UnAuthorizedException("Invalid API Key or Credentials.");
// return ResponseEntity.status(401).body("Invalid API Key or Credentials");
// }

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

// } catch (Exception e) {
// log.error("Error processing payload: ", e);
// // throw new RuntimeException("Failed to process payload", e);
// return ResponseEntity.status(500).body("Failed to process payload: " + e);
// } finally {
// stopWatch.stop(); // Stop stopwatch
// log.info("Processing time: {} ms", stopWatch.getTotalTimeMillis());
// }