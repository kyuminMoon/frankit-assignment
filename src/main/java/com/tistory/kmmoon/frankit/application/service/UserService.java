package com.tistory.kmmoon.frankit.application.service;

import com.tistory.kmmoon.frankit.common.annotation.ReadOnlyTransactional;
import com.tistory.kmmoon.frankit.domain.entity.User;
import com.tistory.kmmoon.frankit.domain.exception.CustomExceptions;
import com.tistory.kmmoon.frankit.domain.repository.UserRepository;
import com.tistory.kmmoon.frankit.presentation.dto.request.UserRequest;
import com.tistory.kmmoon.frankit.presentation.dto.response.UserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 사용자 가입
     */
    @Transactional
    public UserResponse registerUser(UserRequest request) {
        log.info("사용자 가입 요청: {}", request.getEmail());
        
        // 이메일 중복 검사
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 등록된 이메일입니다.");
        }
        
        // 사용자 엔티티 생성
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .role(User.Role.USER) // 기본 역할은 USER
                .build();
        
        User savedUser = userRepository.save(user);
        log.info("사용자 가입 완료: id={}", savedUser.getId());
        
        return UserResponse.fromEntity(savedUser);
    }
    
    /**
     * 사용자 조회
     */
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long userId) {
        log.info("사용자 조회 요청: id={}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomExceptions.ResourceNotFoundException("사용자를 찾을 수 없습니다. id: " + userId));
        
        return UserResponse.fromEntity(user);
    }
    
    /**
     * 사용자 이메일로 조회
     */
    @ReadOnlyTransactional
    public UserResponse getUserByEmail(String email) {
        log.info("사용자 조회 요청: email={}", email);
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomExceptions.ResourceNotFoundException("사용자를 찾을 수 없습니다. email: " + email));
        
        return UserResponse.fromEntity(user);
    }
    
    /**
     * 전체 사용자 목록 조회
     */
    @ReadOnlyTransactional
    public List<UserResponse> getAllUsers() {
        log.info("전체 사용자 목록 조회 요청");
        
        return userRepository.findAll().stream()
                .map(UserResponse::fromEntity)
                .collect(Collectors.toList());
    }
    
    /**
     * 사용자 정보 수정
     */
    @Transactional
    public UserResponse updateUser(Long userId, UserRequest request) {
        log.info("사용자 정보 수정 요청: id={}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomExceptions.ResourceNotFoundException("사용자를 찾을 수 없습니다. id: " + userId));
        
        // 이름 수정
        if (request.getName() != null) {
            user.setName(request.getName());
        }
        
        // 비밀번호 수정
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        
        User updatedUser = userRepository.save(user);
        log.info("사용자 정보 수정 완료: id={}", updatedUser.getId());
        
        return UserResponse.fromEntity(updatedUser);
    }
    
    /**
     * 사용자 삭제
     */
    @Transactional
    public void deleteUser(Long userId) {
        log.info("사용자 삭제 요청: id={}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomExceptions.ResourceNotFoundException("사용자를 찾을 수 없습니다. id: " + userId));
        
        userRepository.delete(user);
        log.info("사용자 삭제 완료: id={}", userId);
    }
}