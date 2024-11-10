package com.boit_droid.wallet.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ApiResponse {

    private Boolean success;
    private String message;
    private String requestId;
    private String locale;
    private String salt;
    private String signature;
}

