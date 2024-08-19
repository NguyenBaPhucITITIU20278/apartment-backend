package com.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.model.Room;
import com.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class RoomService {

    @Autowired
    private RoomRepository roomRepository;

    public List<Room> getAllRooms() {
        System.out.println("Getting all rooms");
        return roomRepository.findAll();
    }

    public List<Room> getRoomByAddress(String address) {
        System.out.println("Getting room by address: " + address);
        address = address.trim();
        return roomRepository.findByAddress(address);// Trim leading and trailing spaces
    }

    public Room addRoom(Room room, MultipartFile file) {
        if (file != null && !file.isEmpty()) {
            try {
                // Lưu hình ảnh vào hệ thống file
                String uploadDir = "C:/Users/Admin/Desktop/intern/apartment-control/Apartment-frontend/src/assets";
                File uploadDirFile = new File(uploadDir);
                if (!uploadDirFile.exists()) {
                    uploadDirFile.mkdirs();
                }
                String filePath = uploadDir + file.getOriginalFilename();
                FileOutputStream fos = new FileOutputStream(filePath);
                fos.write(file.getBytes());
                fos.close();

                // Lưu đường dẫn hình ảnh vào đối tượng Room
                room.setImagePath("/images/" + file.getOriginalFilename());
            } catch (IOException e) {
                throw new RuntimeException("Error uploading image", e);
            }
        }
        return roomRepository.save(room);
    }
}