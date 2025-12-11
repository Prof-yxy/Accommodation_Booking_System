package com.camping.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;

/**
 * JWT Token 工具类
 */
public class JwtUtil {

    private static final String SECRET_KEY = "camping_system_secret_key_2024";
    private static final long EXPIRATION = 24 * 60 * 60 * 1000; // 24 小时

    /**
     * 生成 Token
     */
    public static String generateToken(Long userId, String username, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("role", role);

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + EXPIRATION);

        Key key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * 验证并解析 Token
     */
    public static Claims parseToken(String token) {
        try {
            byte[] keyBytes = SECRET_KEY.getBytes(StandardCharsets.UTF_8);
            Key key = Keys.hmacShaKeyFor(keyBytes);

            // Use reflection to support different jjwt versions (parserBuilder vs parser).
            Class<?> jwtsClass = Class.forName("io.jsonwebtoken.Jwts");

            try {
                // Prefer parserBuilder() -> setSigningKey(Key) -> build() -> parseClaimsJws(token)
                java.lang.reflect.Method parserBuilderMethod = jwtsClass.getMethod("parserBuilder");
                Object builder = parserBuilderMethod.invoke(null);

                // Try setSigningKey(Key) first, fallback to setSigningKey(byte[])
                java.lang.reflect.Method setSigningKeyMethod = null;
                try {
                    setSigningKeyMethod = builder.getClass().getMethod("setSigningKey", java.security.Key.class);
                    builder = setSigningKeyMethod.invoke(builder, key);
                } catch (NoSuchMethodException ex) {
                    setSigningKeyMethod = builder.getClass().getMethod("setSigningKey", byte[].class);
                    builder = setSigningKeyMethod.invoke(builder, (Object) keyBytes);
                }

                java.lang.reflect.Method buildMethod = builder.getClass().getMethod("build");
                Object parser = buildMethod.invoke(builder);

                java.lang.reflect.Method parseClaimsJwsMethod = parser.getClass().getMethod("parseClaimsJws", String.class);
                Object jws = parseClaimsJwsMethod.invoke(parser, token);
                java.lang.reflect.Method getBodyMethod = jws.getClass().getMethod("getBody");
                return (Claims) getBodyMethod.invoke(jws);
            } catch (NoSuchMethodException nsme) {
                // Fallback to older parser() API: parser().setSigningKey(byte[]).parseClaimsJws(token)
                java.lang.reflect.Method parserMethod = jwtsClass.getMethod("parser");
                Object parserObj = parserMethod.invoke(null);
                java.lang.reflect.Method setSigningKeyMethod = parserObj.getClass().getMethod("setSigningKey", byte[].class);
                Object parserAfterSet = setSigningKeyMethod.invoke(parserObj, (Object) keyBytes);
                java.lang.reflect.Method parseClaimsJwsMethod = parserAfterSet.getClass().getMethod("parseClaimsJws", String.class);
                Object jws = parseClaimsJwsMethod.invoke(parserAfterSet, token);
                java.lang.reflect.Method getBodyMethod = jws.getClass().getMethod("getBody");
                return (Claims) getBodyMethod.invoke(jws);
            }
        } catch (Exception e) {
            throw new RuntimeException("Token 验证失败", e);
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
