package com.services.impl;

import com.dto.PaymentDTO;
import com.model.Room;
import com.model.Payment;
import com.model.PaymentStatus;
import com.repository.RoomRepository;
import com.repository.PaymentRepository;
import com.services.PaymentService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Override
    @Transactional
    public PaymentDTO createPayment(PaymentDTO paymentDTO) {
        Payment payment = new Payment();
        BeanUtils.copyProperties(paymentDTO, payment);
        
        // Fetch and set the room
        Room room = roomRepository.findById(paymentDTO.getRoomId())
            .orElseThrow(() -> new EntityNotFoundException("Room not found"));
        payment.setRoom(room);
        
        // Set initial status if not provided
        if (payment.getStatus() == null) {
            payment.setStatus(PaymentStatus.PENDING);
        }
        
        payment = paymentRepository.save(payment);
        return convertToDTO(payment);
    }

    @Override
    @Transactional
    public PaymentDTO updatePayment(Long id, PaymentDTO paymentDTO) {
        Payment payment = paymentRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Payment not found"));
        
        BeanUtils.copyProperties(paymentDTO, payment, "id", "createdAt");
        
        if (paymentDTO.getRoomId() != null) {
            Room room = roomRepository.findById(paymentDTO.getRoomId())
                .orElseThrow(() -> new EntityNotFoundException("Room not found"));
            payment.setRoom(room);
        }
        
        payment = paymentRepository.save(payment);
        return convertToDTO(payment);
    }

    @Override
    public PaymentDTO getPaymentById(Long id) {
        Payment payment = paymentRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Payment not found"));
        return convertToDTO(payment);
    }

    @Override
    public List<PaymentDTO> getAllPayments() {
        return paymentRepository.findAll().stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    @Override
    public List<PaymentDTO> getPaymentsByRoomId(Long roomId) {
        return paymentRepository.findByRoomId(roomId).stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    @Override
    public List<PaymentDTO> getPaymentsByStatus(PaymentStatus status) {
        return paymentRepository.findByStatus(status).stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    @Override
    public List<PaymentDTO> getPaymentsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return paymentRepository.findByPaymentDateBetween(startDate, endDate).stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deletePayment(Long id) {
        Payment payment = paymentRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Payment not found"));
        paymentRepository.delete(payment);
    }

    @Override
    @Transactional
    public PaymentDTO updatePaymentStatus(Long id, PaymentStatus status) {
        Payment payment = paymentRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Payment not found"));
        payment.setStatus(status);
        payment = paymentRepository.save(payment);
        return convertToDTO(payment);
    }

    private PaymentDTO convertToDTO(Payment payment) {
        PaymentDTO dto = new PaymentDTO();
        BeanUtils.copyProperties(payment, dto);
        dto.setRoomId(payment.getRoom().getId());
        return dto;
    }
} 