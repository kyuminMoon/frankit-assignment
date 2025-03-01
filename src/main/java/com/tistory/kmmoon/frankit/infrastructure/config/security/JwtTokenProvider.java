package com.tistory.kmmoon.frankit.infrastructure.config.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.Key;
import java.time.Duration;
import java.util.Base64;
import java.util.Date;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    @Value("${spring.jwt.secret-key}")
    private String secretKey;

    @Value("${spring.jwt.access-token-expiration}")
    private long accessTokenExpiration;

    @Value("${spring.jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    private Key key;

    private final UserDetailsService userDetailsService;
    private final RedisTemplate<String, String> redisTemplate;


    @PostConstruct
    protected void init() {
        this.key = Jwts.SIG.HS256.key().build();

        byte[] keyBytes = Base64.getDecoder().decode(Base64.getEncoder().encodeToString(secretKey.getBytes()));
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    // 액세스 토큰 생성
    public String createAccessToken(String email) {
        Claims claims = Jwts.claims().subject(email).build();
        Date now = new Date();
        Date validity = new Date(now.getTime() + accessTokenExpiration);

        String token = Jwts.builder()
                .claims(claims)
                .issuedAt(now)
                .expiration(validity)
                .signWith(key)
                .compact();

        // Redis에 토큰 저장 (TTL 설정)
        redisTemplate.opsForValue().set(
                "ACCESS_TOKEN:" + email,
                token,
                Duration.ofMillis(accessTokenExpiration)
        );

        return token;
    }

    // 리프레시 토큰 생성
    public String createRefreshToken(String email) {
        Claims claims = Jwts.claims().subject(email).build();
        Date now = new Date();
        Date validity = new Date(now.getTime() + refreshTokenExpiration);

        String token = Jwts.builder()
                .claims(claims)
                .issuedAt(now)
                .expiration(validity)
                .signWith(key)
                .compact();

        // Redis에 리프레시 토큰 저장 (TTL 설정)
        redisTemplate.opsForValue().set(
                "REFRESH_TOKEN:" + email,
                token,
                Duration.ofMillis(refreshTokenExpiration)
        );

        return token;
    }

    // 토큰에서 인증 정보 추출
    public Authentication getAuthentication(String token) {
        String email = getEmail(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        
        // Redis에서 토큰 유효성 확인
        String storedToken = redisTemplate.opsForValue().get("ACCESS_TOKEN:" + email);
        if (!storedToken.equals(token)) {
            throw new JwtException("Invalid or expired token");
        }
        
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    // 토큰에서 이메일 추출
    public String getEmail(String token) {
        return Jwts.parser()
                .verifyWith((SecretKey)key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    // 토큰 유효성 검증
    public boolean validateToken(String token) {
        try {
            Jws<Claims> claims = Jwts.parser()
                    .verifyWith((SecretKey) key)
                    .build()
                    .parseSignedClaims(token);
            return !claims.getPayload().getExpiration().before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            log.error("유효하지 않은 JWT 토큰: {}", e.getMessage());
            return false;
        }
    }

    // 토큰 만료 처리
    public void invalidateToken(String email) {
        redisTemplate.delete("ACCESS_TOKEN:" + email);
        redisTemplate.delete("REFRESH_TOKEN:" + email);
    }
}