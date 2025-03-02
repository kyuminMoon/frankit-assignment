package com.tistory.kmmoon.frankit.application.service;

import com.tistory.kmmoon.frankit.domain.entity.User;
import com.tistory.kmmoon.frankit.domain.exception.CustomExceptions;
import com.tistory.kmmoon.frankit.domain.repository.UserRepository;
import com.tistory.kmmoon.frankit.infrastructure.config.security.JwtTokenProvider;
import com.tistory.kmmoon.frankit.presentation.dto.request.LoginRequest;
import com.tistory.kmmoon.frankit.presentation.dto.response.TokenResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private LoginRequest loginRequest;
    
    @BeforeEach
    void setUp() {
        // 테스트 사용자 설정
        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .password("encodedPassword")
                .name("Test User")
                .role(User.Role.USER)
                .build();
        
        // 테스트 로그인 요청 설정
        loginRequest = LoginRequest.builder()
                .email("test@example.com")
                .password("password123")
                .build();
    }

    @Test
    @DisplayName("로그인 성공 테스트")
    void login_Success() {
        // given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
        when(jwtTokenProvider.createAccessToken("test@example.com")).thenReturn("access-token");
        when(jwtTokenProvider.createRefreshToken("test@example.com")).thenReturn("refresh-token");
        
        // when
        TokenResponse tokenResponse = authService.login(loginRequest);
        
        // then
        assertNotNull(tokenResponse);
        assertEquals("access-token", tokenResponse.getAccessToken());
        assertEquals("refresh-token", tokenResponse.getRefreshToken());
        assertEquals("Bearer", tokenResponse.getTokenType());
        assertEquals(3600000L, tokenResponse.getExpiresIn());
        
        verify(userRepository, times(1)).findByEmail("test@example.com");
        verify(passwordEncoder, times(1)).matches("password123", "encodedPassword");
        verify(jwtTokenProvider, times(1)).createAccessToken("test@example.com");
        verify(jwtTokenProvider, times(1)).createRefreshToken("test@example.com");
    }
    
    @Test
    @DisplayName("로그인 실패 테스트 - 존재하지 않는 이메일")
    void login_Fail_EmailNotFound() {
        // given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
        
        // when & then
        assertThrows(CustomExceptions.AuthenticationException.class, () -> {
            authService.login(loginRequest);
        });
        
        verify(userRepository, times(1)).findByEmail("test@example.com");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtTokenProvider, never()).createAccessToken(anyString());
        verify(jwtTokenProvider, never()).createRefreshToken(anyString());
    }
    
    @Test
    @DisplayName("로그인 실패 테스트 - 잘못된 비밀번호")
    void login_Fail_WrongPassword() {
        // given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(false);
        
        // when & then
        assertThrows(CustomExceptions.AuthenticationException.class, () -> {
            authService.login(loginRequest);
        });
        
        verify(userRepository, times(1)).findByEmail("test@example.com");
        verify(passwordEncoder, times(1)).matches("password123", "encodedPassword");
        verify(jwtTokenProvider, never()).createAccessToken(anyString());
        verify(jwtTokenProvider, never()).createRefreshToken(anyString());
    }
    
    @Test
    @DisplayName("로그아웃 테스트")
    void logout_Success() {
        // given
        doNothing().when(jwtTokenProvider).invalidateToken("test@example.com");
        
        // when
        authService.logout("test@example.com");
        
        // then
        verify(jwtTokenProvider, times(1)).invalidateToken("test@example.com");
    }
    
    @Test
    @DisplayName("토큰 갱신 성공 테스트")
    void refreshToken_Success() {
        // given
        String refreshToken = "valid-refresh-token";
        when(jwtTokenProvider.validateToken(refreshToken)).thenReturn(true);
        when(jwtTokenProvider.getEmail(refreshToken)).thenReturn("test@example.com");
        when(jwtTokenProvider.createAccessToken("test@example.com")).thenReturn("new-access-token");
        when(jwtTokenProvider.createRefreshToken("test@example.com")).thenReturn("new-refresh-token");
        
        // when
        TokenResponse tokenResponse = authService.refreshToken(refreshToken);
        
        // then
        assertNotNull(tokenResponse);
        assertEquals("new-access-token", tokenResponse.getAccessToken());
        assertEquals("new-refresh-token", tokenResponse.getRefreshToken());
        assertEquals("Bearer", tokenResponse.getTokenType());
        assertEquals(3600000L, tokenResponse.getExpiresIn());
        
        verify(jwtTokenProvider, times(1)).validateToken(refreshToken);
        verify(jwtTokenProvider, times(1)).getEmail(refreshToken);
        verify(jwtTokenProvider, times(1)).createAccessToken("test@example.com");
        verify(jwtTokenProvider, times(1)).createRefreshToken("test@example.com");
    }
    
    @Test
    @DisplayName("토큰 갱신 실패 테스트 - 유효하지 않은 리프레시 토큰")
    void refreshToken_Fail_InvalidToken() {
        // given
        String refreshToken = "invalid-refresh-token";
        when(jwtTokenProvider.validateToken(refreshToken)).thenReturn(false);
        
        // when & then
        assertThrows(CustomExceptions.AuthenticationException.class, () -> {
            authService.refreshToken(refreshToken);
        });
        
        verify(jwtTokenProvider, times(1)).validateToken(refreshToken);
        verify(jwtTokenProvider, never()).getEmail(anyString());
        verify(jwtTokenProvider, never()).createAccessToken(anyString());
        verify(jwtTokenProvider, never()).createRefreshToken(anyString());
    }
}