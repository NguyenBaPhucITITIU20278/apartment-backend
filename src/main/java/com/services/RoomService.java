package com.services;

import com.model.Room;
import com.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${upload.room.path}")
    private String uploadRoomPath;

    public List<Room> getAllRooms() {
        System.out.println("Getting all rooms");
        return roomRepository.findAll();
    }

    public List<Room> getRoomByAddress(String address) {
        System.out.println("Getting room by address: " + address);
        address = address.trim();
        return roomRepository.findByAddress(address); // Trim leading and trailing spaces
    }

    public Room addRoom(Room room, MultipartFile file) {
        if (file != null && !file.isEmpty()) {
            try {
                // Chọn thư mục upload
                String uploadPath = uploadRoomPath;

                // Lưu hình ảnh vào hệ thống file
                File uploadDirFile = new File(uploadPath);
                if (!uploadDirFile.exists()) {
                    uploadDirFile.mkdirs();
                }
                String fileName = file.getOriginalFilename();
                String filePath = uploadPath + "/room/" + fileName;
                FileOutputStream fos = new FileOutputStream(filePath);
                fos.write(file.getBytes());
                fos.close();

                // Lưu tên tệp hình ảnh vào đối tượng Room
                room.setImagePath(fileName);
            } catch (IOException e) {
                throw new RuntimeException("Error uploading image", e);
            }
        }
        return roomRepository.save(room);
    }
}