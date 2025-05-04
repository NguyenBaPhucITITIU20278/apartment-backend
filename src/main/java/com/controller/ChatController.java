package com.controller;

import com.dto.ChatMessageDTO;
import com.model.ChatMessage;
import com.model.ChatMessageReceiver;
import com.services.ChatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);

    @Autowired
    private ChatService chatService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat")
    public void processMessage(@Payload ChatMessageDTO chatMessage) {
        logger.info("=== Received WebSocket message ===");
        logger.info("Message content: {}", chatMessage.getContent());
        logger.info("From: {}", chatMessage.getSenderId());
        logger.info("To: {}", chatMessage.getAdminIds());

        try {
            chatService.sendMessage(chatMessage);
            logger.info("Message processed successfully");
        } catch (Exception e) {
            logger.error("Error processing message: ", e);
            throw e;
        }
    }

    @GetMapping("/history/{userId}/{adminId}")
    public ResponseEntity<List<ChatMessage>> getChatHistory(
            @PathVariable String userId,
            @PathVariable String adminId) {
        try {
            logger.debug("Getting chat history between user {} and admin {}", userId, adminId);
            return ResponseEntity.ok(chatService.getChatHistory(userId, adminId));
        } catch (Exception e) {
            logger.error("Error getting chat history: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/unread/{adminId}")
    public ResponseEntity<List<ChatMessageDTO>> getUnreadMessages(@PathVariable String adminId) {
        logger.debug("Getting unread messages for admin {}", adminId);
        return ResponseEntity.ok(chatService.getUnreadMessages(adminId));
    }

    @PostMapping("/read/{messageId}/{adminId}")
    public ResponseEntity<Void> markMessageAsRead(
            @PathVariable Long messageId,
            @PathVariable String adminId) {
        logger.debug("Marking message {} as read for admin {}", messageId, adminId);
        chatService.markMessageAsRead(messageId, adminId);

        // Gửi thông báo đã đọc đến client
        messagingTemplate.convertAndSendToUser(
                adminId,
                "/queue/read-receipts",
                Map.of("messageId", messageId)
        );

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/messages/{userId}/{adminId}")
    public ResponseEntity<Void> deleteAllMessages(
            @PathVariable String userId,
            @PathVariable String adminId) {
        try {
            logger.info("Deleting all messages between user {} and admin {}", userId, adminId);
            chatService.deleteAllMessages(userId, adminId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Error deleting messages: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ChatMessage> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("senderId") String senderId,
            @RequestParam("receiverId") String receiverId,
            @RequestParam("content") String content) {
        try {
            logger.info("=== Received file upload request ===");
            logger.info("From sender: {}", senderId);
            logger.info("To receiver: {}", receiverId);
            logger.info("File name: {}", file.getOriginalFilename());
            logger.info("File type: {}", file.getContentType());
            logger.info("File size: {}", file.getSize());

            ChatMessage message = chatService.sendFileMessage(file, senderId, receiverId, content);
            
            // Gửi tin nhắn qua WebSocket
            ChatMessageDTO messageDTO = new ChatMessageDTO();
            messageDTO.setId(message.getId());
            messageDTO.setContent(content);
            messageDTO.setSenderId(senderId);
            messageDTO.setReceiverId(receiverId);
            messageDTO.setTimestamp(message.getTimestamp());
            messageDTO.setFileUrl(message.getFileUrl());
            messageDTO.setFileType(message.getFileType());
            
            // Gửi tin nhắn cho người nhận
            logger.info("Sending file message to receiver: {}", receiverId);
            messagingTemplate.convertAndSendToUser(
                receiverId,
                "/queue/messages",
                messageDTO
            );
            
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            logger.error("Error uploading file: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
