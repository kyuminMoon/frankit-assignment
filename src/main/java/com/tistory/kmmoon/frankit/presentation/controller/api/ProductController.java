package com.tistory.kmmoon.frankit.presentation.controller.api;

import com.tistory.kmmoon.frankit.application.service.ProductService;
import com.tistory.kmmoon.frankit.domain.entity.User;
import com.tistory.kmmoon.frankit.presentation.dto.ProductDto;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/products")
@Tag(name = "상품 관리", description = "상품 관리 관련 API")
public class ProductController {

    private final ProductService productService;

    /**
     * 상품 등록
     *
     * @param ProductDto.Request
     * @return ResponseEntity<ProductDto.Response>
     */
    @PostMapping
    @Operation(summary = "상품 등록", description = "새로운 상품을 등록합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "상품 등록 성공",
                    content = @Content(schema = @Schema(implementation = ProductDto.Response.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    public ResponseEntity<ProductDto.Response> createProduct(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "상품 등록 정보", required = true,
                    content = @Content(schema = @Schema(implementation = ProductDto.Request.class)))
            @Valid @RequestBody ProductDto.Request request) {
        log.info("상품 등록 API 호출");
        ProductDto.Response response = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 상품 목록 조회 (페이징)
     *
     * @param pageable
     * @return ResponseEntity<Page<ProductDto.ListResponse>>
     */
    @GetMapping
    @Operation(summary = "상품 목록 조회", description = "상품 목록을 페이징하여 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    public ResponseEntity<Page<ProductDto.ListResponse>> getProducts(
            @Parameter(description = "페이지 정보 (크기, 정렬 등)")
            @PageableDefault(size = 10, sort = "createdAt,desc") Pageable pageable) {
        log.info("상품 목록 조회 API 호출");
        Page<ProductDto.ListResponse> products = productService.getProducts(pageable);
        return ResponseEntity.ok(products);
    }

    /**
     * 상품 상세 조회
     *
     * @param productId
     * @return ResponseEntity<ProductDto.Response>
     */
    @GetMapping("/{productId}")
    @Operation(summary = "상품 상세 조회", description = "특정 상품의 상세 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = ProductDto.Response.class))),
            @ApiResponse(responseCode = "404", description = "상품 없음")
    })
    public ResponseEntity<ProductDto.Response> getProductById(
            @Parameter(description = "조회할 상품 ID", required = true)
            @PathVariable Long productId) {
        log.info("상품 상세 조회 API 호출, id: {}", productId);
        ProductDto.Response product = productService.getProductById(productId);
        return ResponseEntity.ok(product);
    }

    /**
     * 상품 수정
     *
     * @param productId
     * @param ProductDto.Request
     * @return ResponseEntity<ProductDto.Response>
     */
    @PutMapping("/{productId}")
    @Operation(summary = "상품 수정", description = "특정 상품의 정보를 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "수정 성공",
                    content = @Content(schema = @Schema(implementation = ProductDto.Response.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "404", description = "상품 없음")
    })
    public ResponseEntity<ProductDto.Response> updateProduct(
            @Parameter(description = "수정할 상품 ID", required = true)
            @PathVariable Long productId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "수정할 상품 정보", required = true,
                    content = @Content(schema = @Schema(implementation = ProductDto.Request.class)))
            @Valid @RequestBody ProductDto.Request request,
            @AuthenticationPrincipal User user) {
        log.info("상품 수정 API 호출, id: {}", productId);
        ProductDto.Response response = productService.updateProduct(productId, request, user);
        return ResponseEntity.ok(response);
    }

    /**
     * 상품 삭제
     *
     * @param productId
     * @return ResponseEntity<Void>
     */
    @DeleteMapping("/{productId}")
    @Operation(summary = "상품 삭제", description = "특정 상품을 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "404", description = "상품 없음")
    })
    public ResponseEntity<Void> deleteProduct(
            @Parameter(description = "삭제할 상품 ID", required = true)
            @PathVariable Long productId,
            @AuthenticationPrincipal User user) {
        log.info("상품 삭제 API 호출, id: {}", productId);
        productService.deleteProduct(productId, user);
        return ResponseEntity.noContent().build();
    }
}