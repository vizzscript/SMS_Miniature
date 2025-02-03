package com.pinnacle.backend.service.impl;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.pinnacle.backend.dto.LoginRequestDTO;
import com.pinnacle.backend.dto.LoginResponseDTO;
import com.pinnacle.backend.dto.SignupRequestDTO;
import com.pinnacle.backend.dto.SignupResponseDTO;
import com.pinnacle.backend.exceptions.InvalidPasswordException;
import com.pinnacle.backend.exceptions.UserNameAlreadyExistsException;
import com.pinnacle.backend.exceptions.UserNotFoundException;
import com.pinnacle.backend.model.ClientModel;
import com.pinnacle.backend.repository.ClientRepository;
import com.pinnacle.backend.service.ClientService;

@Service
public class ClientServiceImpl implements ClientService {
    @Autowired
    private ClientRepository clientRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public SignupResponseDTO signup(SignupRequestDTO signupRequest, String ipAddress) throws UserNameAlreadyExistsException {
        if (clientRepo.existsByUserName(signupRequest.getUserName())) {
            throw new UserNameAlreadyExistsException("Username already exists!!");
        }

        String hashedPassword = passwordEncoder.encode(signupRequest.getPwd());

        ClientModel client = new ClientModel();
        client.setUserName(signupRequest.getUserName());
        client.setPwd(hashedPassword);
        client.setStatus(true);
        client.setCreatedAt(Instant.now());
        client.setApiKey(UUID.randomUUID().toString());
        client.setIpLogin(ipAddress);

        clientRepo.save(client);

        SignupResponseDTO response = new SignupResponseDTO();
        response.setUserName(client.getUserName());
        response.setMemId(client.getMemId());
        response.setApiKey(client.getApiKey());

        return response;
    }

    @Override
    public LoginResponseDTO login(LoginRequestDTO loginRequest, String ipAddress) throws UserNotFoundException, InvalidPasswordException {
        Optional<ClientModel> optionalClient = clientRepo.findByUserName(loginRequest.getUserName());
        if (!optionalClient.isPresent()) {
            throw new UserNotFoundException("User not found!!");
        }

        ClientModel client = optionalClient.get();
        if (!passwordEncoder.matches(loginRequest.getPwd(), client.getPwd())) {
            throw new InvalidPasswordException("Invalid Password!!");
        }

        

        client.setLastLogin(Instant.now());
        client.setIpLogin(ipAddress);
        clientRepo.save(client);

        LoginResponseDTO response = new LoginResponseDTO();
        response.setMemId(client.getMemId());
        response.setUserName(client.getUserName());
        response.setApiKey(client.getApiKey());
        response.setLastLogin(client.getLastLogin());

        return response;
    }

    @Override
    public String getHello() {
        return "Hello, Pinnacle!!";
    }

}
