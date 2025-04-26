package com.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MomoPaymentRequest {
    private String partnerCode;
    private String orderId;
    private String orderInfo;
    private String redirectUrl;
    private String ipnUrl;
    private Long amount;
    private String requestType;
    private String requestId;
    private String extraData;
    private String signature;
    private String lang;
    private String paymentCode;
} 