package com.controller;

import com.model.Room;
import com.services.RoomService;

import org.hibernate.query.sqm.tree.domain.SqmListJoin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.List;

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
    
    @CrossOrigin(origins = "http://localhost:3000")
    @GetMapping("/rooms-by-address")
    public Room getRoomByAddress(@RequestBody Room room) {
        System.out.println(room);
        return roomService.getRoomByAddress(room);
    }
}