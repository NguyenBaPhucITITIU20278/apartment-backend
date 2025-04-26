package com.services;

import com.dto.PaymentDTO;
import com.model.PaymentStatus;
import java.time.LocalDateTime;
import java.util.List;

public interface PaymentService {
    PaymentDTO createPayment(PaymentDTO paymentDTO);
    PaymentDTO updatePayment(Long id, PaymentDTO paymentDTO);
    PaymentDTO getPaymentById(Long id);
    List<PaymentDTO> getAllPayments();
    List<PaymentDTO> getPaymentsByRoomId(Long roomId);
    List<PaymentDTO> getPaymentsByStatus(PaymentStatus status);
    List<PaymentDTO> getPaymentsByDateRange(LocalDateTime startDate, LocalDateTime endDate);
    void deletePayment(Long id);
    PaymentDTO updatePaymentStatus(Long id, PaymentStatus status);
} 