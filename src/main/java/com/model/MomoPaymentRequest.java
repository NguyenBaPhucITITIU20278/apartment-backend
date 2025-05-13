package com.model;

import lombok.Data;

@Data
public class MomoPaymentRequest {
    private String partnerCode;
    private String orderId;
    private String orderInfo;
    private String accessKey;
    private String amount;
    private String signature;
    private String extraData;
    private String requestId;
    private String notifyUrl;
    private String redirectUrl;
    private String requestType;
    private String ipnUrl;
} 