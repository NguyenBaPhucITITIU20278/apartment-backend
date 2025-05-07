package com.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.security.jwt.JwtUtil;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestBody TokenRequest tokenRequest) {
        logger.info("Received refresh token request");
        
        String refreshToken = tokenRequest.getRefreshToken();
        if (refreshToken == null || refreshToken.trim().isEmpty()) {
            logger.error("Refresh token is missing or empty");
            return ResponseEntity.status(400).body("Refresh token is missing or empty");
        }

        try {
            logger.debug("Validating refresh token");
            String username = jwtUtil.extractUserName(refreshToken, true);
            logger.debug("Username extracted from token: {}", username);
            
            if (jwtUtil.validateToken(refreshToken, username, true)) {
                String newAccessToken = jwtUtil.generateToken(username);
                logger.info("New access token generated successfully for user: {}", username);
                return ResponseEntity.ok(new TokenResponse(newAccessToken));
            } else {
                logger.error("Invalid refresh token for user: {}", username);
                return ResponseEntity.status(403).body("Invalid refresh token");
            }
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            logger.error("Refresh token has expired", e);
            return ResponseEntity.status(401).body("Refresh token has expired");
        } catch (io.jsonwebtoken.MalformedJwtException e) {
            logger.error("Malformed refresh token", e);
            return ResponseEntity.status(400).body("Malformed refresh token");
        } catch (Exception e) {
            logger.error("Error processing refresh token", e);
            return ResponseEntity.status(500).body("Internal Server Error");
        }
    }
}

class TokenRequest {
    private String refreshToken;

    // Getters and setters
    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}

class TokenResponse {
    private String accessToken;

    public TokenResponse(String accessToken) {
        this.accessToken = accessToken;
    }

    // Getters and setters
    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
}