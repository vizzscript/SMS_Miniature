package com.pinnacle.backend.dto;

import lombok.Data;
import java.util.List;


@Data
public class PayloadRequest {
    private String username;
    private String password;
    private List<FinalModel> payload;

    @Data
    public static class FinalModel {
        private Long mobileNo;
        private String message;
        private String sender;
    }
}