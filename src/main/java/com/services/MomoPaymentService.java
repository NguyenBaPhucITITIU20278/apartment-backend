package com.services;

import com.config.MomoConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.model.*;
import com.repository.PaymentRepository;
import okhttp3.*;
import org.apache.commons.codec.digest.HmacUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class MomoPaymentService {
    private static final Logger logger = LoggerFactory.getLogger(MomoPaymentService.class);

    @Autowired
    private MomoConfig momoConfig;

    @Autowired
    private PaymentRepository paymentRepository;

    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public MomoPaymentResponse createListingPayment(String amount, String packageName, String duration) throws IOException {
        logger.info("Creating listing payment - Amount: {}, Package: {}, Duration: {}", amount, packageName, duration);
        
        // Create payment record first
        Payment payment = new Payment();
        payment.setAmount(new BigDecimal(amount));
        payment.setPaymentMethod("MOMO");
        payment.setDescription("Payment for " + packageName + " package (" + duration + " months)");
        payment.setStatus(PaymentStatus.PENDING);
        payment.setPaymentDate(LocalDateTime.now());
        payment.setPaymentType("LISTING");
        payment.setPackageInfo(packageName);
        payment.setDuration(Integer.parseInt(duration));
        
        logger.info("Saving payment record to database");
        payment = paymentRepository.save(payment);
        logger.info("Payment record saved with ID: {}", payment.getId());

        // Create MoMo payment request
        MomoPaymentRequest request = new MomoPaymentRequest();
        request.setPartnerCode(momoConfig.getPartnerCode());
        request.setOrderId(payment.getId() + "_" + System.currentTimeMillis());
        request.setOrderInfo(payment.getDescription());
        request.setAccessKey(momoConfig.getAccessKey());
        request.setAmount(amount);
        request.setRequestId(UUID.randomUUID().toString());
        request.setExtraData("");
        request.setIpnUrl(momoConfig.getNotifyUrl());
        request.setRedirectUrl(momoConfig.getReturnUrl());
        request.setRequestType("captureWallet");

        String rawSignature = "accessKey=" + request.getAccessKey() +
                "&amount=" + request.getAmount() +
                "&extraData=" + request.getExtraData() +
                "&ipnUrl=" + request.getIpnUrl() +
                "&orderId=" + request.getOrderId() +
                "&orderInfo=" + request.getOrderInfo() +
                "&partnerCode=" + request.getPartnerCode() +
                "&redirectUrl=" + request.getRedirectUrl() +
                "&requestId=" + request.getRequestId() +
                "&requestType=" + request.getRequestType();

        logger.info("Raw signature string: {}", rawSignature);
        
        String signature = new HmacUtils("HmacSHA256", momoConfig.getSecretKey())
                .hmacHex(rawSignature);
        request.setSignature(signature);
        
        logger.info("Generated signature: {}", signature);

        // Create HTTP request
        String requestBody = objectMapper.writeValueAsString(request);
        logger.info("Request body to MoMo: {}", requestBody);
        
        RequestBody body = RequestBody.create(
                requestBody,
                MediaType.parse("application/json; charset=utf-8")
        );

        Request httpRequest = new Request.Builder()
                .url(momoConfig.getEndpoint())
                .post(body)
                .build();

        try (Response response = client.newCall(httpRequest).execute()) {
            String responseBody = response.body().string();
            logger.info("Response from MoMo: {}", responseBody);
            
            MomoPaymentResponse momoResponse = objectMapper.readValue(responseBody, MomoPaymentResponse.class);
            
            // Update payment status based on MoMo response
            if ("0".equals(momoResponse.getResultCode())) {
                payment.setTransactionId(momoResponse.getRequestId());
                logger.info("Payment request successful. Transaction ID: {}", momoResponse.getRequestId());
            } else {
                payment.setStatus(PaymentStatus.FAILED);
                payment.setDescription(payment.getDescription() + " - Error: " + momoResponse.getMessage());
                logger.error("Payment request failed. Error: {}", momoResponse.getMessage());
                if (momoResponse.getSubErrors() != null && !momoResponse.getSubErrors().isEmpty()) {
                    logger.error("Sub errors: {}", momoResponse.getSubErrors());
                }
            }
            paymentRepository.save(payment);
            
            return momoResponse;
        } catch (Exception e) {
            logger.error("Error while processing MoMo payment", e);
            throw e;
        }
    }

    public boolean verifyPaymentResponse(String amount, String orderId, String requestId, 
                                       String orderInfo, String orderType, String transId, 
                                       String resultCode, String message, String responseSignature) {
        logger.info("Verifying payment response - OrderId: {}, Amount: {}", orderId, amount);
        
        // Verify signature
        String rawSignature = "accessKey=" + momoConfig.getAccessKey() +
                "&amount=" + amount +
                "&extraData=" +
                "&message=" + message +
                "&orderId=" + orderId +
                "&orderInfo=" + orderInfo +
                "&orderType=" + orderType +
                "&partnerCode=" + momoConfig.getPartnerCode() +
                "&payType=qr" +
                "&requestId=" + requestId +
                "&responseTime=" + System.currentTimeMillis() +
                "&resultCode=" + resultCode +
                "&transId=" + transId;

        String signature = new HmacUtils("HmacSHA256", momoConfig.getSecretKey())
                .hmacHex(rawSignature);

        if (signature.equals(responseSignature)) {
            // Update payment status
            try {
                // Extract payment ID from orderId (format: paymentId_timestamp)
                String paymentId = orderId.split("_")[0];
                Payment payment = paymentRepository.findById(Long.parseLong(paymentId))
                    .orElseThrow(() -> new RuntimeException("Payment not found"));
                
                if ("0".equals(resultCode)) {
                    payment.setStatus(PaymentStatus.COMPLETED);
                    payment.setTransactionId(transId);
                    logger.info("Payment verified and completed successfully");
                } else {
                    payment.setStatus(PaymentStatus.FAILED);
                    payment.setDescription(payment.getDescription() + " - Error: " + message);
                    logger.error("Payment verification failed with result code: {}", resultCode);
                }
                
                paymentRepository.save(payment);
                return true;
            } catch (Exception e) {
                logger.error("Error while updating payment status", e);
                return false;
            }
        }
        
        logger.error("Payment signature verification failed");
        return false;
    }

    public Payment getPaymentById(Long paymentId) {
        return paymentRepository.findById(paymentId)
            .orElseThrow(() -> new RuntimeException("Payment not found"));
    }

    public void handlePaymentCancellation(Long paymentId) {
        logger.info("Processing payment cancellation for payment ID: {}", paymentId);
        
        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new RuntimeException("Payment not found"));
            
        // Update payment status to COMPLETED
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setDescription(payment.getDescription() + " (Manually marked as completed)");
        payment.setTransactionId("MANUAL_COMPLETION_" + System.currentTimeMillis());
        
        logger.info("Updating payment status to COMPLETED for payment ID: {}", paymentId);
        paymentRepository.save(payment);
        logger.info("Payment status updated successfully");
    }
} 