package com.tistory.kmmoon.frankit.application.service;

import com.tistory.kmmoon.frankit.common.annotation.ReadOnlyTransactional;
import com.tistory.kmmoon.frankit.domain.entity.OptionValue;
import com.tistory.kmmoon.frankit.domain.entity.Product;
import com.tistory.kmmoon.frankit.domain.entity.ProductOption;
import com.tistory.kmmoon.frankit.domain.entity.User;
import com.tistory.kmmoon.frankit.domain.exception.CustomExceptions;
import com.tistory.kmmoon.frankit.domain.exception.ErrorCode;
import com.tistory.kmmoon.frankit.domain.repository.ProductRepository;
import com.tistory.kmmoon.frankit.infrastructure.aspect.DistributedLock;
import com.tistory.kmmoon.frankit.infrastructure.util.DistributedLockTemplate;
import com.tistory.kmmoon.frankit.presentation.dto.ProductDto;
import com.tistory.kmmoon.frankit.presentation.dto.ProductOptionDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    // 상수 분리하여 코드 명확성 개선
    private static final String PRODUCT_LOCK_PREFIX = "product:lock:";

    private final ProductRepository productRepository;
    private final DistributedLockTemplate lockTemplate; // 템플릿 기반 락은 여전히 필요하므로 유지

    /**
     * 상품 등록
     */
    @Transactional
    public ProductDto.Response createProduct(ProductDto.Request request) {
        log.info("상품 등록 요청: {}", request);

        Product product = request.toEntity();

        // 옵션 처리 로직을 별도 메서드로 추출하여 가독성 개선
        addOptionsToProduct(product, request);

        Product savedProduct = productRepository.save(product);
        log.info("상품 등록 완료: id={}", savedProduct.getId());

        return ProductDto.Response.fromEntity(savedProduct);
    }

    /**
     * 상품 목록 조회
     */
    @ReadOnlyTransactional
    public Page<ProductDto.ListResponse> getProducts(Pageable pageable) {
        log.info("상품 목록 조회 요청: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        return productRepository.findAll(pageable).map(ProductDto.ListResponse::fromEntity);
    }

    /**
     * 상품 상세 조회
     */
    @ReadOnlyTransactional
    public ProductDto.Response getProductById(Long productId) {
        log.info("상품 상세 조회 요청: id={}", productId);

        // 중복 코드 제거를 위해 추출한 메서드 사용
        Product product = findProductWithOptionsOrThrow(productId);

        return ProductDto.Response.fromEntity(product);
    }

    /**
     * 상품 수정 (어노테이션 기반 분산락 적용)
     */
    @Transactional
    @DistributedLock(key = "'" + PRODUCT_LOCK_PREFIX + "' + #productId", waitTime = 3000, leaseTime = 5000)
    public ProductDto.Response updateProduct(Long productId, ProductDto.Request request, User user) {
        log.info("상품 수정 요청: id={}, request={}", productId, request);

        // 중복 코드 제거를 위해 추출한 메서드 사용
        Product product = findProductWithOptionsOrThrow(productId);
        checkUserAuth(user, product);

        // 기본 정보 수정 로직을 별도 메서드로 추출하여 가독성 개선
        updateProductBasicInfo(product, request);

        // 옵션 업데이트 로직을 별도 메서드로 추출
        updateProductOptions(product, request);

        Product updatedProduct = productRepository.save(product);
        log.info("상품 수정 완료: id={}", updatedProduct.getId());

        return ProductDto.Response.fromEntity(updatedProduct);
    }

    /**
     * 상품 삭제
     * - 분산락 구현 방식의 일관성을 위해 템플릿 방식 유지
     */
    @Transactional
    public void deleteProduct(Long productId, User user) {
        log.info("상품 삭제 요청: id={}", productId);

        // 템플릿 기반 분산락 유지 (기존 코드와의 일관성 유지)
        lockTemplate.executeWithLock(
                PRODUCT_LOCK_PREFIX + productId,
                3000,
                5000,
                () -> {
                    Product product = findProductWithOptionsOrThrow(productId);
                    checkUserAuth(user, product);
                    productRepository.delete(product);
                    log.info("상품 삭제 완료: id={}", productId);
                    return null;
                }
        );
    }

    /**
     * 상품 재고 업데이트
     */
    @Transactional
    @DistributedLock(key = "'" + PRODUCT_LOCK_PREFIX + "'.concat(#productId.toString())")
    public ProductDto.Response updateProductStock(Long productId, int stockChange) {
        log.info("상품 재고 업데이트 요청: id={}, stockChange={}", productId, stockChange);

        // 중복 코드 제거를 위해 추출한 메서드 사용
        Product product = findProductWithOptionsOrThrow(productId);

        long newStock = product.getStock() + stockChange;
        if (newStock < 0) {
            log.warn("재고가 부족합니다. 현재 재고: {}", product.getStock());
            throw new CustomExceptions.CustomException(ErrorCode.LACK_OF_STOCK);
        }

        product.setStock(newStock);

        Product updatedProduct = productRepository.save(product);
        log.info("상품 재고 업데이트 완료: id={}, 새 재고: {}", updatedProduct.getId(), updatedProduct.getStock());

        return ProductDto.Response.fromEntity(updatedProduct);
    }

    /**
     * 상품 조회
     */
    private Product findProductWithOptionsOrThrow(Long productId) {
        return productRepository.findByIdWithOptionsAndValues(productId)
                .orElseThrow(() -> new CustomExceptions.ResourceNotFoundException("상품을 찾을 수 없습니다. id: " + productId));
    }

    /**
     * 사용자 권한 검증
     */
    private void checkUserAuth(User user, Product product) {
        if(!user.getRole().equals(User.Role.ADMIN)) {
            if(!user.getId().equals(product.getCreatedBy())) {
                throw new CustomExceptions.AuthorizationException("권한이 부족합니다.");
            }
        }
    }

    /**
     * 상품 기본 정보 업데이트
     */
    private void updateProductBasicInfo(Product product, ProductDto.Request request) {
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setShippingFee(request.getShippingFee());
        product.setStock(request.getStock());
    }

    /**
     * 상품에 옵션 추가
     */
    private void addOptionsToProduct(Product product, ProductDto.Request request) {
        if (request.getOptions() == null || request.getOptions().isEmpty()) {
            return;
        }

        for (var optionDto : request.getOptions()) {
            ProductOption option = optionDto.toEntity();
            product.addOption(option);

            // 옵션 값 추가 로직을 별도 메서드로 추출
            addOptionValuesToOption(option, optionDto);
        }
    }

    /**
     * 옵션에 옵션 값 추가
     */
    private void addOptionValuesToOption(ProductOption option, ProductOptionDto.Request optionDto) {
        if (option.getOptionType() != ProductOption.OptionType.SELECT ||
                optionDto.getOptionValues() == null ||
                optionDto.getOptionValues().isEmpty()) {
            return;
        }

        for (var valueDto : optionDto.getOptionValues()) {
            OptionValue optionValue = valueDto.toEntity();
            option.addOptionValue(optionValue);
        }
    }

    /**
     * 상품 옵션 업데이트
     */
    private void updateProductOptions(Product product, ProductDto.Request request) {
        // 기존 옵션 모두 제거
        product.getOptions().clear();

        // 새 옵션 추가
        addOptionsToProduct(product, request);
    }
}