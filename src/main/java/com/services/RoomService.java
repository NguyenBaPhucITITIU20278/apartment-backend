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
import com.services.S3Service;

@Service
public class RoomService {

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private S3Service s3Service;

    @Value("${upload.room.path}")
    private String uploadRoomPath;

    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    public List<Room> getRoomByAddress(String address) {
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
                    results.add(room);
                }
            }
        }
        if (results.isEmpty()) {
            List<Room> fullAddressMatches = roomRepository.findByAddressStartingWith(address);
            for (Room room : fullAddressMatches) {
                results.add(room);
            }
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
                    List<String> videoPaths = new ArrayList<>();
                    videoPaths.add(fileName);
                    room.setVideoPaths(videoPaths);
                }
            } catch (IOException e) {
                throw new RuntimeException("Error uploading video: " + e.getMessage(), e);
            }
        }

        return roomRepository.save(room);
    }

    public Room getRoomById(Long id) {
        return roomRepository.findById(id).orElse(null);
    }

    public List<Room> searchRooms(String query) {
        return roomRepository.findByAddressStartingWith(query);
    }
    public void addRoomWithModel(Room room, MultipartFile[] files, MultipartFile[] model, MultipartFile[] web360, MultipartFile video) {
        List<String> imagePaths = new ArrayList<>();
        List<String> web360Paths = new ArrayList<>();
        List<String> videoPaths = new ArrayList<>();

        String addressFolder = formatAddress(room.getAddress());

        // Upload images
        if (files != null && files.length > 0) {
            for (MultipartFile file : files) {
                String imageUrl = s3Service.uploadFile(file, addressFolder, "images");
                imagePaths.add(imageUrl);
            }
        }
        room.setImagePaths(imagePaths);

        // Upload 3D model
        if (model != null && model.length > 0 && !model[0].isEmpty()) {
            String modelUrl = s3Service.uploadFile(model[0], addressFolder, "models");
            room.setModelPath(modelUrl);
        }

        // Upload web360 files
        if (web360 != null && web360.length > 0) {
            for (MultipartFile file : web360) {
                String web360Url = s3Service.uploadFile(file, addressFolder, "web360");
                web360Paths.add(web360Url);
            }
        }
        room.setWeb360Paths(web360Paths);

        // Upload video
        if (video != null && !video.isEmpty()) {
            String videoUrl = s3Service.uploadFile(video, addressFolder, "video");
            videoPaths.add(videoUrl);
            room.setVideoPaths(videoPaths);
        }

        // Save room information to database
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

        // Di chuyển file trên S3 nếu địa chỉ thay đổi
        if (!oldDirectoryAddress.equals(newDirectoryAddress)) {
            // Di chuyển file trên S3 và cập nhật lại link trong room
            s3Service.moveRoomFilesToNewAddress(oldDirectoryAddress, newDirectoryAddress, room);
            // Xóa folder cũ trên S3 (nếu cần, có thể dùng AWS SDK để xóa folder)
            s3Service.deleteS3Folder("images/" + oldDirectoryAddress);
        }

        return roomRepository.save(room);
    }

    public Room updateRoomImages(Long id, MultipartFile[] images) {
        Room room = roomRepository.findById(id).orElseThrow(() -> new RuntimeException("Room not found"));
        if (images != null && images.length > 0) {
            List<String> existingPaths = room.getImagePaths();
            if (existingPaths == null) existingPaths = new ArrayList<>();
            String addressFolder = formatAddress(room.getAddress());
            for (MultipartFile image : images) {
                if (!image.isEmpty()) {
                    String imageUrl = s3Service.uploadFile(image, addressFolder, "images");
                    existingPaths.add(imageUrl);
                }
            }
            room.setImagePaths(existingPaths);
        }
        return roomRepository.save(room);
    }

    public Room updateRoomModel(Long id, MultipartFile model) {
        Room room = roomRepository.findById(id).orElseThrow(() -> new RuntimeException("Room not found"));
        if (model != null && !model.isEmpty()) {
            String addressFolder = formatAddress(room.getAddress());
            // Xóa model cũ trên S3 nếu có
            if (room.getModelPath() != null) {
                s3Service.deleteFileFromS3(room.getModelPath());
            }
            String modelUrl = s3Service.uploadFile(model, addressFolder, "models");
            room.setModelPath(modelUrl);
        }
        return roomRepository.save(room);
    }

    public Room updateRoomWeb360(Long id, MultipartFile[] web360Files) {
        Room room = roomRepository.findById(id).orElseThrow(() -> new RuntimeException("Room not found"));
        if (web360Files != null && web360Files.length > 0) {
            List<String> existingWeb360Paths = room.getWeb360Paths();
            if (existingWeb360Paths == null) existingWeb360Paths = new ArrayList<>();
            String addressFolder = formatAddress(room.getAddress());
            for (MultipartFile web360File : web360Files) {
                if (!web360File.isEmpty()) {
                    String web360Url = s3Service.uploadFile(web360File, addressFolder, "web360");
                    if (!existingWeb360Paths.contains(web360Url)) {
                        existingWeb360Paths.add(web360Url);
                    }
                }
            }
            room.setWeb360Paths(existingWeb360Paths);
        }
        return roomRepository.save(room);
    }

    public Room updateRoomVideo(Long id, MultipartFile video) {
        Room room = roomRepository.findById(id).orElseThrow(() -> new RuntimeException("Room not found with id: " + id));
        if (video != null && !video.isEmpty()) {
            String addressFolder = formatAddress(room.getAddress());
            // Xóa video cũ trên S3 nếu có
            if (room.getVideoPaths() != null && !room.getVideoPaths().isEmpty()) {
                for (String videoPath : room.getVideoPaths()) {
                    s3Service.deleteFileFromS3(videoPath);
                }
            }
            String videoUrl = s3Service.uploadFile(video, addressFolder, "video");
            List<String> videoPaths = new ArrayList<>();
            videoPaths.add(videoUrl);
            room.setVideoPaths(videoPaths);
        }
        return roomRepository.save(room);
    }

    public Room deleteRoomImage(Long id, String imageName) {
        Room room = roomRepository.findById(id).orElseThrow(() -> new RuntimeException("Room not found"));
        List<String> updatedPaths = room.getImagePaths();
        if (updatedPaths != null) {
            // Tìm link S3 chứa imageName
            String toDelete = null;
            for (String url : updatedPaths) {
                if (url.contains(imageName)) {
                    toDelete = url;
                    break;
                }
            }
            if (toDelete != null) {
                s3Service.deleteFileFromS3(toDelete);
                updatedPaths.remove(toDelete);
                room.setImagePaths(updatedPaths);
            }
        }
        return roomRepository.save(room);
    }

    public Room deleteRoomModel(Long id) {
        Room room = roomRepository.findById(id).orElseThrow(() -> new RuntimeException("Room not found"));
        if (room.getModelPath() != null) {
            s3Service.deleteFileFromS3(room.getModelPath());
            room.setModelPath(null);
            return roomRepository.save(room);
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

        // Xóa toàn bộ file/thư mục trên S3 nếu cần, ví dụ:
        s3Service.deleteS3Folder("images/" + formattedAddress);

        // Xóa room từ database
        roomRepository.delete(room);
    }

    public List<Room> getRoomsByUser(String username) {
        return roomRepository.findByUsername(username);
    }

    public Room deleteRoomVideo(Long id) {
        Room room = roomRepository.findById(id).orElseThrow(() -> new RuntimeException("Room not found with id: " + id));
        if (room.getVideoPaths() != null && !room.getVideoPaths().isEmpty()) {
            for (String videoPath : room.getVideoPaths()) {
                s3Service.deleteFileFromS3(videoPath);
            }
            room.setVideoPaths(new ArrayList<>());
            return roomRepository.save(room);
        }
        return room;
    }

}

