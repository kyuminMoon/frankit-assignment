package com.tistory.kmmoon.frankit.presentation.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tistory.kmmoon.frankit.domain.entity.User;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Long id;
    private String email;
    private String name;
    private User.Role role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    @JsonIgnore
    private String password; // 응답에 비밀번호 포함하지 않음

    public static UserResponse fromEntity(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}