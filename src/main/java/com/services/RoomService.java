package com.services;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.nio.file.Files;
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
        List<Room> rooms = roomRepository.findAll();
        
        // Set image paths for each room
        for (Room room : rooms) {
            setRoomImagePaths(room);
        }
        
        return rooms;
    }

    public List<Room> getRoomByAddress(String address) {
        System.out.println("Getting room by address: " + address);
        if (address == null || address.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        address = address.trim();
        String[] searchTerms = address.split("\\s+");
        List<Room> results = new ArrayList<>();
        
        for (String term : searchTerms) {
            List<Room> partialMatches = roomRepository.findByAddressStartingWith(term);
            for (Room room : partialMatches) {
                if (!results.contains(room)) {
                    setRoomImagePaths(room);  // Set image paths for each room
                    results.add(room);
                }
            }
        }
        
        if (results.isEmpty()) {
            List<Room> fullAddressMatches = roomRepository.findByAddressStartingWith(address);
            for (Room room : fullAddressMatches) {
                setRoomImagePaths(room);  // Set image paths for each room
                results.add(room);
            }
        }
        
        System.out.println("Search query: " + address);
        System.out.println("Number of matches found: " + results.size());
        if (!results.isEmpty()) {
            System.out.println("First match address: " + results.get(0).getAddress());
        }
        
        return results;
    }

    private String formatAddress(String address) {
        if (address == null) return "";
        
        // Normalize the address - keep the original structure but replace invalid characters
        String normalized = address
            .trim()
            .replaceAll("\\s+", "_") // Replace multiple spaces with single underscore
            .replaceAll("[,]", "") // Remove commas
            .replaceAll("[^a-zA-Z0-9_/]", "") // Keep letters, numbers, underscores, and forward slashes
            .replaceAll("_+", "_") // Replace multiple underscores with single underscore
            .replaceAll("^_|_$", ""); // Remove leading and trailing underscores
        
        System.out.println("Original address: " + address);
        System.out.println("Formatted address: " + normalized);
        
        return normalized;
    }

    // Helper method to set image paths for a room
    private void setRoomImagePaths(Room room) {
        if (room != null && room.getAddress() != null) {
            String formattedAddress = formatAddress(room.getAddress());
            // Replace forward slashes with underscores only for directory path
            String directoryAddress = formattedAddress.replaceAll("/", "_");
            String uploadPath = uploadRoomPath + "/" + directoryAddress + "/images";
            System.out.println("Checking directory: " + uploadPath);
            
            // Create directory if it doesn't exist
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) {
                System.out.println("Creating directory: " + uploadPath);
                if (!uploadDir.mkdirs()) {
                    System.err.println("Failed to create directory: " + uploadPath);
                }
            }
            
            if (uploadDir.exists() && uploadDir.isDirectory()) {
                String[] imageFiles = uploadDir.list((dir, name) -> {
                    String lowercaseName = name.toLowerCase();
                    return lowercaseName.endsWith(".jpg") || 
                           lowercaseName.endsWith(".jpeg") || 
                           lowercaseName.endsWith(".png") || 
                           lowercaseName.endsWith(".gif");
                });
                
                if (imageFiles != null && imageFiles.length > 0) {
                    System.out.println("Found images for room " + room.getId() + ": " + Arrays.toString(imageFiles));
                    room.setImagePaths(Arrays.asList(imageFiles));
                } else {
                    System.out.println("No images found for room " + room.getId() + " in directory: " + uploadPath);
                    room.setImagePaths(new ArrayList<>());
                }
            } else {
                System.out.println("Directory does not exist or is not a directory: " + uploadPath);
                room.setImagePaths(new ArrayList<>());
            }

            // Set video path
            String uploadVideoPath = uploadRoomPath + "/" + directoryAddress + "/video";
            File videoDir = new File(uploadVideoPath);
            if (videoDir.exists() && videoDir.isDirectory()) {
                String[] videoFiles = videoDir.list((dir, name) -> {
                    String lowercaseName = name.toLowerCase();
                    return lowercaseName.endsWith(".mp4") || 
                           lowercaseName.endsWith(".avi") || 
                           lowercaseName.endsWith(".mov") || 
                           lowercaseName.endsWith(".wmv");
                });
                
                if (videoFiles != null && videoFiles.length > 0) {
                    room.setVideoPath(videoFiles[0]); // Set the first video found
                }
            }
        }
    }

    public Room addRoom(Room room, MultipartFile[] files, MultipartFile video) {
        // Handle image files first
        if (files != null && files.length > 0) {
            try {
                String formattedAddress = formatAddress(room.getAddress());
                String uploadPath = uploadRoomPath + "/" + formattedAddress + "/images";
                System.out.println("Creating directory for address '" + room.getAddress() + "': " + uploadPath);
                
                File uploadDirFile = new File(uploadPath);
                if (!uploadDirFile.exists()) {
                    if (!uploadDirFile.mkdirs()) {
                        throw new RuntimeException("Failed to create directory: " + uploadPath);
                    }
                }

                List<String> imagePaths = new ArrayList<>();
                for (MultipartFile file : files) {
                    if (file != null && !file.isEmpty()) {
                        String fileName = file.getOriginalFilename();
                        if (fileName != null && !fileName.trim().isEmpty()) {
                            String filePath = uploadPath + "/" + fileName;
                            System.out.println("Saving file: " + filePath);
                            
                            File destFile = new File(filePath);
                            file.transferTo(destFile);
                            imagePaths.add(fileName);
                        }
                    }
                }

                room.setImagePaths(imagePaths);
            } catch (IOException e) {
                System.err.println("Error uploading images: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("Error uploading images: " + e.getMessage(), e);
            }
        }

        // Handle video file if present
        if (video != null && !video.isEmpty()) {
            try {
                String formattedAddress = formatAddress(room.getAddress());
                String uploadPath = uploadRoomPath + "/" + formattedAddress + "/video";
                
                File uploadDirFile = new File(uploadPath);
                if (!uploadDirFile.exists()) {
                    if (!uploadDirFile.mkdirs()) {
                        throw new RuntimeException("Failed to create video directory: " + uploadPath);
                    }
                }

                String fileName = video.getOriginalFilename();
                if (fileName != null && !fileName.trim().isEmpty()) {
                    String filePath = uploadPath + "/" + fileName;
                    File destFile = new File(filePath);
                    video.transferTo(destFile);
                    room.setVideoPath(fileName);
                }
            } catch (IOException e) {
                throw new RuntimeException("Error uploading video: " + e.getMessage(), e);
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
    public void addRoomWithModel(Room room, MultipartFile[] files, MultipartFile[] model, MultipartFile[] web360, MultipartFile video) {
        String formattedAddress = formatAddress(room.getAddress());
        String directoryAddress = formattedAddress.replaceAll("/", "_");
        String baseUploadPath = uploadRoomPath + "/" + directoryAddress;
        System.out.println("Base upload path: " + baseUploadPath);

        // Create directories for images, models, web360, and video
        File imageDir = new File(baseUploadPath + "/images");
        File modelDir = new File(baseUploadPath + "/models");
        File web360Dir = new File(baseUploadPath + "/web360");
        File videoDir = new File(baseUploadPath + "/video");

        if (!imageDir.exists()) imageDir.mkdirs();
        if (!modelDir.exists()) modelDir.mkdirs();
        if (!web360Dir.exists()) web360Dir.mkdirs();
        if (!videoDir.exists()) videoDir.mkdirs();

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
                    imagePaths.add(fileName); // Store only the filename
                } catch (IOException e) {
                    throw new RuntimeException("Error uploading images", e);
                }
            }
        }

        room.setImagePaths(imagePaths);

        // Save 3D model
        if (model != null && model.length > 0 && !model[0].isEmpty()) {
            try {
                String fileName = model[0].getOriginalFilename();
                String filePath = modelDir.getPath() + "/" + fileName;
                File modelFile = new File(filePath);
                model[0].transferTo(modelFile);
                room.setModelPath(fileName); // Store only the filename
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
                    String filePath = web360Dir.getPath() + "/" + fileName;
                    File web360File = new File(filePath);
                    file.transferTo(web360File);
                    web360Paths.add(fileName); // Store only the filename
                } catch (IOException e) {
                    throw new RuntimeException("Error uploading web360 files", e);
                }
            }
        }

        room.setWeb360Paths(web360Paths);

        // Handle video file
        if (video != null && !video.isEmpty()) {
            try {
                String fileName = video.getOriginalFilename();
                String filePath = videoDir.getPath() + "/" + fileName;
                File videoFile = new File(filePath);
                video.transferTo(videoFile);
                room.setVideoPath(fileName);
            } catch (IOException e) {
                throw new RuntimeException("Error uploading video: " + e.getMessage(), e);
            }
        }

        // Save room information to the database
        roomRepository.save(room);
    }

    public Room updateRoom(Long id, RoomRequest roomRequest) {
        Room room = roomRepository.findById(id).orElseThrow(() -> new RuntimeException("Room not found"));
        
        String oldAddress = formatAddress(room.getAddress());
        String newAddress = formatAddress(roomRequest.getAddress());
        
        // Replace forward slashes with underscores for directory paths
        String oldDirectoryAddress = oldAddress.replaceAll("/", "_");
        String newDirectoryAddress = newAddress.replaceAll("/", "_");

        System.out.println("Old directory address: " + oldDirectoryAddress);
        System.out.println("New directory address: " + newDirectoryAddress);

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
        if (!oldDirectoryAddress.equals(newDirectoryAddress)) {
            try {
                String oldBasePath = uploadRoomPath + "/" + oldDirectoryAddress;
                String newBasePath = uploadRoomPath + "/" + newDirectoryAddress;
                
                System.out.println("Moving files from: " + oldBasePath);
                System.out.println("Moving files to: " + newBasePath);

                // Create the new base directory
                File newBaseDir = new File(newBasePath);
                if (!newBaseDir.exists() && !newBaseDir.mkdirs()) {
                    throw new RuntimeException("Failed to create new directory: " + newBasePath);
                }

                // Move each subdirectory (images, models, web360, video)
                moveDirectoryContents(oldBasePath + "/images", newBasePath + "/images");
                moveDirectoryContents(oldBasePath + "/models", newBasePath + "/models");
                moveDirectoryContents(oldBasePath + "/web360", newBasePath + "/web360");
                moveDirectoryContents(oldBasePath + "/video", newBasePath + "/video");

                // Delete the old directory structure after successful move
                File oldBaseDir = new File(oldBasePath);
                if (oldBaseDir.exists()) {
                    System.out.println("Deleting old directory: " + oldBasePath);
                    deleteDirectory(oldBaseDir);
                    if (oldBaseDir.exists()) {
                        System.err.println("Warning: Failed to delete old directory: " + oldBasePath);
                    } else {
                        System.out.println("Successfully deleted old directory: " + oldBasePath);
                    }
                }
                
                System.out.println("Successfully moved all files to new location");

                // Update paths in room object (only store filenames, not full paths)
                if (room.getImagePaths() != null) {
                    // Image paths are already just filenames, no need to update
                    System.out.println("Current image paths: " + room.getImagePaths());
                }

                if (room.getModelPath() != null) {
                    // Model path is already just filename, no need to update
                    System.out.println("Current model path: " + room.getModelPath());
                }

                if (room.getWeb360Paths() != null) {
                    // Web360 paths are already just filenames, no need to update
                    System.out.println("Current web360 paths: " + room.getWeb360Paths());
                }
            } catch (Exception e) {
                System.err.println("Error during file moving: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("Error moving files: " + e.getMessage());
            }
        }

        return roomRepository.save(room);
    }

    // Helper method to move directory contents
    private void moveDirectoryContents(String sourcePath, String destPath) {
        File sourceDir = new File(sourcePath);
        File destDir = new File(destPath);

        System.out.println("Moving directory contents from: " + sourcePath);
        System.out.println("Moving directory contents to: " + destPath);

        if (sourceDir.exists() && sourceDir.isDirectory()) {
            // Create destination directory if it doesn't exist
            if (!destDir.exists() && !destDir.mkdirs()) {
                throw new RuntimeException("Failed to create destination directory: " + destPath);
            }

            File[] files = sourceDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    File destFile = new File(destDir, file.getName());
                    System.out.println("Moving file: " + file.getName());
                    
                    if (!file.renameTo(destFile)) {
                        // If rename fails, try copy and delete
                        try {
                            Files.copy(file.toPath(), destFile.toPath());
                            if (!file.delete()) {
                                System.err.println("Warning: Could not delete original file: " + file.getAbsolutePath());
                            }
                        } catch (IOException e) {
                            throw new RuntimeException("Failed to move file: " + file.getName(), e);
                        }
                    }
                }
            }
        }
    }

    // Improved deleteDirectory method with better logging
    private void deleteDirectory(File directory) {
        System.out.println("Attempting to delete directory: " + directory.getAbsolutePath());
        
        if (!directory.exists()) {
            System.out.println("Directory does not exist: " + directory.getAbsolutePath());
            return;
        }

        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    System.out.println("Deleting file: " + file.getAbsolutePath());
                    if (!file.delete()) {
                        System.err.println("Failed to delete file: " + file.getAbsolutePath());
                    }
                }
            }
        }

        System.out.println("Deleting directory itself: " + directory.getAbsolutePath());
        if (!directory.delete()) {
            System.err.println("Failed to delete directory: " + directory.getAbsolutePath());
        }
    }

    public Room updateRoomImages(Long id, MultipartFile[] images) {
        Room room = roomRepository.findById(id).orElseThrow(() -> new RuntimeException("Room not found"));

        if (images != null && images.length > 0) {
            try {
                String formattedAddress = formatAddress(room.getAddress());
                String uploadPath = uploadRoomPath + "/" + formattedAddress + "/images";
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
                String formattedAddress = formatAddress(room.getAddress());
                String modelPath = uploadRoomPath + "/" + formattedAddress + "/models";
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
                String formattedAddress = formatAddress(room.getAddress());
                String web360Path = uploadRoomPath + "/" + formattedAddress + "/web360";
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

    public Room updateRoomVideo(Long id, MultipartFile video) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found with id: " + id));

        try {
            String formattedAddress = formatAddress(room.getAddress());
            String directoryAddress = formattedAddress.replaceAll("/", "_");
            String uploadPath = uploadRoomPath + "/" + directoryAddress + "/video";
            
            // Create video directory if it doesn't exist
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) {
                if (!uploadDir.mkdirs()) {
                    throw new RuntimeException("Failed to create video directory: " + uploadPath);
                }
            }

            // Delete existing video if present
            if (room.getVideoPath() != null) {
                File existingVideo = new File(uploadPath + "/" + room.getVideoPath());
                if (existingVideo.exists()) {
                    existingVideo.delete();
                }
            }

            // Save new video
            String fileName = video.getOriginalFilename();
            if (fileName != null && !fileName.trim().isEmpty()) {
                String filePath = uploadPath + "/" + fileName;
                File destFile = new File(filePath);
                video.transferTo(destFile);
                room.setVideoPath(fileName);
            }

            return roomRepository.save(room);
        } catch (IOException e) {
            throw new RuntimeException("Error updating room video: " + e.getMessage(), e);
        }
    }

    public Room deleteRoomImage(Long id, String imageName) {
        Room room = roomRepository.findById(id).orElseThrow(() -> new RuntimeException("Room not found"));
        
        String formattedAddress = formatAddress(room.getAddress());
        String imagePath = uploadRoomPath + "/" + formattedAddress + "/images/" + imageName;
        File imageFile = new File(imagePath);
        
        System.out.println("Attempting to delete image at path: " + imagePath);
        
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
            System.err.println("Image file not found at path: " + imagePath);
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
        
        String formattedAddress = formatAddress(room.getAddress());
        String web360Path = uploadRoomPath + "/" + formattedAddress + "/web360/" + web360Name;
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
        String formattedAddress = formatAddress(room.getAddress());
        String roomPath = uploadRoomPath + "/" + formattedAddress;
        File roomDir = new File(roomPath);

        // Xóa tất cả files và thư mục
        if (roomDir.exists()) {
            deleteDirectory(roomDir);
        }

        // Xóa room từ database
        roomRepository.delete(room);
    }

    public List<Room> getRoomsByUser(String username) {
        return roomRepository.findByUsername(username);
    }

    public Room deleteRoomVideo(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found with id: " + id));

        if (room.getVideoPath() != null) {
            String formattedAddress = formatAddress(room.getAddress());
            String directoryAddress = formattedAddress.replaceAll("/", "_");
            String videoPath = uploadRoomPath + "/" + directoryAddress + "/video/" + room.getVideoPath();
            File videoFile = new File(videoPath);
            
            if (videoFile.exists()) {
                if (!videoFile.delete()) {
                    throw new RuntimeException("Failed to delete video file: " + videoPath);
                }
            }

            room.setVideoPath(null);
            return roomRepository.save(room);
        }
        return room;
    }

}

