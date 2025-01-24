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

    public @NotBlank @Size(max = 50) String getUserName() {
        return userName;
    }

    public @NotBlank String getPwd() {
        return pwd;
    }


    @NotBlank
    private String pwd;
}
