package com.pinnacle.backend.service.impl;

import java.time.Instant;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.pinnacle.backend.dto.PayloadRequest;
import com.pinnacle.backend.exceptions.UnAuthorizedException;
import com.pinnacle.backend.model.ClientModel;
import com.pinnacle.backend.model.FinalModel;
import com.pinnacle.backend.repository.ClientRepository;
import com.pinnacle.backend.repository.FinalRepository;
import com.pinnacle.backend.service.PayloadService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PayloadServiceImpl implements PayloadService {
    @Autowired
    private final ClientRepository clientRepository;
    @Autowired
    private final FinalRepository finalRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

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
            entity.setClient(client);  // Associate with client
            entity.setMobileNo(dto.getMobileNo());
            entity.setMessage(dto.getMessage());
            entity.setSender(dto.getSender());
            entity.setRespId(UUID.randomUUID().toString());
            entity.setReqDateTime(Instant.now());
            finalRepository.save(entity);
        });
    }
}
