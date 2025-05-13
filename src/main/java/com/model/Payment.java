package com.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "room_id")
    private Room room;

    private BigDecimal amount;
    
    private String paymentMethod; // CASH, BANK_TRANSFER, CREDIT_CARD
    
    private String description;
    
    private LocalDateTime paymentDate;
    
    @Enumerated(EnumType.STRING)
    private PaymentStatus status;
    
    private String transactionId; // For tracking bank transfers or card payments
    
    @Column(name = "payment_type")
    private String paymentType; // LISTING or ROOM
    
    @Column(name = "package_info")
    private String packageInfo; // STANDARD, PREMIUM, DELUXE
    
    private Integer duration; // Duration in months
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
} 