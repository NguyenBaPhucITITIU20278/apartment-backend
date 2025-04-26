package com.repository;

import com.model.Payment;
import com.model.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByRoomId(Long roomId);
    List<Payment> findByStatus(PaymentStatus status);
    List<Payment> findByPaymentDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    List<Payment> findByRoomIdAndStatus(Long roomId, PaymentStatus status);
} 