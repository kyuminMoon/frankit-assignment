package com.tistory.kmmoon.frankit.application.service;

import com.tistory.kmmoon.frankit.domain.entity.User;
import com.tistory.kmmoon.frankit.domain.exception.CustomExceptions;
import com.tistory.kmmoon.frankit.infrastructure.config.security.JwtTokenProvider;
import com.tistory.kmmoon.frankit.domain.repository.UserRepository;
import com.tistory.kmmoon.frankit.presentation.dto.request.LoginRequest;
import com.tistory.kmmoon.frankit.presentation.dto.response.TokenResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public TokenResponse login(LoginRequest loginDto) {
        // 사용자 인증
        User user = userRepository.findByEmail(loginDto.getEmail())
                .orElseThrow(() -> new CustomExceptions.AuthenticationException("이메일 또는 비밀번호가 잘못되었습니다."));

        log.info("test pass : {}, {}", passwordEncoder.encode(loginDto.getPassword()), user.getPassword());
        if (!passwordEncoder.matches(loginDto.getPassword(), user.getPassword())) {
            throw new CustomExceptions.AuthenticationException("이메일 또는 비밀번호가 잘못되었습니다.");
        }

        // JWT 토큰 생성
        String accessToken = jwtTokenProvider.createAccessToken(user.getEmail());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getEmail());

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(3600000L) // 1시간 (밀리초)
                .build();
    }

    @Transactional
    public void logout(String email) {
        jwtTokenProvider.invalidateToken(email);
    }

    @Transactional
    public TokenResponse refreshToken(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new CustomExceptions.AuthenticationException("유효하지 않은 리프레시 토큰입니다.");
        }

        String email = jwtTokenProvider.getEmail(refreshToken);
        
        // 새 토큰 발급
        String newAccessToken = jwtTokenProvider.createAccessToken(email);
        String newRefreshToken = jwtTokenProvider.createRefreshToken(email);

        return TokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(3600000L) // 1시간 (밀리초)
                .build();
    }
}