package com.dto;

import java.time.LocalDateTime;
import java.time.ZoneId;
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
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        return new CommentDTO(
            id,
            roomId,
            username,
            content,
            createdAt != null ? createdAt.atZone(ZoneId.systemDefault())
                                        .withZoneSameInstant(ZoneId.of("UTC"))
                                        .format(formatter) : null,
            updatedAt != null ? updatedAt.atZone(ZoneId.systemDefault())
                                       .withZoneSameInstant(ZoneId.of("UTC"))
                                       .format(formatter) : null
        );
    }
} 