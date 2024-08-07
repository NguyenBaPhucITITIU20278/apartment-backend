package com.controller;

import com.model.Room;
import com.services.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    @Autowired
    private RoomService roomService;

    @GetMapping("/all-rooms")
    public List<Room> getAllRooms() {
        return roomService.getAllRooms();
    }

    @PostMapping("/rooms-by-address")
    public Room getRoomByAddress(@RequestBody Map<String, String> payload) {
        String address = payload.get("address");
        return roomService.getRoomByAddress(address);
    }
}