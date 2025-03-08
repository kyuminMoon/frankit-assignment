package com.tistory.kmmoon.frankit.presentation.dto;

import com.tistory.kmmoon.frankit.domain.entity.Product;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ProductDto {

    @Getter
    @Builder
    @ToString
    @EqualsAndHashCode
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        @NotBlank(message = "상품명은 필수 입력 항목입니다.")
        private String name;

        private String description;

        @NotNull(message = "가격은 필수 입력 항목입니다.")
        @Positive(message = "가격은 양수여야 합니다.")
        private BigDecimal price;

        @NotNull(message = "배송비는 필수 입력 항목입니다.")
        @Positive(message = "배송비는 양수여야 합니다.")
        private BigDecimal shippingFee;

        @NotNull(message = "재고는 필수 입력 항목입니다.")
        @Positive(message = "재고는 양수여야 합니다.")
        private Long stock;

        @Valid
        @Size(max = 3, message = "옵션은 최대 3개까지 추가할 수 있습니다.")
        @Builder.Default
        private List<ProductOptionDto.Request> options = new ArrayList<>();

        public Product toEntity() {
            return Product.builder()
                    .name(name)
                    .description(description)
                    .price(price)
                    .stock(stock)
                    .shippingFee(shippingFee)
                    .build();
        }
    }

    @Getter
    @Builder
    @ToString
    @EqualsAndHashCode
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private String name;
        private String description;
        private BigDecimal price;
        private BigDecimal shippingFee;
        private Long stock;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        @Builder.Default
        private List<ProductOptionDto.Response> options = new ArrayList<>();

        public static Response fromEntity(Product product) {
            return Response.builder()
                    .id(product.getId())
                    .name(product.getName())
                    .description(product.getDescription())
                    .price(product.getPrice())
                    .shippingFee(product.getShippingFee())
                    .stock(product.getStock())
                    .createdAt(product.getCreatedAt())
                    .updatedAt(product.getUpdatedAt())
                    .options(product.getOptions().stream()
                            .map(ProductOptionDto.Response::fromEntity)
                            .collect(Collectors.toList()))
                    .build();
        }
    }

    @Getter
    @Builder
    @ToString
    @EqualsAndHashCode
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ListResponse {
        private Long id;
        private String name;
        private BigDecimal price;
        private Long stock;
        private BigDecimal shippingFee;
        private LocalDateTime createdAt;

        public static ListResponse fromEntity(Product product) {
            return ListResponse.builder()
                    .id(product.getId())
                    .name(product.getName())
                    .price(product.getPrice())
                    .shippingFee(product.getShippingFee())
                    .stock(product.getStock())
                    .createdAt(product.getCreatedAt())
                    .build();
        }
    }
}