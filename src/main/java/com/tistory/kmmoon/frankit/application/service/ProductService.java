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

    private final ProductRepository productRepository;
    private final DistributedLockTemplate lockTemplate;

    private static final String PRODUCT_LOCK_PREFIX = "product:lock:";

    /**
     * 상품 등록
     */
    @Transactional
    public ProductDto.Response createProduct(ProductDto.Request request) {
        log.info("상품 등록 요청: {}", request);

        Product product = request.toEntity();

        // 옵션 추가
        if (request.getOptions() != null && !request.getOptions().isEmpty()) {
            for (var optionDto : request.getOptions()) {
                ProductOption option = optionDto.toEntity();
                product.addOption(option);

                // 옵션 값 추가 (선택 타입인 경우)
                if (option.getOptionType() == ProductOption.OptionType.SELECT) {
                    if (optionDto.getOptionValues() != null && !optionDto.getOptionValues().isEmpty()) {
                        for (var valueDto : optionDto.getOptionValues()) {
                            OptionValue optionValue = valueDto.toEntity();
                            option.addOptionValue(optionValue);
                        }
                    }
                }
            }
        }

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

        Page<Product> products = productRepository.findAll(pageable);

        return products.map(ProductDto.ListResponse::fromEntity);
    }

    /**
     * 상품 상세 조회
     */
    @ReadOnlyTransactional
    public ProductDto.Response getProductById(Long productId) {
        log.info("상품 상세 조회 요청: id={}", productId);

        Product product = productRepository.findByIdWithOptionsAndValues(productId)
                .orElseThrow(() -> new CustomExceptions.ResourceNotFoundException("상품을 찾을 수 없습니다. id: " + productId));

        return ProductDto.Response.fromEntity(product);
    }

    /**
     * 상품 수정 (어노테이션 기반 분산락 적용)
     */
    @Transactional
    @DistributedLock(key = "'" + PRODUCT_LOCK_PREFIX + "' + #productId", waitTime = 3000, leaseTime = 5000)
    public ProductDto.Response updateProduct(Long productId, ProductDto.Request request, User user) {
        log.info("상품 수정 요청: id={}, request={}", productId, request);

        Product product = productRepository.findByIdWithOptionsAndValues(productId)
                .orElseThrow(() -> new CustomExceptions.ResourceNotFoundException("상품을 찾을 수 없습니다. id: " + productId));

        userAuthChk(user, product);

        // 기본 정보 수정
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setShippingFee(request.getShippingFee());
        product.setStock(request.getStock());

        // 옵션 정보 업데이트 (기존 옵션 모두 제거 후 새로 추가)
        product.getOptions().clear();

        if (request.getOptions() != null && !request.getOptions().isEmpty()) {
            for (var optionDto : request.getOptions()) {
                ProductOption option = optionDto.toEntity();
                product.addOption(option);

                // 옵션 값 추가 (선택 타입인 경우)
                if (option.getOptionType() == ProductOption.OptionType.SELECT) {
                    if (optionDto.getOptionValues() != null && !optionDto.getOptionValues().isEmpty()) {
                        for (var valueDto : optionDto.getOptionValues()) {
                            OptionValue optionValue = valueDto.toEntity();
                            option.addOptionValue(optionValue);
                        }
                    }
                }
            }
        }

        Product updatedProduct = productRepository.save(product);
        log.info("상품 수정 완료: id={}", updatedProduct.getId());

        return ProductDto.Response.fromEntity(updatedProduct);
    }

    /**
     * 상품 삭제 (템플릿 기반 분산락 적용)
     */
    @Transactional
    public void deleteProduct(Long productId, User user) {
        log.info("상품 삭제 요청: id={}", productId);

        // 템플릿을 사용한 분산락 적용
        lockTemplate.executeWithLock(
            PRODUCT_LOCK_PREFIX + productId,
            3000, // 대기 시간 3초
            5000, // 유지 시간 5초
            () -> {
                Product product = productRepository.findByIdWithOptionsAndValues(productId)
                        .orElseThrow(() -> new CustomExceptions.ResourceNotFoundException("상품을 찾을 수 없습니다. id: " + productId));
                userAuthChk(user, product);
                productRepository.delete(product);
                log.info("상품 삭제 완료: id={}", productId);
                return null;
            }
        );
    }

    /**
     * 상품 재고 업데이트 (재고가 없다면 주문이 불가능해야 하므로 레이스 컨디션 이슈를 제거하기 위해 분산 락 사용)
     */
    @Transactional
    @DistributedLock(key = "'" + PRODUCT_LOCK_PREFIX + "'.concat(#productId.toString())")
    public ProductDto.Response updateProductStock(Long productId, int stockChange) {
        log.info("상품 재고 업데이트 요청: id={}, stockChange={}", productId, stockChange);

        Product product = productRepository.findByIdWithOptionsAndValues(productId)
                .orElseThrow(() -> new CustomExceptions.ResourceNotFoundException("상품을 찾을 수 없습니다. id: " + productId));

        long newStock = product.getStock() + stockChange;
        if (newStock < 0) {
            log.warn("재고가 부족합니다. 현재 재고: {}", product.getStock());
            throw new CustomExceptions.CustomException(ErrorCode.LACK_OF_STOCK);
        }

        product.setStock(product.getStock() + stockChange);

        Product updatedProduct = productRepository.save(product);
        log.info("상품 재고 업데이트 완료: id={}", updatedProduct.getId());

        return ProductDto.Response.fromEntity(updatedProduct);
    }

    private void userAuthChk(User user, Product product) {
        if(!user.getRole().equals(User.Role.ADMIN)) {
            if(!user.getId().equals(product.getCreatedBy())) {
                throw new CustomExceptions.AuthorizationException("권한이 부족합니다.");
            }
        }
    }
}