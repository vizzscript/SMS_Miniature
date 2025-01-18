package com.pinnacle.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SignupRequestDTO {
    @NotBlank
    @Size(max = 50)
    private String userName;

    @NotBlank
    private String pwd;
}
