package com.pinnacle.backend.dto;

import lombok.Data;
import java.util.List;


@Data
public class PayloadRequest {
    private String userName;
    private String password;
    private List<FinalModel> payload;

    @Data
    public static class FinalModel {
        private String mobileNo;
        private String message;
        private String sender;
    }
}