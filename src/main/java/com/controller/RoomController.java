package com.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.model.Room;
import com.model.RoomRequest;
import com.security.jwt.JwtUtil;
import com.services.RoomService;

@EnableAutoConfiguration
@Configuration
@ComponentScan
@RestController
@RequestMapping("/api/rooms")
@CrossOrigin(origins = "http://localhost:3000")
public class RoomController {

    private static final Logger logger = LoggerFactory.getLogger(RoomController.class);

    @Autowired
    private RoomService roomService;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping("/all-rooms")
    public ResponseEntity<List<Room>> getAllRooms() {
        logger.info("Getting all rooms");
        // String accessToken = authorizationHeader.replace("Bearer ", "");
        // boolean isValidToken = jwtUtil.validateToken(accessToken, userName);
        // if (!isValidToken) {
        // return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        // }

        List<Room> rooms = roomService.getAllRooms();
        return new ResponseEntity<>(rooms, HttpStatus.OK);
    }

    @PostMapping("/rooms-by-address")
    public ResponseEntity<List<Room>> getRoomByAddress(@RequestBody RoomRequest roomRequest) {
        System.out.println("Starting getRoomByAddress");
        logger.info("Getting rooms by address");
        String address = roomRequest.getAddress();
        List<Room> rooms = roomService.getRoomByAddress(address);
        return new ResponseEntity<>(rooms, HttpStatus.OK);
    }

    @PostMapping("/add-room")
    public ResponseEntity<?> addRoom(@RequestParam("files") MultipartFile[] files, @RequestParam("data") String data) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Room room = objectMapper.readValue(data, Room.class);
            room.setPostedTime(LocalDateTime.now());
            // Gọi phương thức addRoom với mảng tệp
            roomService.addRoom(room, files, null);
            return ResponseEntity.ok("Room added successfully with images");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error adding room: " + e.getMessage());
        }
    }

    @PostMapping("/add-room-with-model")
    public ResponseEntity<?> addRoomWithModel(
            @RequestParam(value = "files", required = false) MultipartFile[] files,
            @RequestParam(value = "model", required = false) MultipartFile[] model,
            @RequestParam(value = "web360", required = false) MultipartFile[] web360,
            @RequestParam(value = "video", required = false) MultipartFile video,
            @RequestParam("data") String data,
            @RequestHeader("Authorization") String token) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Room room = objectMapper.readValue(data, Room.class);
            room.setPostedTime(LocalDateTime.now());

            // Extract username from JWT token
            String jwtToken = token.substring(7);
            String username = jwtUtil.extractUserName(jwtToken, false);
            room.setUsername(username);

            // Ensure files, model, and web360 are not null
            files = files != null ? files : new MultipartFile[0];
            model = model != null ? model : new MultipartFile[0];
            web360 = web360 != null ? web360 : new MultipartFile[0];
            
            // Call the addRoomWithModel method with all files including video
            roomService.addRoomWithModel(room, files, model, web360, video);
            return ResponseEntity.ok("Room added successfully with images, 3D model, web360 and video");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error adding room: " + e.getMessage());
        }
    }

    @GetMapping("/room-by-id/{id}")
    public ResponseEntity<?> getRoomById(@PathVariable Long id) {
        try {
            Room room = roomService.getRoomById(id);
            return new ResponseEntity<>(room, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace(); // Log the stack trace for debugging
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error getting room by id: " + e.getMessage());
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<Room>> searchRooms(@RequestParam("query") String query) {
        logger.info("Searching rooms with query: {}", query);
        List<Room> rooms = roomService.searchRooms(query);
        return new ResponseEntity<>(rooms, HttpStatus.OK);
    }

    @PostMapping("/update-room/{id}")
    public ResponseEntity<?> updateRoom(@PathVariable Long id, @RequestBody RoomRequest roomRequest, @RequestHeader("Authorization") String token) {
        try {
            String jwtToken = token.substring(7);
            String currentUserName = jwtUtil.extractUserName(jwtToken, false);
            
            Room room = roomService.getRoomById(id);
            if (!room.getUsername().equals(currentUserName)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You do not have permission to edit this room");
            }

            Room updatedRoom = roomService.updateRoom(id, roomRequest);
            return ResponseEntity.status(HttpStatus.OK).body(updatedRoom);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating room: " + e.getMessage());
        }
    }


    @PostMapping("/update-room-images/{id}")
    public ResponseEntity<?> updateRoomImages(
            @PathVariable Long id,
            @RequestParam(value = "files", required = false) MultipartFile[] images,
            @RequestHeader("Authorization") String token) {
        try {
            String jwtToken = token.substring(7);
            String currentUserName = jwtUtil.extractUserName(jwtToken, false);
            
            Room room = roomService.getRoomById(id);
            if (!room.getUsername().equals(currentUserName)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You do not have permission to update images for this room");
            }

            // Log the received images
            if (images != null && images.length > 0) {
                logger.info("Received {} images for room ID {}.", images.length, id);
                for (MultipartFile image : images) {
                    logger.info("Image name: {}, Size: {} bytes", image.getOriginalFilename(), image.getSize());
                }
            } else {
                logger.warn("No images received for room ID {}.", id);
            }

            Room updatedRoom = roomService.updateRoomImages(id, images);
            return ResponseEntity.status(HttpStatus.OK).body(updatedRoom);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating room images: " + e.getMessage());
        }
    }

    @PostMapping("/update-room-model/{id}")
    public ResponseEntity<?> updateRoomModel(
            @PathVariable Long id,
            @RequestParam(value = "model", required = false) MultipartFile model,
            @RequestHeader("Authorization") String token) {
        try {
            String jwtToken = token.substring(7);
            String currentUserName = jwtUtil.extractUserName(jwtToken, false);
            
            Room room = roomService.getRoomById(id);
            if (!room.getUsername().equals(currentUserName)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You do not have permission to update the model for this room");
            }

            Room updatedRoom = roomService.updateRoomModel(id, model);
            return ResponseEntity.status(HttpStatus.OK).body(updatedRoom);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating room model: " + e.getMessage());
        }
    }

    @PostMapping("/update-room-web360/{id}")
    public ResponseEntity<?> updateRoomWeb360(
            @PathVariable Long id,
            @RequestParam(value = "web360", required = false) MultipartFile[] web360Files,
            @RequestHeader("Authorization") String token) {
        try {
            String jwtToken = token.substring(7);
            String currentUserName = jwtUtil.extractUserName(jwtToken, false);
            
            Room room = roomService.getRoomById(id);
            if (!room.getUsername().equals(currentUserName)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You do not have permission to update web360 for this room");
            }

            Room updatedRoom = roomService.updateRoomWeb360(id, web360Files);
            return ResponseEntity.status(HttpStatus.OK).body(updatedRoom);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating room web360: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete-room-image/{id}/{imageName}")
    public ResponseEntity<?> deleteRoomImage(
            @PathVariable Long id,
            @PathVariable String imageName,
            @RequestHeader("Authorization") String token) {
        try {
            String jwtToken = token.substring(7);
            String currentUserName = jwtUtil.extractUserName(jwtToken, false);
            
            Room room = roomService.getRoomById(id);
            if (!room.getUsername().equals(currentUserName)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You do not have permission to delete images for this room");
            }

            Room updatedRoom = roomService.deleteRoomImage(id, imageName);
            return ResponseEntity.ok(updatedRoom);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting room image: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete-room-model/{id}")
    public ResponseEntity<?> deleteRoomModel(@PathVariable Long id, @RequestHeader("Authorization") String token) {
        try {
            String jwtToken = token.substring(7);
            String currentUserName = jwtUtil.extractUserName(jwtToken, false);
            
            Room room = roomService.getRoomById(id);
            if (!room.getUsername().equals(currentUserName)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You do not have permission to delete the model for this room");
            }

            Room updatedRoom = roomService.deleteRoomModel(id);
            return ResponseEntity.ok(updatedRoom);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting room model: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete-room-web360/{id}/{web360Name}")
    public ResponseEntity<?> deleteRoomWeb360(
            @PathVariable Long id,
            @PathVariable String web360Name,
            @RequestHeader("Authorization") String token) {
        try {
            String jwtToken = token.substring(7);
            String currentUserName = jwtUtil.extractUserName(jwtToken, false);
            
            Room room = roomService.getRoomById(id);
            if (!room.getUsername().equals(currentUserName)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You do not have permission to delete web360 for this room");
            }

            Room updatedRoom = roomService.deleteRoomWeb360(id, web360Name);
            return ResponseEntity.ok(updatedRoom);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting room web360: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete-room/{id}")
    public ResponseEntity<?> deleteEntireRoom(@PathVariable Long id, @RequestHeader("Authorization") String token) {
        try {
            String jwtToken = token.substring(7);
            String currentUserName = jwtUtil.extractUserName(jwtToken, false);
            
            Room room = roomService.getRoomById(id);
            if (!room.getUsername().equals(currentUserName)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You do not have permission to delete this room");
            }

            roomService.deleteEntireRoom(id);
            return ResponseEntity.ok("Room and all associated files deleted successfully");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting room: " + e.getMessage());
        }
    }

    @GetMapping("/my-rooms")
    public ResponseEntity<?> getMyRooms(@RequestHeader("Authorization") String token) {
        try {
            String jwtToken = token.substring(7);
            String currentUserName = jwtUtil.extractUserName(jwtToken, false);
            
            List<Room> rooms = roomService.getRoomsByUser(currentUserName);
            return new ResponseEntity<>(rooms, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error getting rooms: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/video")
    public ResponseEntity<Room> updateRoomVideo(
            @PathVariable Long id,
            @RequestParam("video") MultipartFile video) {
        try {
            Room updatedRoom = roomService.updateRoomVideo(id, video);
            return ResponseEntity.ok(updatedRoom);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}/video")
    public ResponseEntity<Room> deleteRoomVideo(@PathVariable Long id) {
        try {
            Room updatedRoom = roomService.deleteRoomVideo(id);
            return ResponseEntity.ok(updatedRoom);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    public ResponseEntity<Room> addRoom(
            @RequestParam(value = "files", required = false) MultipartFile[] files,
            @RequestParam(value = "video", required = false) MultipartFile video,
            @RequestParam("room") String roomJson) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Room room = mapper.readValue(roomJson, Room.class);
            Room savedRoom = roomService.addRoom(room, files, video);
            return ResponseEntity.ok(savedRoom);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
