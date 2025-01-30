package com.pinnacle.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.pinnacle.backend.dto.LoginRequestDTO;
import com.pinnacle.backend.dto.LoginResponseDTO;
import com.pinnacle.backend.dto.SignupRequestDTO;
import com.pinnacle.backend.dto.SignupResponseDTO;
import com.pinnacle.backend.service.ClientService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/client")
@Slf4j
@CrossOrigin(origins = "http://localhost:5173")
public class ClientController {

    @Autowired
    private ClientService clientService;

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public SignupResponseDTO signup(@RequestBody @Valid SignupRequestDTO request, HttpServletRequest httpServletRequest)
            throws Exception {
        log.info("Incoming Signup Data: {}", request);
        System.out.println("Signup method invoked.");
        String ipAddress = httpServletRequest.getRemoteAddr();
        return clientService.signup(request, ipAddress);
    }

    @PostMapping("/login")
    public LoginResponseDTO login(@RequestBody @Valid LoginRequestDTO request, HttpServletRequest httpServletRequest)
            throws Exception {
        log.info("Incoming Login Data: {}", request);
        System.out.println("Login method invoked.");
        String ipAddress = httpServletRequest.getRemoteAddr();
        return clientService.login(request, ipAddress);
    }

    @GetMapping("/hello")
    public String getHello() {
        return clientService.getHello();
    }

}
