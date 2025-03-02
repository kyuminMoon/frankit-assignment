package com.tistory.kmmoon.frankit.presentation.dto;

import com.tistory.kmmoon.frankit.domain.entity.ProductOption;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ProductOptionDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        @NotBlank(message = "옵션명은 필수 입력 항목입니다.")
        private String name;

        @NotNull(message = "옵션 타입은 필수 입력 항목입니다.")
        private ProductOption.OptionType optionType;

        @NotNull(message = "추가 금액은 필수 입력 항목입니다.")
        private BigDecimal additionalPrice;

        @Valid
        @Builder.Default
        private List<OptionValueDto.Request> optionValues = new ArrayList<>();

        public ProductOption toEntity() {
            return ProductOption.builder()
                    .name(name)
                    .optionType(optionType)
                    .additionalPrice(additionalPrice)
                    .build();
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private String name;
        private ProductOption.OptionType optionType;
        private BigDecimal additionalPrice;
        @Builder.Default
        private List<OptionValueDto.Response> optionValues = new ArrayList<>();
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public static Response fromEntity(ProductOption option) {
            return Response.builder()
                    .id(option.getId())
                    .name(option.getName())
                    .optionType(option.getOptionType())
                    .additionalPrice(option.getAdditionalPrice())
                    .optionValues(option.getOptionValues().stream()
                            .map(OptionValueDto.Response::fromEntity)
                            .collect(Collectors.toList()))
                    .createdAt(option.getCreatedAt())
                    .updatedAt(option.getUpdatedAt())
                    .build();
        }
    }
}