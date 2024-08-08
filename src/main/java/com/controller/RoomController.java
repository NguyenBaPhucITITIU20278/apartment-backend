package com.controller;

import com.model.Room;
import com.services.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

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

    @GetMapping("/all-rooms")
    public List<Room> getAllRooms() {
        logger.info("Getting all rooms");
        return roomService.getAllRooms();
    }

    @PostMapping("/rooms-by-address")
    public Room getRoomByAddressAndBedroom(@RequestBody Map<String, String> payload) {
        String address = payload.get("address");
        String numberOfBedroomsStr = payload.get("number_of_bedroom");

        if (address == null || numberOfBedroomsStr == null) {
            throw new IllegalArgumentException("Address and number_of_bedroom must not be null");
        }

        int numberOfBedrooms;
        try {
            numberOfBedrooms = Integer.parseInt(numberOfBedroomsStr);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("number_of_bedroom must be a valid integer", e);
        }

        logger.info("Fetching room by address: {}", address);
        return roomService.getRoomByAddressAndBedroom(address, numberOfBedrooms);
    }
}