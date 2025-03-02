package com.tistory.kmmoon.frankit.infrastructure.config.security;

import com.tistory.kmmoon.frankit.domain.entity.User;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * JPA Auditing 설정
 * - 엔티티의 생성자, 수정자, 생성일시, 수정일시를 자동으로 관리
 */
@Configuration
@EnableJpaAuditing
public class AuditConfig {

    /**
     * 현재 인증된 사용자 정보를 제공하는 AuditorAware 구현체
     * - Spring Security의 인증 정보에서 현재 사용자 이메일을 추출
     */
    @Bean
    public AuditorAware<Long> auditorProvider() {
        return () -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated()) {
                return Optional.empty();
            }

            Object principal = authentication.getPrincipal();

            if (principal instanceof User userDetails) {
                return Optional.of(userDetails.getId());
            }

            return Optional.empty();
        };
    }
}