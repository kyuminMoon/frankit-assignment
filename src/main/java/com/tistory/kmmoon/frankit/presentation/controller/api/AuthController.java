package com.tistory.kmmoon.frankit.presentation.controller.api;

import com.tistory.kmmoon.frankit.application.service.AuthService;
import com.tistory.kmmoon.frankit.presentation.dto.request.LoginRequest;
import com.tistory.kmmoon.frankit.presentation.dto.response.TokenResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    /**
     * 로그인
     *
     * @param loginDto
     * @return ResponseEntity<TokenResponse>
     */
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest loginDto) {
        log.info("로그인 요청: {}", loginDto.getEmail());
        TokenResponse response = authService.login(loginDto);
        return ResponseEntity.ok(response);
    }

    /**
     * 로그아웃
     *
     * @return the response entity
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails userDetails) {
            log.info("로그아웃 요청: {}", userDetails.getUsername());
            authService.logout(userDetails.getUsername());
        }
        return ResponseEntity.ok().build();
    }

    /**
     * 토큰 갱신
     *
     * @param refreshToken
     * @return ResponseEntity<TokenResponse>
     */
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refreshToken(@RequestParam String refreshToken) {
        log.info("토큰 갱신 요청");
        TokenResponse response = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(response);
    }
}