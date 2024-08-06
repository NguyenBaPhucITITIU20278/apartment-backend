package com.services;

import com.model.Room;
import com.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class RoomService {

    private static final Logger logger = LoggerFactory.getLogger(RoomService.class);

    @Autowired
    private RoomRepository roomRepository;

    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    public Optional<Room> getRoomByAddress(String address) {
        logger.info("Searching for room with address: {}", address);
        Optional<Room> room = roomRepository.findByAddress(address);
        if (room.isPresent()) {
            logger.info("Room found: {}", room.get());
        } else {
            logger.warn("No room found with address: {}", address);
        }
        return room;
    }
}