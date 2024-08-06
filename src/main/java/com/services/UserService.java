package com.services;

import com.model.UserEntity;
import com.repository.UserRepository;
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


@EnableAutoConfiguration
@Configuration
@ComponentScan
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private OtpRepository otpRepository;
    @Autowired
    private EmailService emailService;

    public List<UserEntity> getAllUsers() {
        return userRepository.findAll();
    }

    public UserEntity createUser(UserEntity user) {
        user.setId(UUID.randomUUID().toString());
        return userRepository.save(user);
    }

    public UserEntity updateUser(String userName, UserEntity userDetails) {
        UserEntity user = userRepository.findByUserName(userName);
        if (user == null) {
            throw new RuntimeException("User not found with userName " + userName);
        }
        user.setUserName(userDetails.getUserName());
        user.setPassword(userDetails.getPassword());
        user.setEmail(userDetails.getEmail());
        user.setPhone(userDetails.getPhone());
        return userRepository.save(user);
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
        if (foundUser != null) {
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

    public void sendOtp(Otp otp ) {
        if(otp.getEmail() == null){
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
        if (!verifyOtp(otp, email )) {
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

   
}