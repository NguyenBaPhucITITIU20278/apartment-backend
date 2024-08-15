package com.controller;

import com.model.Room;
import com.services.RoomService;
import com.model.RoomRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import com.security.jwt.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

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
    public ResponseEntity<Room> addRoom(@RequestHeader("Authorization") String authorizationHeader,
            @RequestHeader("userName") String userName, @RequestBody Room room) {
        logger.info("Adding a new room");
        String accessToken = authorizationHeader.replace("Bearer ", "");
        boolean isValidToken = jwtUtil.validateToken(accessToken, userName);
        if (!isValidToken) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        Room addedRoom = roomService.addRoom(room);
        return new ResponseEntity<>(addedRoom, HttpStatus.CREATED);
    }
}