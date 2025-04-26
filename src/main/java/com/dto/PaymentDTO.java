package com.dto;

import com.model.PaymentStatus;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaymentDTO {
    private Long id;
    private Long roomId;
    private BigDecimal amount;
    private String paymentMethod;
    private String description;
    private LocalDateTime paymentDate;
    private PaymentStatus status;
    private String transactionId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 