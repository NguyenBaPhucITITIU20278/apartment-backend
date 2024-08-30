package com.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Component;

import java.util.Date;

public class JwtUtil {

    private String secretKey = "Authorization";
    private String refreshSecretKey = "refreshToken";

    public String generateToken(String userName) {
        return Jwts.builder()
                .setSubject(userName)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60)) // 1 giờ
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    public String generateRefreshToken(String userName) {
        return Jwts.builder()
                .setSubject(userName)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24 * 7)) // 7 ngày
                .signWith(SignatureAlgorithm.HS256, refreshSecretKey)
                .compact();
    }

    public Claims extractClaims(String token, boolean isRefreshToken) {
        String key = isRefreshToken ? refreshSecretKey : secretKey;
        return Jwts.parser()
                .setSigningKey(key)
                .parseClaimsJws(token)
                .getBody();
    }

    public String extractUserName(String token, boolean isRefreshToken) {
        return extractClaims(token, isRefreshToken).getSubject();
    }

    public boolean isTokenExpired(String token, boolean isRefreshToken) {
        return extractClaims(token, isRefreshToken).getExpiration().before(new Date());
    }

    public boolean validateToken(String token, String userName, boolean isRefreshToken) {
        return (extractUserName(token, isRefreshToken).equals(userName) && !isTokenExpired(token, isRefreshToken));
    }
}