package com.services;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.model.Room;
import com.repository.RoomRepository;

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

    public Room addRoom(Room room, MultipartFile[] files) {
        if (files != null && files.length > 0) {
            try {
                // Chọn thư mục upload
                String uploadPath = uploadRoomPath + "/" + room.getAddress().replaceAll("\\s+", "_");
    
                // Tạo thư mục nếu chưa tồn tại
                File uploadDirFile = new File(uploadPath);
                if (!uploadDirFile.exists()) {
                    uploadDirFile.mkdirs();
                }
    
                // Lưu từng tệp
                for (MultipartFile file : files) {
                    String fileName = file.getOriginalFilename();
                    String filePath = uploadPath + "/" + fileName;
                    FileOutputStream fos = new FileOutputStream(filePath);
                    fos.write(file.getBytes());
                    fos.close();
                }
    
                // Lưu tên tệp hình ảnh vào đối tượng Room (có thể lưu danh sách tên tệp)
                room.setImagePath("Files uploaded to: " + uploadPath);
            } catch (IOException e) {
                throw new RuntimeException("Error uploading images", e);
            }
        }
        return roomRepository.save(room);
    }

    public Room getRoomById(Long id) {
        Room room = roomRepository.findById(id).orElse(null);
        if (room != null) {
            String uploadPath = uploadRoomPath + "/" + room.getAddress().replaceAll("\\s+", "_");
            File uploadDir = new File(uploadPath);
            if (uploadDir.exists() && uploadDir.isDirectory()) {
                String[] imageFiles = uploadDir.list((dir, name) -> {
                    String lowercaseName = name.toLowerCase();
                    return lowercaseName.endsWith(".jpg") || 
                           lowercaseName.endsWith(".jpeg") || 
                           lowercaseName.endsWith(".png") || 
                           lowercaseName.endsWith(".gif");
                });
                
                if (imageFiles != null) {
                    room.setImagePaths(Arrays.asList(imageFiles));
                }
            }
        }
        return room;
    }

    public List<Room> searchRooms(String query) {
        return roomRepository.findByAddressStartingWith(query);
    }
    public void addRoomWithModel(Room room, MultipartFile[] files, MultipartFile[] model) {
        // Lưu trữ hình ảnh
        String uploadPath = uploadRoomPath + "/" + room.getAddress().replaceAll("\\s+", "_");
    
        // Tạo thư mục nếu chưa tồn tại
        File uploadDirFile = new File(uploadPath);
        if (!uploadDirFile.exists()) {
            uploadDirFile.mkdirs();
        }
    
        // Kiểm tra và lưu trữ các tệp hình ảnh nếu có
        if (files != null && files.length > 0) {
            for (MultipartFile file : files) {
                try {
                    String fileName = file.getOriginalFilename();
                    String filePath = uploadPath + "/" + fileName;
                    FileOutputStream fos = new FileOutputStream(filePath);
                    fos.write(file.getBytes());
                    fos.close();
                } catch (IOException e) {
                    throw new RuntimeException("Error uploading images", e);
                }
            }
        }
    
        // Lưu trữ mô hình 3D
        if (model != null && model.length > 0 && !model[0].isEmpty()) {
            try {
                String modelPath = uploadPath + "/" + model[0].getOriginalFilename();
                File modelFile = new File(modelPath);
                model[0].transferTo(modelFile);
                room.setModelPath(modelPath); // Lưu đường dẫn mô hình vào đối tượng Room
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException("Error saving 3D model: " + e.getMessage());
            }
        }
    
        // Lưu thông tin phòng vào cơ sở dữ liệu
        roomRepository.save(room);
    }
}

