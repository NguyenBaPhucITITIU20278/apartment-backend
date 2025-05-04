package com.services;

import com.dto.ChatMessageDTO;
import com.model.ChatMessage;
import com.model.ChatMessageReceiver;
import com.model.UserEntity;
import com.repository.ChatMessageRepository;
import com.repository.ChatMessageReceiverRepository;
import com.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.web.multipart.MultipartFile;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import java.util.UUID;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

@Service
public class ChatService {

    private static final Logger logger = LoggerFactory.getLogger(ChatService.class);

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private ChatMessageReceiverRepository receiverRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Transactional
    public void sendMessage(ChatMessageDTO messageDTO) {
        try {
            logger.info("=== Start sending message ===");
            logger.info("Message content: {}", messageDTO.getContent());
            logger.info("From: {}", messageDTO.getSenderId());
            logger.info("To: {}", messageDTO.getReceiverId() != null ? messageDTO.getReceiverId() : "all admins");

            // Create new message
            ChatMessage message = new ChatMessage();
            message.setContent(messageDTO.getContent());
            message.setSenderId(messageDTO.getSenderId());
            message.setTimestamp(LocalDateTime.now());

            // Save message
            logger.info("Saving message to database...");
            ChatMessage savedMessage = chatMessageRepository.save(message);
            logger.info("Message saved with ID: {}", savedMessage.getId());

            // Create message receiver
            ChatMessageReceiver receiver = new ChatMessageReceiver();
            receiver.setMessage(savedMessage);
            // Set the correct receiver ID (admin for user messages, specific user for admin messages)
            String receiverId = messageDTO.getReceiverId() != null ? 
                messageDTO.getReceiverId() : "admin";
            receiver.setReceiverId(receiverId);
            receiver.setRead(false);
            receiverRepository.save(receiver);
            logger.info("Created receiver entry with receiverId: {}", receiverId);

            // Send message via WebSocket
            messageDTO.setId(savedMessage.getId());
            messageDTO.setTimestamp(savedMessage.getTimestamp());
            messagingTemplate.convertAndSendToUser(
                receiverId,
                "/queue/messages",
                messageDTO
            );
            logger.info("Message sent to receiver: {}", receiverId);
        } catch (Exception e) {
            logger.error("Error in sendMessage: ", e);
            throw e;
        }
    }

    public List<ChatMessage> getChatHistory(String userId, String adminId) {
        try {
            logger.info("=== Starting to fetch chat history ===");
            logger.info("Parameters - userId: {}, adminId: {}", userId, adminId);
            
            if (userId == null || adminId == null) {
                logger.error("Invalid parameters - userId or adminId is null");
                throw new IllegalArgumentException("userId and adminId cannot be null");
            }
            
            // Get all messages where either user is sender and admin is receiver
            // or admin is sender and user is receiver
            List<ChatMessage> allMessages = chatMessageRepository.findAll();
            List<ChatMessageReceiver> allReceivers = receiverRepository.findAll();
            
            Set<Long> validMessageIds = allReceivers.stream()
                .filter(receiver -> {
                    ChatMessage msg = receiver.getMessage();
                    if (msg == null) return false;
                    
                    // Case 1: User sent to admin
                    boolean userToAdmin = msg.getSenderId().equals(userId) && 
                                       receiver.getReceiverId().equals(adminId);
                    
                    // Case 2: Admin sent to user
                    boolean adminToUser = msg.getSenderId().equals(adminId) && 
                                       receiver.getReceiverId().equals(userId);
                    
                    return userToAdmin || adminToUser;
                })
                .map(receiver -> receiver.getMessage().getId())
                .collect(Collectors.toSet());
            
            List<ChatMessage> chatHistory = allMessages.stream()
                .filter(msg -> validMessageIds.contains(msg.getId()))
                .sorted(Comparator.comparing(ChatMessage::getTimestamp))
                .collect(Collectors.toList());
            
            logger.info("Found {} messages in chat history", chatHistory.size());
            chatHistory.forEach(msg -> {
                logger.debug("Final message - ID: {}, From: {}, Content: {}, Time: {}", 
                    msg.getId(), 
                    msg.getSenderId(),
                    msg.getContent(), 
                    msg.getTimestamp());
            });
            
            return chatHistory;
        } catch (Exception e) {
            logger.error("Error while fetching chat history: ", e);
            throw e;
        }
    }

    public List<ChatMessageDTO> getUnreadMessages(String adminId) {
        try {
            logger.info("=== Fetching unread messages for admin ===");
            logger.info("Admin ID: {}", adminId);
            
            List<ChatMessageReceiver> unreadMessages = receiverRepository.findByReceiverIdAndIsReadFalse(adminId);
            logger.info("Found {} unread messages", unreadMessages.size());
            
            return unreadMessages.stream()
                .map(receiver -> {
                    ChatMessageDTO dto = new ChatMessageDTO();
                    try {
                        ChatMessage message = receiver.getMessage();
                        if (message != null) {
                            dto.setId(message.getId());
                            dto.setContent(message.getContent());
                            dto.setSenderId(message.getSenderId());
                            dto.setTimestamp(message.getTimestamp());
                            dto.setRead(receiver.isRead());
                        } else {
                            logger.warn("Found receiver with null message, ID: {}", receiver.getId());
                        }
                    } catch (Exception e) {
                        logger.error("Error mapping message to DTO: {}", e.getMessage());
                    }
                    return dto;
                })
                .filter(dto -> dto.getId() != null) // Filter out any failed conversions
                .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error while fetching unread messages: ", e);
            throw new RuntimeException("Failed to fetch unread messages", e);
        }
    }

    @Transactional
    public void markMessageAsRead(Long messageId, String adminId) {
        try {
            logger.info("=== Marking message as read ===");
            logger.info("Message ID: {}, Admin ID: {}", messageId, adminId);
            
            ChatMessageReceiver receiver = receiverRepository.findByMessage_IdAndReceiverId(messageId, adminId)
                .orElseThrow(() -> new RuntimeException("Message receiver not found"));
            
            receiver.setRead(true);
            receiverRepository.save(receiver);
            logger.info("Message marked as read successfully");
        } catch (Exception e) {
            logger.error("Error while marking message as read: ", e);
            throw e;
        }
    }

    @Value("${aws.s3.bucket}")
    private String bucketName;

    @Autowired
    private AmazonS3 amazonS3;

    private void deleteFileFromS3(String fileUrl) {
        try {
            if (fileUrl != null && fileUrl.contains("chat-files/")) {
                // Trích xuất key của file từ URL
                String key = "chat-files/" + fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
                logger.info("Deleting file from S3 with key: {}", key);
                
                // Xóa file từ S3
                amazonS3.deleteObject(new DeleteObjectRequest(bucketName, key));
                logger.info("Successfully deleted file from S3");
            }
        } catch (Exception e) {
            logger.error("Error deleting file from S3: ", e);
            // Không throw exception để không ảnh hưởng đến việc xóa tin nhắn
        }
    }

    @Transactional
    public void deleteAllMessages(String userId, String adminId) {
        try {
            logger.info("=== Deleting all messages between user {} and admin {} ===", userId, adminId);
            
            // Tìm tất cả tin nhắn giữa user và admin
            List<ChatMessageReceiver> receivers = receiverRepository.findByReceiverId(adminId);
            List<ChatMessage> messagesToDelete = receivers.stream()
                .map(ChatMessageReceiver::getMessage)
                .filter(msg -> msg.getSenderId().equals(userId))
                .collect(Collectors.toList());

            // Xóa files từ S3 trước
            for (ChatMessage message : messagesToDelete) {
                if (message.getFileUrl() != null) {
                    deleteFileFromS3(message.getFileUrl());
                }
            }

            // Xóa tất cả receivers
            for (ChatMessage message : messagesToDelete) {
                receiverRepository.deleteByMessage(message);
            }

            // Sau đó xóa các tin nhắn
            chatMessageRepository.deleteAll(messagesToDelete);
            
            logger.info("Successfully deleted {} messages and their associated files", messagesToDelete.size());
        } catch (Exception e) {
            logger.error("Error while deleting messages: ", e);
            throw new RuntimeException("Failed to delete messages", e);
        }
    }

    private String saveFile(MultipartFile file, String fileName) {
        try {
            logger.info("Starting to upload file to S3: {}", fileName);
            
            // Set metadata
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(file.getContentType());
            metadata.setContentLength(file.getSize());

            // Upload to S3
            String s3Key = "chat-files/" + fileName;
            amazonS3.putObject(bucketName, s3Key, file.getInputStream(), metadata);
            logger.info("File uploaded successfully to S3: {}", s3Key);

            // Get the S3 URL
            String fileUrl = amazonS3.getUrl(bucketName, s3Key).toString();
            logger.info("Generated S3 URL: {}", fileUrl);
            
            return fileUrl;
        } catch (IOException e) {
            logger.error("Error uploading file to S3: {}", e.getMessage());
            logger.error("File details - Name: {}, Size: {}, Type: {}", 
                file.getOriginalFilename(), 
                file.getSize(), 
                file.getContentType());
            throw new RuntimeException("Failed to upload file to S3: " + e.getMessage());
        }
    }

    @Transactional
    public ChatMessage sendFileMessage(MultipartFile file, String senderId, String receiverId, String content) {
        try {
            logger.info("=== Processing file message ===");
            logger.info("From: {}", senderId);
            logger.info("To: {}", receiverId);
            logger.info("File details - Name: {}, Size: {}, Type: {}", 
                file.getOriginalFilename(), 
                file.getSize(), 
                file.getContentType());

            // Create new message
            ChatMessage message = new ChatMessage();
            message.setContent(content);
            message.setSenderId(senderId);
            message.setTimestamp(LocalDateTime.now());
            message.setFileType(file.getContentType());
            message.setFileName(file.getOriginalFilename()); // Add this if not already in your entity

            // Generate a unique filename and upload to S3
            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            String fileUrl = saveFile(file, fileName);
            message.setFileUrl(fileUrl);
            logger.info("File URL set in message: {}", fileUrl);

            // Save the message
            ChatMessage savedMessage = chatMessageRepository.save(message);
            logger.info("Saved message with ID: {}", savedMessage.getId());

            // Create message receiver
            ChatMessageReceiver receiver = new ChatMessageReceiver();
            receiver.setMessage(savedMessage);
            receiver.setReceiverId(receiverId);
            receiver.setRead(false);
            receiverRepository.save(receiver);
            logger.info("Created receiver for message");

            return savedMessage;
        } catch (Exception e) {
            logger.error("Error processing file message: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to process file message: " + e.getMessage());
        }
    }
} 