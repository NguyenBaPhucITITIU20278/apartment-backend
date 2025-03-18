package com.services;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.model.Room;
import com.repository.RoomRepository;
import com.model.RoomRequest;

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

    public Room updateRoom(Long id, RoomRequest roomRequest) {
        Room room = roomRepository.findById(id).orElseThrow(() -> new RuntimeException("Room not found"));
        
        String oldAddress = room.getAddress().replaceAll("\\s+", "_");
        String newAddress = roomRequest.getAddress().replaceAll("\\s+", "_");

        // Update room details from roomRequest
        room.setName(roomRequest.getName());
        room.setPrice(roomRequest.getPrice());
        room.setStatus(roomRequest.getStatus());
        room.setCustomerId(roomRequest.getCustomerId());
        room.setNumberOfBedrooms(roomRequest.getNumberOfBedrooms());
        room.setDescription(roomRequest.getDescription());
        room.setPhoneNumber(roomRequest.getPhoneNumber());
        room.setAddress(roomRequest.getAddress());
        room.setArea(roomRequest.getArea());

        // Di chuyển tệp nếu địa chỉ thay đổi
        if (!oldAddress.equals(newAddress)) {
            try {
                // Di chuyển thư mục images
                String oldImagesPath = uploadRoomPath + "/" + oldAddress + "/images";
                String newImagesPath = uploadRoomPath + "/" + newAddress + "/images";
                moveDirectory(oldImagesPath, newImagesPath);

                // Di chuyển thư mục models
                String oldModelPath = uploadRoomPath + "/" + oldAddress + "/models";
                String newModelPath = uploadRoomPath + "/" + newAddress + "/models";
                moveDirectory(oldModelPath, newModelPath);

                // Di chuyển thư mục web360
                String oldWeb360Path = uploadRoomPath + "/" + oldAddress + "/web360";
                String newWeb360Path = uploadRoomPath + "/" + newAddress + "/web360";
                moveDirectory(oldWeb360Path, newWeb360Path);

                // Cập nhật đường dẫn trong imagePaths
                if (room.getImagePaths() != null) {
                    List<String> updatedImagePaths = new ArrayList<>();
                    for (String path : room.getImagePaths()) {
                        updatedImagePaths.add(path.replace(oldAddress, newAddress));
                    }
                    room.setImagePaths(updatedImagePaths);
                }

                // Cập nhật đường dẫn model
                if (room.getModelPath() != null) {
                    String updatedModelPath = room.getModelPath().replace(oldAddress, newAddress);
                    room.setModelPath(updatedModelPath);
                }

                // Cập nhật đường dẫn web360
                if (room.getWeb360Paths() != null) {
                    List<String> updatedWeb360Paths = new ArrayList<>();
                    for (String path : room.getWeb360Paths()) {
                        updatedWeb360Paths.add(path.replace(oldAddress, newAddress));
                    }
                    room.setWeb360Paths(updatedWeb360Paths);
                }
            } catch (Exception e) {
                throw new RuntimeException("Error moving files: " + e.getMessage());
            }
        }

        return roomRepository.save(room);
    }

    public Room updateRoomImages(Long id, MultipartFile[] images) {
        Room room = roomRepository.findById(id).orElseThrow(() -> new RuntimeException("Room not found"));

        if (images != null && images.length > 0) {
            try {
                String uploadPath = uploadRoomPath + "/" + room.getAddress().replaceAll("\\s+", "_") + "/images";
                File uploadDir = new File(uploadPath);
                if (!uploadDir.exists()) {
                    uploadDir.mkdirs();
                }

                // Lấy danh sách đường dẫn hiện có hoặc tạo mới nếu chưa có
                List<String> existingPaths = room.getImagePaths();
                if (existingPaths == null) {
                    existingPaths = new ArrayList<>();
                }

                // Thêm các đường dẫn mới vào danh sách hiện có
                for (MultipartFile image : images) {
                    if (!image.isEmpty()) {
                        String filePath = uploadPath + "/" + image.getOriginalFilename();
                        File destFile = new File(filePath);
                        image.transferTo(destFile);
                        existingPaths.add(filePath);
                    }
                }

                // Cập nhật danh sách đường dẫn hình ảnh với cả đường dẫn cũ và mới
                room.setImagePaths(existingPaths);
            } catch (IOException e) {
                throw new RuntimeException("Error uploading images", e);
            }
        }

        return roomRepository.save(room);
    }

    public Room updateRoomModel(Long id, MultipartFile model) {
        Room room = roomRepository.findById(id).orElseThrow(() -> new RuntimeException("Room not found"));

        if (model != null && !model.isEmpty()) {
            try {
                String modelPath = uploadRoomPath + "/" + room.getAddress().replaceAll("\\s+", "_") + "/models";
                File modelDir = new File(modelPath);
                if (!modelDir.exists()) {
                    modelDir.mkdirs();
                }

                // Xóa model cũ nếu tồn tại
                if (room.getModelPath() != null) {
                    File oldModel = new File(room.getModelPath());
                    if (oldModel.exists()) {
                        oldModel.delete();
                    }
                }

                // Lưu model mới
                String filePath = modelPath + "/" + model.getOriginalFilename();
                File destFile = new File(filePath);
                model.transferTo(destFile);

                // Cập nhật đường dẫn model mới
                room.setModelPath(filePath);
            } catch (IOException e) {
                throw new RuntimeException("Error uploading model", e);
            }
        }

        return roomRepository.save(room);
    }

    public Room updateRoomWeb360(Long id, MultipartFile[] web360Files) {
        Room room = roomRepository.findById(id).orElseThrow(() -> new RuntimeException("Room not found"));

        if (web360Files != null && web360Files.length > 0) {
            try {
                String web360Path = uploadRoomPath + "/" + room.getAddress().replaceAll("\\s+", "_") + "/web360";
                File web360Dir = new File(web360Path);
                if (!web360Dir.exists()) {
                    web360Dir.mkdirs();
                }

                // Lấy danh sách đường dẫn web360 hiện có
                List<String> existingWeb360Paths = room.getWeb360Paths();
                if (existingWeb360Paths == null) {
                    existingWeb360Paths = new ArrayList<>();
                } else {
                    // Tạo một bản sao của danh sách hiện có để tránh reference issues
                    existingWeb360Paths = new ArrayList<>(existingWeb360Paths);
                }

                // Thêm các file web360 mới vào danh sách hiện có
                for (MultipartFile web360File : web360Files) {
                    if (!web360File.isEmpty()) {
                        String filePath = web360Path + "/" + web360File.getOriginalFilename();
                        File destFile = new File(filePath);
                        web360File.transferTo(destFile);
                        
                        // Kiểm tra xem đường dẫn đã tồn tại chưa
                        if (!existingWeb360Paths.contains(filePath)) {
                            existingWeb360Paths.add(filePath);
                        }
                    }
                }

                // Cập nhật danh sách đường dẫn web360
                room.setWeb360Paths(existingWeb360Paths);
                
                // Log để kiểm tra
                System.out.println("Updated web360 paths: " + existingWeb360Paths);
            } catch (IOException e) {
                throw new RuntimeException("Error uploading web360 files", e);
            }
        }

        return roomRepository.save(room);
    }

    // Phương thức hỗ trợ di chuyển thư mục
    private void moveDirectory(String sourcePath, String destPath) {
        File sourceDir = new File(sourcePath);
        File destDir = new File(destPath);

        if (sourceDir.exists()) {
            if (!destDir.exists()) {
                destDir.mkdirs();
            }
            
            File[] files = sourceDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    File destFile = new File(destPath + "/" + file.getName());
                    file.renameTo(destFile);
                }
            }
            // Xóa thư mục nguồn sau khi di chuyển
            sourceDir.delete();
        }
    }

    public Room deleteRoomImage(Long id, String imageName) {
        Room room = roomRepository.findById(id).orElseThrow(() -> new RuntimeException("Room not found"));
        
        String imagePath = uploadRoomPath + "/" + room.getAddress().replaceAll("\\s+", "_") + "/images/" + imageName;
        File imageFile = new File(imagePath);
        
        if (imageFile.exists()) {
            if (imageFile.delete()) {
                // Xóa đường dẫn khỏi danh sách imagePaths
                List<String> updatedPaths = room.getImagePaths();
                updatedPaths.removeIf(path -> path.endsWith(imageName));
                room.setImagePaths(updatedPaths);
                return roomRepository.save(room);
            } else {
                throw new RuntimeException("Could not delete image file");
            }
        } else {
            throw new RuntimeException("Image file not found");
        }
    }

    public Room deleteRoomModel(Long id) {
        Room room = roomRepository.findById(id).orElseThrow(() -> new RuntimeException("Room not found"));
        
        if (room.getModelPath() != null) {
            File modelFile = new File(room.getModelPath());
            if (modelFile.exists()) {
                if (modelFile.delete()) {
                    room.setModelPath(null);
                    return roomRepository.save(room);
                } else {
                    throw new RuntimeException("Could not delete model file");
                }
            }
        }
        throw new RuntimeException("Model file not found");
    }

    public Room deleteRoomWeb360(Long id, String web360Name) {
        Room room = roomRepository.findById(id).orElseThrow(() -> new RuntimeException("Room not found"));
        
        String web360Path = uploadRoomPath + "/" + room.getAddress().replaceAll("\\s+", "_") + "/web360/" + web360Name;
        File web360File = new File(web360Path);
        
        if (web360File.exists()) {
            if (web360File.delete()) {
                // Xóa đường dẫn khỏi danh sách web360Paths
                List<String> updatedPaths = room.getWeb360Paths();
                updatedPaths.removeIf(path -> path.endsWith(web360Name));
                room.setWeb360Paths(updatedPaths);
                return roomRepository.save(room);
            } else {
                throw new RuntimeException("Could not delete web360 file");
            }
        } else {
            throw new RuntimeException("Web360 file not found");
        }
    }

    public void deleteEntireRoom(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        // Xây dựng đường dẫn đến thư mục chứa tất cả files của phòng
        String roomPath = uploadRoomPath + "/" + room.getAddress().replaceAll("\\s+", "_");
        File roomDir = new File(roomPath);

        // Xóa tất cả files và thư mục
        if (roomDir.exists()) {
            deleteDirectory(roomDir);
        }

        // Xóa room từ database
        roomRepository.delete(room);
    }

    // Phương thức đệ quy để xóa thư mục và tất cả nội dung bên trong
    private void deleteDirectory(File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    if (!file.delete()) {
                        System.out.println("Failed to delete file: " + file.getAbsolutePath());
                    }
                }
            }
        }
        if (!directory.delete()) {
            System.out.println("Failed to delete directory: " + directory.getAbsolutePath());
        }
    }

}

