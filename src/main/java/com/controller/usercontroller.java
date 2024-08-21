package com.controller;

import com.model.UserEntity;
import com.model.Role;
import com.model.Contact;
import com.services.UserService;
import com.security.jwt.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.yaml.snakeyaml.tokens.Token.ID;
import com.model.Otp;

import java.util.Map;
import java.util.List;

@EnableAutoConfiguration
@Configuration
@ComponentScan
@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:3000")
public class usercontroller {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @GetMapping("/all")
    public List<UserEntity> getAllUsers() {
        return userService.getAllUsers();
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserEntity user) {
        try {
            if (user.getUserName() == null || user.getPassword() == null) {
                System.out.println(user.getUserName());
                System.out.println(user.getPassword());
                System.out.println("Missing username or password");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing username or password");
            }
            UserEntity existingUser = userService.findUser(user.getUserName());
            if (existingUser == null
                    || !bCryptPasswordEncoder.matches(user.getPassword(), existingUser.getPassword())) {
                System.out.println("Invalid username or password");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
            }
            Map<String, String> tokens = userService.login(existingUser);
            System.out.println(tokens);
            return ResponseEntity.status(HttpStatus.OK).body(tokens);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
        }
    }

    @PostMapping("/create")
    public ResponseEntity<?> createUser(@RequestBody UserEntity user) {
        if (user.getPassword() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Password cannot be null");
        }
        boolean checkUser = userService.checkEmail(user.getEmail(), user.getUserName());
        System.out.println(user.getUserName());
        if (checkUser) {
            System.out.println("User already exists");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "User already exists"));
        }
        String encodedPassword = bCryptPasswordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);

        // Kiểm tra và tạo Contact nếu null
        if (user.getContact() == null) {
            Contact contact = new Contact();
            contact.setFirstName("DefaultFirstName"); // Thay thế bằng giá trị mặc định hoặc từ yêu cầu
            contact.setLastName("DefaultLastName");   // Thay thế bằng giá trị mặc định hoặc từ yêu cầu
            contact.setPhone("DefaultPhone");         // Thay thế bằng giá trị mặc định hoặc từ yêu cầu
            user.setContact(contact);
        }

        // Kiểm tra và tạo Role nếu null
        if (user.getRole() == null) {
            Role role = new Role();
            role.setRoleName("user");
            user.setRole(role);
        }

        UserEntity createdUser = userService.createUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    @PostMapping("/updateUser")
    public ResponseEntity<?> updateUser(@RequestBody UserEntity user) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.updateUser(user));
    }

    @GetMapping("/getUser")
    public ResponseEntity<?> getUser(@RequestHeader("Authorization") String token) {
        try {
            if (token == null || !token.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing or invalid token");
            }
            String jwtToken = token.substring(7);
            String userName = jwtUtil.extractUserName(jwtToken);
            UserEntity user = userService.getUser(userName);
            return ResponseEntity.status(HttpStatus.OK).body(user);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
        }
    }

    @GetMapping("/checkUser")
    public ResponseEntity<?> checkUser(@RequestParam String userName) {
        try {
            if (userName == null || userName.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing or empty username");
            }
            UserEntity checkedUser = userService.findUser(userName);
            if (checkedUser != null) {
                return ResponseEntity.status(HttpStatus.OK).body(checkedUser);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
        }
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @PostMapping("/sendOtp")
    public ResponseEntity<?> sendOtp(@RequestBody Otp otp) {
        try {
            userService.sendOtp(otp);
            return ResponseEntity.status(HttpStatus.OK).body("Otp sent successfully");
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
        }
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @PostMapping("/resetPassword")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        try {
            String otp = request.get("otp");
            String email = request.get("email");
            String newPassword = request.get("newPassword");

            if (otp == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing otp ");
            }
            if (email == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing email");
            }
            if (newPassword == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing new password ");
            }

            boolean checkOtp = userService.verifyOtp(otp, email);
            if (!checkOtp) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Otp");
            }
            String encodedPassword = bCryptPasswordEncoder.encode(newPassword);
            userService.resetPassword(otp, encodedPassword, email);
            return ResponseEntity.status(HttpStatus.OK).body("Password reset successfully");
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
        }
    }
}