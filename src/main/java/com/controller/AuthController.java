package com.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.security.jwt.JwtUtil;
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestBody TokenRequest tokenRequest) {
        String refreshToken = tokenRequest.getRefreshToken();
        if (refreshToken == null || refreshToken.trim().isEmpty()) {
            return ResponseEntity.status(400).body("Refresh token is missing or empty");
        }

        try {
            if (jwtUtil.validateToken(refreshToken, jwtUtil.extractUserName(refreshToken, true), true)) {
                String newAccessToken = jwtUtil.generateToken(jwtUtil.extractUserName(refreshToken, true));
                return ResponseEntity.ok(new TokenResponse(newAccessToken));
            } else {
                return ResponseEntity.status(403).body("Invalid refresh token");
            }
        } catch (io.jsonwebtoken.MalformedJwtException e) {
            return ResponseEntity.status(400).body("Malformed refresh token");
        } catch (Exception e) {
            e.printStackTrace();
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