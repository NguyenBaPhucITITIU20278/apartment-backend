package com.dto;

import java.time.LocalDateTime;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDTO {
    private Long id;
    private String content;
    private String senderId;
    private String receiverId; // ID của người nhận tin nhắn
    private LocalDateTime timestamp;
    private Set<String> adminIds; // Danh sách admin nhận tin nhắn
    private boolean isRead;
    private String fileUrl; // URL của file đã upload
    private String fileType; // Loại file (MIME type)
} 