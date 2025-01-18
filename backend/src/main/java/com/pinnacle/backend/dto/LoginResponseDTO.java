package com.pinnacle.backend.dto;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponseDTO {
    private Long memId;
    private String userName;
    private String apiKey;
    private Instant lastLogin;
}