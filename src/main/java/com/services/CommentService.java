package com.services;

import com.dto.CommentDTO;
import com.model.Room;
import com.model.UserEntity;
import com.repository.RoomRepository;
import com.repository.UserRepository;
import com.model.Comment;
import com.repository.CommentRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<CommentDTO> getCommentsByRoomId(Long roomId) {
        log.info("Fetching comments for room ID: {}", roomId);
        List<Comment> comments = commentRepository.findByRoomIdOrderByCreatedAtDesc(roomId);
        log.info("Found {} comments for room ID: {}", comments.size(), roomId);
        return comments.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public CommentDTO createComment(Long roomId, String username, String content) {
        log.info("Creating comment for room ID: {}, username: {}", roomId, username);
        try {
            Room room = roomRepository.findById(roomId)
                    .orElseThrow(() -> new RuntimeException("Room not found with ID: " + roomId));
            log.debug("Found room: {}", room.getId());

            UserEntity user = userRepository.findById(username)
                    .orElseThrow(() -> new RuntimeException("User not found with username: " + username));
            log.debug("Found user: {}", user.getUserName());

            Comment comment = new Comment();
            comment.setRoom(room);
            comment.setUser(user);
            comment.setContent(content);

            Comment savedComment = commentRepository.save(comment);
            log.info("Successfully created comment with ID: {} for room: {}", savedComment.getId(), roomId);
            
            CommentDTO dto = convertToDTO(savedComment);
            log.debug("Converted to DTO: {}", dto);
            return dto;
        } catch (Exception e) {
            log.error("Error creating comment for room ID: " + roomId, e);
            throw e;
        }
    }

    private CommentDTO convertToDTO(Comment comment) {
        try {
            CommentDTO dto = CommentDTO.fromEntity(
                    comment.getId(),
                    comment.getRoom().getId(),
                    comment.getUser().getUserName(),
                    comment.getContent(),
                    comment.getCreatedAt(),
                    comment.getUpdatedAt()
            );
            log.debug("Successfully converted comment ID: {} to DTO", comment.getId());
            return dto;
        } catch (Exception e) {
            log.error("Error converting comment to DTO: " + comment.getId(), e);
            throw e;
        }
    }
} 