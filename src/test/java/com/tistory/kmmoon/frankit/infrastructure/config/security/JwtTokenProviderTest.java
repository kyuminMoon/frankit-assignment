package com.tistory.kmmoon.frankit.infrastructure.config.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.security.Key;
import java.time.Duration;
import java.util.Base64;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtTokenProviderTest {

    @InjectMocks
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private final long accessTokenExpiration = 3600000L; // 1시간
    private final long refreshTokenExpiration = 86400000L; // 24시간
    private final String testEmail = "test@example.com";

    @BeforeEach
    void setUp() {
        String secretKey = "secret-key-1234567890-1234567890-1234567890-1234567890";
        ReflectionTestUtils.setField(jwtTokenProvider, "secretKey", secretKey);
        ReflectionTestUtils.setField(jwtTokenProvider, "accessTokenExpiration", accessTokenExpiration);
        ReflectionTestUtils.setField(jwtTokenProvider, "refreshTokenExpiration", refreshTokenExpiration);
        
        // init() 메소드 동작 모방
        String encodedKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
        Key key = Keys.hmacShaKeyFor(encodedKey.getBytes());
        ReflectionTestUtils.setField(jwtTokenProvider, "key", key);

        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    @DisplayName("액세스 토큰 생성 테스트")
    void createAccessToken() {
        // given
        doNothing().when(valueOperations).set(anyString(), anyString(), any(Duration.class));

        // when
        String token = jwtTokenProvider.createAccessToken(testEmail);

        // then
        assertThat(token).isNotNull();

        Claims claims = Jwts.parser()
                .verifyWith((SecretKey)ReflectionTestUtils.getField(jwtTokenProvider, "key"))
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertThat(claims.getSubject()).isEqualTo(testEmail);
        verify(redisTemplate).opsForValue();
        verify(valueOperations).set(eq("ACCESS_TOKEN:" + testEmail), eq(token), any(Duration.class));
    }

    @Test
    @DisplayName("리프레시 토큰 생성 테스트")
    void createRefreshToken() {
        // given
        doNothing().when(valueOperations).set(anyString(), anyString(), any(Duration.class));

        // when
        String token = jwtTokenProvider.createRefreshToken(testEmail);

        // then
        assertThat(token).isNotNull();

        Claims claims = Jwts.parser()
                .verifyWith((SecretKey)ReflectionTestUtils.getField(jwtTokenProvider, "key"))
                .build()
                .parseSignedClaims(token)
                .getPayload();
        
        assertThat(claims.getSubject()).isEqualTo(testEmail);
        verify(redisTemplate).opsForValue();
        verify(valueOperations).set(eq("REFRESH_TOKEN:" + testEmail), eq(token), any(Duration.class));
    }

    @Test
    @DisplayName("토큰에서 인증 정보 추출 테스트")
    void getAuthentication() {
        // given
        String token = jwtTokenProvider.createAccessToken(testEmail);
        
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetailsService.loadUserByUsername(testEmail)).thenReturn(userDetails);
        when(valueOperations.get("ACCESS_TOKEN:" + testEmail)).thenReturn(token);
        when(userDetails.getAuthorities()).thenReturn(List.of());

        // when
        Authentication authentication = jwtTokenProvider.getAuthentication(token);

        // then
        assertThat(authentication).isNotNull();
        assertThat(authentication).isInstanceOf(UsernamePasswordAuthenticationToken.class);
        assertThat(authentication.getPrincipal()).isEqualTo(userDetails);
    }

    @Test
    @DisplayName("토큰 유효성 검증 테스트")
    void validateToken() {
        // given
        String token = jwtTokenProvider.createAccessToken(testEmail);

        // when
        boolean isValid = jwtTokenProvider.validateToken(token);

        // then
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("토큰 무효화 테스트")
    void invalidateToken() {
        // given
        doReturn(true).when(redisTemplate).delete(anyString());

        // when
        jwtTokenProvider.invalidateToken(testEmail);

        // then
        verify(redisTemplate).delete("ACCESS_TOKEN:" + testEmail);
        verify(redisTemplate).delete("REFRESH_TOKEN:" + testEmail);
    }
}