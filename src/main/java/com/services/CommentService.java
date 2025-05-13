package com.services;

import com.dto.CommentDTO;
import com.model.Room;
import com.model.UserEntity;
import com.repository.RoomRepository;
import com.repository.UserRepository;
import com.model.Comment;
import com.repository.CommentRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<CommentDTO> getCommentsByRoomId(Long roomId) {
        return commentRepository.findByRoomIdOrderByCreatedAtDesc(roomId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public CommentDTO createComment(Long roomId, String username, String content) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));
        UserEntity user = userRepository.findById(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Comment comment = new Comment();
        comment.setRoom(room);
        comment.setUser(user);
        comment.setContent(content);

        Comment savedComment = commentRepository.save(comment);
        return convertToDTO(savedComment);
    }

    private CommentDTO convertToDTO(Comment comment) {
        return new CommentDTO(
                comment.getId(),
                comment.getRoom().getId(),
                comment.getUser().getUserName(),
                comment.getContent(),
                comment.getCreatedAt(),
                comment.getUpdatedAt()
        );
    }
} 