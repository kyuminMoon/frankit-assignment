package com.tistory.kmmoon.frankit.application.service;

import com.tistory.kmmoon.frankit.domain.entity.User;
import com.tistory.kmmoon.frankit.domain.exception.CustomExceptions;
import com.tistory.kmmoon.frankit.domain.repository.UserRepository;
import com.tistory.kmmoon.frankit.presentation.dto.request.UserRequest;
import com.tistory.kmmoon.frankit.presentation.dto.response.UserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UserRequest userRequest;

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
        
        // 테스트 요청 설정
        userRequest = UserRequest.builder()
                .email("test@example.com")
                .password("password123")
                .name("Test User")
                .build();
    }

    @Test
    @DisplayName("사용자 가입 성공 테스트")
    void registerUser_Success() {
        // given
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        
        // when
        UserResponse response = userService.registerUser(userRequest);
        
        // then
        assertNotNull(response);
        assertEquals(testUser.getId(), response.getId());
        assertEquals(testUser.getEmail(), response.getEmail());
        assertEquals(testUser.getName(), response.getName());
        
        verify(userRepository, times(1)).existsByEmail("test@example.com");
        verify(passwordEncoder, times(1)).encode("password123");
        verify(userRepository, times(1)).save(any(User.class));
    }
    
    @Test
    @DisplayName("사용자 가입 실패 테스트 - 이메일 중복")
    void registerUser_Fail_DuplicateEmail() {
        // given
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);
        
        // when & then
        assertThrows(IllegalArgumentException.class, () -> {
            userService.registerUser(userRequest);
        });
        
        verify(userRepository, times(1)).existsByEmail("test@example.com");
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }
    
    @Test
    @DisplayName("사용자 ID로 조회 성공 테스트")
    void getUserById_Success() {
        // given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        
        // when
        UserResponse response = userService.getUserById(1L);
        
        // then
        assertNotNull(response);
        assertEquals(testUser.getId(), response.getId());
        assertEquals(testUser.getEmail(), response.getEmail());
        assertEquals(testUser.getName(), response.getName());
        
        verify(userRepository, times(1)).findById(1L);
    }
    
    @Test
    @DisplayName("사용자 ID로 조회 실패 테스트 - 사용자 없음")
    void getUserById_Fail_UserNotFound() {
        // given
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        
        // when & then
        assertThrows(CustomExceptions.ResourceNotFoundException.class, () -> {
            userService.getUserById(99L);
        });
        
        verify(userRepository, times(1)).findById(99L);
    }
    
    @Test
    @DisplayName("사용자 이메일로 조회 성공 테스트")
    void getUserByEmail_Success() {
        // given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        
        // when
        UserResponse response = userService.getUserByEmail("test@example.com");
        
        // then
        assertNotNull(response);
        assertEquals(testUser.getId(), response.getId());
        assertEquals(testUser.getEmail(), response.getEmail());
        assertEquals(testUser.getName(), response.getName());
        
        verify(userRepository, times(1)).findByEmail("test@example.com");
    }
    
    @Test
    @DisplayName("사용자 이메일로 조회 실패 테스트 - 사용자 없음")
    void getUserByEmail_Fail_UserNotFound() {
        // given
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());
        
        // when & then
        assertThrows(CustomExceptions.ResourceNotFoundException.class, () -> {
            userService.getUserByEmail("nonexistent@example.com");
        });
        
        verify(userRepository, times(1)).findByEmail("nonexistent@example.com");
    }
    
    @Test
    @DisplayName("전체 사용자 목록 조회 테스트")
    void getAllUsers_Success() {
        // given
        User user2 = User.builder()
                .id(2L)
                .email("user2@example.com")
                .password("encodedPassword2")
                .name("User Two")
                .role(User.Role.USER)
                .build();
        
        when(userRepository.findAll()).thenReturn(Arrays.asList(testUser, user2));
        
        // when
        List<UserResponse> responses = userService.getAllUsers();
        
        // then
        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertEquals(testUser.getId(), responses.get(0).getId());
        assertEquals(user2.getId(), responses.get(1).getId());
        
        verify(userRepository, times(1)).findAll();
    }
    
    @Test
    @DisplayName("사용자 정보 수정 성공 테스트")
    void updateUser_Success() {
        // given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        
        UserRequest updateRequest = UserRequest.builder()
                .name("Updated Name")
                .password("newPassword")
                .build();
        
        when(passwordEncoder.encode("newPassword")).thenReturn("newEncodedPassword");
        
        User updatedUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .password("newEncodedPassword")
                .name("Updated Name")
                .role(User.Role.USER)
                .build();
        
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);
        
        // when
        UserResponse response = userService.updateUser(1L, updateRequest);
        
        // then
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("test@example.com", response.getEmail());
        assertEquals("Updated Name", response.getName());
        
        verify(userRepository, times(1)).findById(1L);
        verify(passwordEncoder, times(1)).encode("newPassword");
        verify(userRepository, times(1)).save(any(User.class));
    }
    
    @Test
    @DisplayName("사용자 정보 수정 실패 테스트 - 사용자 없음")
    void updateUser_Fail_UserNotFound() {
        // given
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        
        // when & then
        assertThrows(CustomExceptions.ResourceNotFoundException.class, () -> {
            userService.updateUser(99L, userRequest);
        });
        
        verify(userRepository, times(1)).findById(99L);
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }
    
    @Test
    @DisplayName("사용자 삭제 성공 테스트")
    void deleteUser_Success() {
        // given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        doNothing().when(userRepository).delete(testUser);
        
        // when
        userService.deleteUser(1L);
        
        // then
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).delete(testUser);
    }
    
    @Test
    @DisplayName("사용자 삭제 실패 테스트 - 사용자 없음")
    void deleteUser_Fail_UserNotFound() {
        // given
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        
        // when & then
        assertThrows(CustomExceptions.ResourceNotFoundException.class, () -> {
            userService.deleteUser(99L);
        });
        
        verify(userRepository, times(1)).findById(99L);
        verify(userRepository, never()).delete(any(User.class));
    }
}