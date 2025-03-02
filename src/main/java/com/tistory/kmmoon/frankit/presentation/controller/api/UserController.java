package com.tistory.kmmoon.frankit.presentation.controller.api;

import com.tistory.kmmoon.frankit.application.service.UserService;
import com.tistory.kmmoon.frankit.domain.entity.User;
import com.tistory.kmmoon.frankit.domain.exception.CustomExceptions;
import com.tistory.kmmoon.frankit.domain.exception.ErrorCode;
import com.tistory.kmmoon.frankit.presentation.dto.request.ModifyUserRequest;
import com.tistory.kmmoon.frankit.presentation.dto.request.UserRequest;
import com.tistory.kmmoon.frankit.presentation.dto.response.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
@Tag(name = "사용자 관리", description = "사용자 관리 관련 API")
public class UserController {

    private final UserService userService;

    /**
     * 사용자 등록
     *
     * @param UserRequest
     * @return ResponseEntity<UserResponse>
     */
    @PostMapping
    @PreAuthorize("permitAll")
    @Operation(summary = "사용자 등록", description = "새로운 사용자를 시스템에 등록합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "사용자 등록 성공",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "500", description = "이미 존재하는 이메일")
    })
    public ResponseEntity<UserResponse> registerUser(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "사용자 등록 정보", required = true,
                    content = @Content(schema = @Schema(implementation = UserRequest.class)))
            @Valid @RequestBody UserRequest request) {
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
    @Operation(summary = "사용자 정보 조회", description = "특정 사용자의 정보를 조회합니다. 관리자 또는 본인만 조회 가능합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "사용자 없음")
    })
    public ResponseEntity<UserResponse> getUserById(
            @Parameter(description = "조회할 사용자 ID", required = true)
            @PathVariable Long userId) {
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
    @PreAuthorize("#user.id == authentication.principal.id")
    @Operation(summary = "현재 사용자 정보 조회", description = "현재 로그인한 사용자의 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    public ResponseEntity<UserResponse> getCurrentUser(
            @Parameter(hidden = true)
            @AuthenticationPrincipal User user) {
        log.info("현재 사용자 정보 조회 API 호출");
        UserResponse response = userService.getUserByEmail(user.getEmail());
        return ResponseEntity.ok(response);
    }

    /**
     * 본인 정보 수정
     *
     * @param ModifyUserRequest
     * @return ResponseEntity<UserResponse>
     */
    @PutMapping("/me")
    @PreAuthorize("#user.id == authentication.principal.id")
    @Operation(summary = "본인 정보 수정", description = "사용자의 본인 정보를 수정합니다. 본인만 수정 가능합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "수정 성공",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "사용자 없음")
    })
    public ResponseEntity<UserResponse> updateUser(
            @AuthenticationPrincipal User user,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "수정할 사용자 정보", required = true,
                    content = @Content(schema = @Schema(implementation = ModifyUserRequest.class)))
            @RequestBody ModifyUserRequest request) {
        log.info("본인 정보 수정 API 호출, id: {}", user.getId());
        if(!request.valid()) throw new CustomExceptions.CustomException(ErrorCode.INPUT_VALUE_ERROR);

        UserResponse response = userService.updateUser(user.getId(), request);
        return ResponseEntity.ok(response);
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
    @Operation(summary = "사용자 정보 수정", description = "특정 사용자의 정보를 수정합니다. 관리자 또는 본인만 수정 가능합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "수정 성공",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "사용자 없음")
    })
    public ResponseEntity<UserResponse> updateUser(
            @Parameter(description = "수정할 사용자 ID", required = true)
            @PathVariable Long userId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "수정할 사용자 정보", required = true,
                    content = @Content(schema = @Schema(implementation = UserRequest.class)))
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
    @Operation(summary = "사용자 삭제", description = "특정 사용자를 삭제합니다. 관리자 또는 본인만 삭제 가능합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "사용자 없음")
    })
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "삭제할 사용자 ID", required = true)
            @PathVariable Long userId) {
        log.info("사용자 삭제 API 호출, id: {}", userId);
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/add-role")
    @Operation(summary = "USER에게 ADMIN Role 추가 API", description = "특정 사용자의 역할을 변경합니다. (간편 테스트용 API)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "역할 변경 성공"),
            @ApiResponse(responseCode = "404", description = "사용자 없음")
    })
    public ResponseEntity<UserResponse> updateUserRole(
            @AuthenticationPrincipal User user
    ) {
        User.Role newRole = User.Role.ADMIN;

        UserResponse response = userService.updateUserRole(user.getId(), newRole);
        return ResponseEntity.ok(response);
    }
}
