package com.tistory.kmmoon.frankit.presentation.controller.api;

import com.tistory.kmmoon.frankit.application.service.UserService;
import com.tistory.kmmoon.frankit.presentation.dto.request.UserRequest;
import com.tistory.kmmoon.frankit.presentation.dto.response.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;

    /**
     * 전체 사용자 목록 조회 (관리자 전용)
     *
     * @return ResponseEntity<List<UserResponse>>
     */
    @GetMapping("/users")
    @Operation(summary = "전체 사용자 목록 조회", description = "시스템의 모든 사용자 목록을 조회합니다. 관리자만 접근 가능합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        log.info("전체 사용자 목록 조회 API 호출");
        List<UserResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * 사용자 등록
     *
     * @param UserRequest
     * @return ResponseEntity<UserResponse>
     */
    @PostMapping("/users")
    @Operation(summary = "사용자 등록", description = "새로운 관리자를 시스템에 등록합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "관리자 등록 성공",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "500", description = "이미 존재하는 이메일")
    })
    public ResponseEntity<UserResponse> registerUser(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "사용자 등록 정보", required = true,
                    content = @Content(schema = @Schema(implementation = UserRequest.class)))
            @Valid @RequestBody UserRequest request) {
        log.info("관리자 등록 API 호출");
        UserResponse response = userService.registerAdmin(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
