package com.controller;

import com.model.Room;
import com.services.RoomService;
import com.model.RoomRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import com.security.jwt.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import java.time.LocalDateTime;
@EnableAutoConfiguration
@Configuration
@ComponentScan
@RestController
@RequestMapping("/api/rooms")
@CrossOrigin(origins = "http://localhost:3000")
public class RoomController {

    private static final Logger logger = LoggerFactory.getLogger(RoomController.class);

    @Autowired
    private RoomService roomService;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping("/all-rooms")
    public ResponseEntity<List<Room>> getAllRooms() {
        logger.info("Getting all rooms");
        // String accessToken = authorizationHeader.replace("Bearer ", "");
        // boolean isValidToken = jwtUtil.validateToken(accessToken, userName);
        // if (!isValidToken) {
        // return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        // }

        List<Room> rooms = roomService.getAllRooms();
        return new ResponseEntity<>(rooms, HttpStatus.OK);
    }

    @PostMapping("/rooms-by-address")
    public ResponseEntity<List<Room>> getRoomByAddress(@RequestBody RoomRequest roomRequest) {
        System.out.println("Starting getRoomByAddress");
        logger.info("Getting rooms by address");
        String address = roomRequest.getAddress();
        List<Room> rooms = roomService.getRoomByAddress(address);
        return new ResponseEntity<>(rooms, HttpStatus.OK);
    }

    @PostMapping("/add-room")
    public ResponseEntity<?> addRoom(@RequestParam("file") MultipartFile file, @RequestParam("data") String data) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Room room = objectMapper.readValue(data, Room.class);
            room.setPostedTime(LocalDateTime.now());
            roomService.addRoom(room, file);
            return ResponseEntity.ok("Room added successfully");
        } catch (Exception e) {
            e.printStackTrace(); // Log the stack trace for debugging
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error adding room: " + e.getMessage());
        }
    }

    @GetMapping("/room-by-id/{id}")
    public ResponseEntity<?> getRoomById(@PathVariable Long id) {
        try {   
            Room room = roomService.getRoomById(id);
            return new ResponseEntity<>(room, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace(); // Log the stack trace for debugging
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error getting room by id: " + e.getMessage());
        }
    }
}