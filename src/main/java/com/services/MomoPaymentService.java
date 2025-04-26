package com.services;

import com.config.MomoConfig;
import com.dto.MomoPaymentRequest;
import com.dto.PaymentDTO;
import com.model.Payment;
import com.model.PaymentStatus;
import org.apache.commons.codec.digest.HmacUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Service
public class MomoPaymentService {

    @Autowired
    private MomoConfig momoConfig;

    @Autowired
    private PaymentService paymentService;

    public String createMomoPayment(PaymentDTO paymentDTO) {
        // Tạo payment record với trạng thái PENDING
        paymentDTO.setStatus(PaymentStatus.PENDING);
        paymentDTO.setPaymentMethod("MOMO");
        PaymentDTO savedPayment = paymentService.createPayment(paymentDTO);

        // Tạo thông tin thanh toán MoMo
        String orderId = String.valueOf(savedPayment.getId());
        String requestId = UUID.randomUUID().toString();
        String orderInfo = "Thanh toan tien phong: " + paymentDTO.getDescription();
        
        // Tạo signature
        String rawSignature = "accessKey=" + momoConfig.getAccessKey() +
                "&amount=" + paymentDTO.getAmount() +
                "&extraData=" +
                "&ipnUrl=" + momoConfig.getNotifyUrl() +
                "&orderId=" + orderId +
                "&orderInfo=" + orderInfo +
                "&partnerCode=" + momoConfig.getPartnerCode() +
                "&redirectUrl=" + momoConfig.getReturnUrl() +
                "&requestId=" + requestId +
                "&requestType=captureWallet";

        String signature = new HmacUtils("HmacSHA256", momoConfig.getSecretKey())
                .hmacHex(rawSignature);

        // Tạo request body
        MomoPaymentRequest momoRequest = MomoPaymentRequest.builder()
                .partnerCode(momoConfig.getPartnerCode())
                .orderId(orderId)
                .orderInfo(orderInfo)
                .redirectUrl(momoConfig.getReturnUrl())
                .ipnUrl(momoConfig.getNotifyUrl())
                .amount(paymentDTO.getAmount().longValue())
                .requestType("captureWallet")
                .requestId(requestId)
                .extraData("")
                .signature(signature)
                .lang("vi")
                .build();

        // Gọi API MoMo
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<MomoPaymentRequest> entity = new HttpEntity<>(momoRequest, headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.postForEntity(
                momoConfig.getApiEndpoint(), 
                entity, 
                String.class
        );

        return response.getBody();
    }

    public void handleMomoCallback(String orderId, String resultCode) {
        Long paymentId = Long.valueOf(orderId);
        if ("0".equals(resultCode)) {
            // Thanh toán thành công
            paymentService.updatePaymentStatus(paymentId, PaymentStatus.COMPLETED);
        } else {
            // Thanh toán thất bại
            paymentService.updatePaymentStatus(paymentId, PaymentStatus.FAILED);
        }
    }
} 