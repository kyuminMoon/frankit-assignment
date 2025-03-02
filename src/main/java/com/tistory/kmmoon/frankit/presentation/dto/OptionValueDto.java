package com.tistory.kmmoon.frankit.presentation.dto;

import com.tistory.kmmoon.frankit.domain.entity.OptionValue;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class OptionValueDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        @NotBlank(message = "옵션 값은 필수 입력 항목입니다.")
        private String value;

        public OptionValue toEntity() {
            return OptionValue.builder()
                    .value(value)
                    .build();
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private String value;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public static Response fromEntity(OptionValue optionValue) {
            return Response.builder()
                    .id(optionValue.getId())
                    .value(optionValue.getValue())
                    .createdAt(optionValue.getCreatedAt())
                    .updatedAt(optionValue.getUpdatedAt())
                    .build();
        }
    }
}