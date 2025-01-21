package com.pinnacle.backend.service;

import com.pinnacle.backend.dto.LoginRequestDTO;
import com.pinnacle.backend.dto.LoginResponseDTO;
import com.pinnacle.backend.dto.SignupRequestDTO;
import com.pinnacle.backend.dto.SignupResponseDTO;

public interface ClientService {
    SignupResponseDTO signup(SignupRequestDTO signupRequest, String ipAddress) throws Exception;

    LoginResponseDTO login(LoginRequestDTO loginRequest, String ipAddress) throws Exception;

    public String getHello();

}
