package com.services;

import com.model.Room;
import com.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoomService {

    @Autowired
    private RoomRepository roomRepository;

    public List<Room> getAllRooms() {
        System.out.println("Getting all rooms");
        return roomRepository.findAll();
    }

    public List<Room> getRoomByAddressAndNumberOfBedrooms(String address, Integer numberOfBedrooms) {
        System.out.println("Getting room by address: " + address);
        System.out.println("Getting room by number of bedrooms: " + numberOfBedrooms);
        address = address.trim();
        return roomRepository.findByAddressAndNumberOfBedrooms(address, numberOfBedrooms);// Trim leading and trailing spaces
    }

    public Room addRoom(Room room) {
        room.setNumberOfBedrooms(room.getNumberOfBedrooms());
        room.setAddress(room.getAddress());
        return roomRepository.save(room);
    }
}