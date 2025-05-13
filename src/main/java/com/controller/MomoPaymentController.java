package com.controller;

import com.model.MomoPaymentResponse;
import com.model.Payment;
import com.services.MomoPaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/payment/momo")
public class MomoPaymentController {

    @Autowired
    private MomoPaymentService momoPaymentService;

    @PostMapping("/create")
    public ResponseEntity<MomoPaymentResponse> createPayment(
            @RequestBody Map<String, String> paymentRequest) throws IOException {
        String amount = paymentRequest.get("amount");
        String packageName = paymentRequest.get("packageName"); // STANDARD, PREMIUM, DELUXE
        String duration = paymentRequest.get("duration"); // Number of months
        
        MomoPaymentResponse response = momoPaymentService.createListingPayment(amount, packageName, duration);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/notify")
    public ResponseEntity<String> paymentNotification(
            @RequestParam String amount,
            @RequestParam String orderId,
            @RequestParam String requestId,
            @RequestParam String orderInfo,
            @RequestParam String orderType,
            @RequestParam String transId,
            @RequestParam String resultCode,
            @RequestParam String message,
            @RequestParam String signature) {

        boolean isValid = momoPaymentService.verifyPaymentResponse(
                amount, orderId, requestId, orderInfo, orderType,
                transId, resultCode, message, signature);

        if (isValid) {
            return ResponseEntity.ok("Payment processed successfully");
        } else {
            return ResponseEntity.badRequest().body("Payment verification failed");
        }
    }

    @GetMapping("/result")
    public ResponseEntity<String> paymentResult(
            @RequestParam(required = false) String resultCode,
            @RequestParam(required = false) String orderId) {
        
        if ("0".equals(resultCode)) {
            // Get payment details
            try {
                Long paymentId = Long.parseLong(orderId.split("_")[0]);
                Payment payment = momoPaymentService.getPaymentById(paymentId);
                return ResponseEntity.ok("Payment successful for " + payment.getPackageInfo() + " package");
            } catch (Exception e) {
                return ResponseEntity.ok("Payment successful but failed to get details");
            }
        } else {
            // Handle payment cancellation or failure
            try {
                Long paymentId = Long.parseLong(orderId.split("_")[0]);
                // Call cancel payment API
                momoPaymentService.handlePaymentCancellation(paymentId);
                return ResponseEntity.ok("Payment cancelled and marked as completed");
            } catch (Exception e) {
                return ResponseEntity.badRequest().body("Failed to process payment cancellation: " + e.getMessage());
            }
        }
    }

    @PostMapping("/cancel/{paymentId}")
    public ResponseEntity<String> cancelPayment(@PathVariable Long paymentId) {
        try {
            momoPaymentService.handlePaymentCancellation(paymentId);
            return ResponseEntity.ok("Payment cancelled and marked as successful");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to process payment cancellation: " + e.getMessage());
        }
    }
} 