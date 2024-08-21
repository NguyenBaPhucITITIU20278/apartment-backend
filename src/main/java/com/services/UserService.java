package com.services;

import com.model.UserEntity;
import com.repository.userRepository;
import com.repository.ContactRepository;
import com.repository.RoleRepository;
import com.security.jwt.JwtUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import com.model.Otp;
import com.repository.OtpRepository;
import java.util.Date;
import java.util.Random;
import com.services.EmailService;
import java.lang.Exception;
import java.time.LocalDateTime;
import org.springframework.transaction.annotation.Transactional;
import com.model.Role;
import com.model.Contact;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@EnableAutoConfiguration
@Configuration
@ComponentScan
@Service
public class UserService {

    @Autowired
    private userRepository userRepository;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private OtpRepository otpRepository;
    @Autowired
    private EmailService emailService;
    @Autowired
    private ContactRepository contactRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    public List<UserEntity> getAllUsers() {
        return userRepository.findAll();
    }

    public UserEntity createUser(UserEntity user) {
        if (user.getContact() == null) {
            throw new IllegalArgumentException("Contact must not be null");
        }
        if (user.getRole() == null) {
            throw new IllegalArgumentException("Role must not be null");
        }
        Contact contact = contactRepository.save(user.getContact());
        Role role = roleRepository.save(user.getRole());
        user.setContact(contact);
        user.setRole(role);
        return userRepository.save(user);
    }

    public UserEntity updateUser(UserEntity user) {
        UserEntity foundUser = userRepository.findByUserName(user.getUserName());
        if (foundUser == null) {
            throw new RuntimeException("User not found with userName " + user.getUserName());
        }

        // Cập nhật thông tin liên hệ nếu có thay đổi
        Contact contact = foundUser.getContact();
        if (contact != null) {
            contact.setFirstName(user.getContact().getFirstName());
            contact.setLastName(user.getContact().getLastName());
            contact.setPhone(user.getContact().getPhone());
            contactRepository.save(contact);
        } else {
            contact = contactRepository.save(user.getContact());
            foundUser.setContact(contact);
        }

        // Cập nhật thông tin vai trò nếu có thay đổi
        Role role = foundUser.getRole();
        if (role != null) {
            role.setRoleName(user.getRole().getRoleName());
            roleRepository.save(role);
        } else {
            role = roleRepository.save(user.getRole());
            foundUser.setRole(role);
        }

        foundUser.setEmail(user.getEmail());
        String encodedPassword = bCryptPasswordEncoder.encode(user.getPassword());
        foundUser.setPassword(encodedPassword);
        return userRepository.save(foundUser);
    }

    public void deleteUser(String userName) {
        UserEntity user = userRepository.findByUserName(userName);
        if (user == null) {
            throw new RuntimeException("User not found with userName " + userName);
        }
        userRepository.delete(user);
    }

    public Map<String, String> login(UserEntity user) {
        UserEntity foundUser = userRepository.findByUserNameAndPassword(user.getUserName(), user.getPassword());
        System.out.println(foundUser);
        if (foundUser != null && foundUser.getRole().getRoleName().equals("user")) {
            String accessToken = jwtUtil.generateToken(foundUser.getUserName());
            Map<String, String> tokens = new HashMap<>();
            tokens.put("accessToken", accessToken);
            tokens.put("refreshToken", "dummyRefreshToken");
            return tokens;
        }
        return null;
    }

    public UserEntity getUser(String userName) {
        return userRepository.findByUserName(userName);
    }

    public UserEntity findUser(String userName) {
        return userRepository.findByUserName(userName);
    }

    public void sendOtp(Otp otp) {
        if (otp.getEmail() == null) {
            throw new RuntimeException("Email is required");
        }
        otp.setOtp(String.valueOf(new Random().nextInt(900000) + 100000));
        otp.setCreateAt(new Date().toString());
        otpRepository.save(otp);

        // Send email OTP
        String subject = "OTP";
        String text = "Your OTP is: " + otp.getOtp();
        emailService.sendEmail(otp.getEmail(), subject, text);
    }

    @Transactional
    public void resetPassword(String otp, String newPassword, String email) throws Exception {
        // Verify the OTP
        if (!verifyOtp(otp, email)) {
            throw new Exception("Invalid OTP");
        }

        // Find the user by OTP
        UserEntity user = userRepository.findByEmail(email);
        if (user == null) {
            throw new Exception("User not found");
        }
        // Update the user's password
        user.setPassword(newPassword);
        userRepository.save(user);

        // Delete the OTP after successful password reset
        otpRepository.deleteByOtp(otp);
    }

    public boolean verifyOtp(String otp, String email) {
        Otp otpEntity = otpRepository.findByOtpAndEmail(otp, email);
        if (otpEntity == null) {
            return false;
        }
        return true;
    }

    public boolean checkEmail(String email, String userName) {
        UserEntity emailCheck = userRepository.findByEmail(email);
        UserEntity nameCheck = userRepository.findByUserName(userName);
        if (emailCheck != null || nameCheck != null) {
            return true;
        }
        return false;
    }
}