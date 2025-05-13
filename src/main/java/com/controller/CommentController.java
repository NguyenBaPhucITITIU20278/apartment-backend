package com.controller;

import com.dto.CommentDTO;
import com.services.CommentService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    @GetMapping("/room/{roomId}")
    public ResponseEntity<List<CommentDTO>> getCommentsByRoomId(@PathVariable Long roomId) {
        return ResponseEntity.ok(commentService.getCommentsByRoomId(roomId));
    }

    @PostMapping
    public ResponseEntity<CommentDTO> createComment(
            @RequestParam Long roomId,
            @RequestParam String username,
            @RequestParam String content) {
        return ResponseEntity.ok(commentService.createComment(roomId, username, content));
    }
} 