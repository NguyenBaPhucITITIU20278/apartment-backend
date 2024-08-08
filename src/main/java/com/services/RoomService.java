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

    public Room getRoomByAddressAndBedroom(String address, int numberOfBedrooms) {
        System.out.println("Getting room by address: " + address);
        return roomRepository.findByAddressAndNumberOfBedrooms(address.trim(), numberOfBedrooms);// Trim leading and trailing spaces
    }
}