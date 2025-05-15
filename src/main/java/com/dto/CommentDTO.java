package com.dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentDTO {
    private Long id;
    private Long roomId;
    private String username;
    private String content;
    private String createdAt;
    private String updatedAt;

    public static CommentDTO fromEntity(Long id, Long roomId, String username, String content, 
            LocalDateTime createdAt, LocalDateTime updatedAt) {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
        return new CommentDTO(
            id,
            roomId,
            username,
            content,
            createdAt != null ? createdAt.format(formatter) : null,
            updatedAt != null ? updatedAt.format(formatter) : null
        );
    }
} 