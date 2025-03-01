package com.tistory.kmmoon.frankit.presentation.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tistory.kmmoon.frankit.application.service.AuthService;
import com.tistory.kmmoon.frankit.domain.exception.CustomExceptions;
import com.tistory.kmmoon.frankit.infrastructure.logging.ApplicationLogger;
import com.tistory.kmmoon.frankit.presentation.dto.request.LoginRequest;
import com.tistory.kmmoon.frankit.presentation.dto.response.TokenResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WithMockUser(username = "test@example.com") // 사용자 이메일 지정
@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ApplicationLogger applicationLogger;
    @MockitoBean
    private AuthService authService;

    @Test
    @DisplayName("로그인 성공 테스트")
    void loginSuccess() throws Exception {
        // given
        LoginRequest login = new LoginRequest("test@example.com", "password123");
        TokenResponse token = new TokenResponse("accessToken", "refreshToken", "Bearer", 3600000L);

        when(authService.login(any(LoginRequest.class))).thenReturn(token);

        // when & then
        mockMvc.perform(post("/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("accessToken"))
                .andExpect(jsonPath("$.refreshToken").value("refreshToken"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(3600000));

        verify(authService, times(1)).login(any(LoginRequest.class));
    }

    @Test
    @DisplayName("로그인 실패 테스트 - 잘못된 자격증명")
    void loginFailWithInvalidCredentials() throws Exception {
        // given
        LoginRequest LoginRequest = new LoginRequest("test@example.com", "wrongpassword");

        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new CustomExceptions.AuthenticationException("이메일 또는 비밀번호가 잘못되었습니다."));

        // when & then
        mockMvc.perform(post("/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(LoginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("이메일 또는 비밀번호가 잘못되었습니다."));

        verify(authService, times(1)).login(any(LoginRequest.class));
    }

    @Test
    @WithMockUser
    @DisplayName("로그아웃 테스트")
    void logout() throws Exception {
        // given
        doNothing().when(authService).logout(anyString());

        // when & then
        mockMvc.perform(post("/auth/logout")
                .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("토큰 갱신 테스트")
    void refreshToken() throws Exception {
        // given
        String accessToken = "validAccessToken";
        String refreshToken = "validRefreshToken";
        TokenResponse newTokens = new TokenResponse("newAccessToken", "newRefreshToken", "Bearer", 3600000L);

        when(authService.refreshToken(eq(refreshToken))).thenReturn(newTokens);

        // when & then
        mockMvc.perform(post("/auth/refresh")
                .with(csrf())
                .header("Authorization", "Bearer " + accessToken)
                .param("refreshToken", refreshToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("newAccessToken"))
                .andExpect(jsonPath("$.refreshToken").value("newRefreshToken"));

        verify(authService, times(1)).refreshToken(eq(refreshToken));
    }

    @Test
    @DisplayName("토큰 갱신 실패 테스트 - 유효하지 않은 리프레시 토큰")
    void refreshTokenFailWithInvalidToken() throws Exception {
        // given
        String accessToken = "validAccessToken";
        String invalidRefreshToken = "invalidRefreshToken";

        when(authService.refreshToken(eq(invalidRefreshToken)))
                .thenThrow(new CustomExceptions.AuthenticationException("유효하지 않은 리프레시 토큰입니다."));

        // when & then
        mockMvc.perform(post("/auth/refresh")
                .with(csrf())
                .param("refreshToken", invalidRefreshToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("유효하지 않은 리프레시 토큰입니다."));

        verify(authService, times(1)).refreshToken(eq(invalidRefreshToken));
    }
}