package com.services;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
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
            String uploadPath = uploadRoomPath + "/images/" + room.getAddress().replaceAll("\\s+", "_");
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
    public void addRoomWithModel(Room room, MultipartFile[] files, MultipartFile[] model, MultipartFile[] web360) {
        // Base upload path
        String baseUploadPath = uploadRoomPath + "/" + room.getAddress().replaceAll("\\s+", "_");

        // Create directories for images, models, and web360
        File imageDir = new File(baseUploadPath + "/images");
        File modelDir = new File(baseUploadPath + "/models");
        File web360Dir = new File(baseUploadPath + "/web360");

        if (!imageDir.exists()) {
            imageDir.mkdirs();
        }
        if (!modelDir.exists()) {
            modelDir.mkdirs();
        }
        if (!web360Dir.exists()) {
            web360Dir.mkdirs();
        }

        List<String> imagePaths = new ArrayList<>();
        List<String> web360Paths = new ArrayList<>();

        // Save image files
        if (files != null && files.length > 0) {
            for (MultipartFile file : files) {
                try {
                    String fileName = file.getOriginalFilename();
                    String filePath = imageDir.getPath() + "/" + fileName;
                    FileOutputStream fos = new FileOutputStream(filePath);
                    fos.write(file.getBytes());
                    fos.close();
                    imagePaths.add(filePath); // Add file path to list
                } catch (IOException e) {
                    throw new RuntimeException("Error uploading images", e);
                }
            }
        }

        room.setImagePaths(imagePaths); // Set all image paths at once

        // Save 3D model
        if (model != null && model.length > 0 && !model[0].isEmpty()) {
            try {
                String modelPath = modelDir.getPath() + "/" + model[0].getOriginalFilename();
                File modelFile = new File(modelPath);
                model[0].transferTo(modelFile);
                room.setModelPath(modelPath); // Save model path to Room object
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException("Error saving 3D model: " + e.getMessage());
            }
        }

        // Save web360 files
        if (web360 != null && web360.length > 0) {
            for (MultipartFile file : web360) {
                try {
                    String fileName = file.getOriginalFilename();
                    String web360Path = web360Dir.getPath() + "/" + fileName;
                    File web360File = new File(web360Path);
                    file.transferTo(web360File);
                    web360Paths.add(web360Path); // Add each path to the list
                } catch (IOException e) {
                    throw new RuntimeException("Error uploading web360 files", e);
                }
            }
        }

        room.setWeb360Paths(web360Paths); // Set all web360 paths at once

        // Save room information to the database
        roomRepository.save(room);
    }
}

