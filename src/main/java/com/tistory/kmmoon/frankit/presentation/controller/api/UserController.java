package com.tistory.kmmoon.frankit.presentation.controller.api;

import com.tistory.kmmoon.frankit.application.service.UserService;
import com.tistory.kmmoon.frankit.domain.entity.User;
import com.tistory.kmmoon.frankit.presentation.dto.request.UserRequest;
import com.tistory.kmmoon.frankit.presentation.dto.response.UserResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    /**
     * 사용자 등록
     *
     * @param UserRequest
     * @return ResponseEntity<UserResponse>
     */
    @PostMapping
    public ResponseEntity<UserResponse> registerUser(@Valid @RequestBody UserRequest request) {
        log.info("사용자 등록 API 호출");
        UserResponse response = userService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 사용자 정보 조회
     *
     * @param userId
     * @return ResponseEntity<UserResponse>
     */
    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long userId) {
        log.info("사용자 정보 조회 API 호출, id: {}", userId);
        UserResponse response = userService.getUserById(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * 현재 로그인한 사용자 정보 조회
     *
     * @return ResponseEntity<UserResponse>
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal User user) {
        log.info("현재 사용자 정보 조회 API 호출");
        UserResponse response = userService.getUserByEmail(user.getEmail());
        return ResponseEntity.ok(response);
    }

    /**
     * 전체 사용자 목록 조회 (관리자 전용)
     *
     * @return ResponseEntity<List<UserResponse>>
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        log.info("전체 사용자 목록 조회 API 호출");
        List<UserResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * 사용자 정보 수정
     *
     * @param userId
     * @param UserRequest
     * @return ResponseEntity<UserResponse>
     */
    @PutMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long userId, 
            @Valid @RequestBody UserRequest request) {
        log.info("사용자 정보 수정 API 호출, id: {}", userId);
        UserResponse response = userService.updateUser(userId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 사용자 삭제
     *
     * @param userId
     * @return ResponseEntity<Void>
     */
    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        log.info("사용자 삭제 API 호출, id: {}", userId);
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
}