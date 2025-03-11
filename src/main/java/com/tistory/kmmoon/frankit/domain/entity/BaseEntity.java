package com.tistory.kmmoon.frankit.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 공통 감사 필드를 포함한 기본 엔티티 클래스
 * - 생성자, 생성일시, 수정자, 수정일시 자동 관리
 */
@Getter
@Setter
@MappedSuperclass
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    /**
     * 생성자 (UserId)
     */
    @CreatedBy
    @Column(name = "created_by", nullable = false, updatable = false, length = 200)
    private Long createdBy;

    /**
     * 생성일시
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 최종 수정자 (UserId)
     */
    @LastModifiedBy
    @Column(name = "last_modified_by", length = 200)
    private Long lastModifiedBy;

    /**
     * 최종 수정일시
     */
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}