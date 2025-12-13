package com.camping.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;

/**
 * JWT Token 工具类
 */
public class JwtUtil {

    private static final String SECRET_KEY = "camping_system_secret_key_2024_make_this_string_very_long_to_satisfy_hs512_security_requirements";
    private static final long EXPIRATION = 24 * 60 * 60 * 1000; // 24 小时

    /**
     * 生成 Token
     */
    public static String generateToken(Long userId, String username, String role) {

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + EXPIRATION);

        Key key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .claim("userId", userId)
                .claim("username", username)
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * 验证并解析 Token (适配 JJWT 0.12.x+)
     */
    public static Claims parseToken(String token) {
        try {
            byte[] keyBytes = SECRET_KEY.getBytes(StandardCharsets.UTF_8);
            javax.crypto.SecretKey key = Keys.hmacShaKeyFor(keyBytes);

            return Jwts.parser()
                    .verifyWith(key)        // 0.12.x 使用 verifyWith 替代 setSigningKey
                    .build()
                    .parseSignedClaims(token) // 0.12.x 使用 parseSignedClaims 替代 parseClaimsJws
                    .getPayload();            // 0.12.x 使用 getPayload 替代 getBody
        } catch (Exception e) {
            // 这里可以打印一下堆栈，方便调试真正的原因
            // e.printStackTrace();
            throw new RuntimeException("Token 验证失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取 Token 中的 userId
     */
    public static Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        return ((Number) claims.get("userId")).longValue();
    }

    /**
     * 检查 Token 是否过期
     */
    public static boolean isTokenExpired(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }
}
