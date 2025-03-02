package com.tistory.kmmoon.frankit.application.service;

import com.tistory.kmmoon.frankit.domain.entity.OptionValue;
import com.tistory.kmmoon.frankit.domain.entity.Product;
import com.tistory.kmmoon.frankit.domain.entity.ProductOption;
import com.tistory.kmmoon.frankit.domain.entity.User;
import com.tistory.kmmoon.frankit.domain.exception.CustomExceptions;
import com.tistory.kmmoon.frankit.domain.repository.ProductRepository;
import com.tistory.kmmoon.frankit.infrastructure.util.DistributedLockTemplate;
import com.tistory.kmmoon.frankit.presentation.dto.OptionValueDto;
import com.tistory.kmmoon.frankit.presentation.dto.ProductDto;
import com.tistory.kmmoon.frankit.presentation.dto.ProductOptionDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private DistributedLockTemplate lockTemplate;

    @InjectMocks
    private ProductService productService;

    private ProductDto.Request productRequest;
    private Product product;
    private ProductOption productOption;
    private OptionValue optionValue;
    private User user;

    @BeforeEach
    void setUp() {
        // 테스트용 옵션값 DTO 생성
        List<OptionValueDto.Request> optionValueList = new ArrayList<>();
        OptionValueDto.Request optionValueRequest = OptionValueDto.Request.builder()
                .value("빨강")
                .build();
        optionValueList.add(optionValueRequest);

        // 테스트용 옵션 DTO 생성
        List<ProductOptionDto.Request> optionList = new ArrayList<>();
        ProductOptionDto.Request optionRequest = ProductOptionDto.Request.builder()
                .name("색상")
                .optionType(ProductOption.OptionType.SELECT)
                .additionalPrice(new BigDecimal("1000"))
                .optionValues(optionValueList)
                .build();
        optionList.add(optionRequest);

        // 테스트용 상품 DTO 생성
        productRequest = ProductDto.Request.builder()
                .name("테스트 상품")
                .description("테스트 상품 설명")
                .price(new BigDecimal("10000"))
                .shippingFee(new BigDecimal("2500"))
                .options(optionList)
                .build();

        // 테스트용 옵션값 엔티티 생성
        optionValue = OptionValue.builder()
                .id(1L)
                .value("빨강")
                .build();
        optionValue.setCreatedBy(1L);
        optionValue.setCreatedAt(LocalDateTime.now());

        // 테스트용 옵션 엔티티 생성 - ArrayList 명시적 초기화
        Set<OptionValue> optionValues = new HashSet<>();
        productOption = ProductOption.builder()
                .id(1L)
                .name("색상")
                .optionType(ProductOption.OptionType.SELECT)
                .additionalPrice(new BigDecimal("1000"))
                .optionValues(optionValues)
                .build();
        productOption.setCreatedBy(1L);
        productOption.setCreatedAt(LocalDateTime.now());

        productOption.getOptionValues().add(optionValue);
        // 양방향 관계 설정
        optionValue.setProductOption(productOption);

        // 테스트용 상품 엔티티 생성 - ArrayList 명시적 초기화
        Set<ProductOption> options = new HashSet<>();
        product = Product.builder()
                .id(1L)
                .name("테스트 상품")
                .description("테스트 상품 설명")
                .price(new BigDecimal("10000"))
                .shippingFee(new BigDecimal("2500"))
                .stock(10L) // stock 필드 추가
                .options(options)
                .build();
        product.setCreatedBy(1L);
        product.setCreatedAt(LocalDateTime.now());

        product.getOptions().add(productOption);
        // 양방향 관계 설정
        productOption.setProduct(product);
        user = User.builder()
                .id(1L)
                .email("email@test.com")
                .role(User.Role.USER)
                .build();
    }

    @Test
    @DisplayName("상품 등록 성공 테스트")
    void createProduct_Success() {
        // given
        // Mock 저장 시 입력된 Product 객체를 그대로 반환하도록 설정
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product savedProduct = invocation.getArgument(0);
            savedProduct.setId(1L); // ID 설정
            // 날짜 설정 필요한 경우 추가
            return savedProduct;
        });

        // when
        ProductDto.Response response = productService.createProduct(productRequest);

        // then
        assertNotNull(response);
        assertEquals("테스트 상품", response.getName());
        assertEquals(1, response.getOptions().size());
        assertEquals("색상", response.getOptions().get(0).getName());
        assertEquals(ProductOption.OptionType.SELECT, response.getOptions().get(0).getOptionType());
        assertEquals(1, response.getOptions().get(0).getOptionValues().size());
        assertEquals("빨강", response.getOptions().get(0).getOptionValues().get(0).getValue());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("상품 목록 조회 성공 테스트")
    void getProducts_Success() {
        // given
        List<Product> productList = List.of(product);
        Page<Product> productPage = new PageImpl<>(productList);
        Pageable pageable = PageRequest.of(0, 10);

        when(productRepository.findAll(pageable)).thenReturn(productPage);

        // when
        Page<ProductDto.ListResponse> response = productService.getProducts(pageable);

        // then
        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
        assertEquals(product.getId(), response.getContent().get(0).getId());
        verify(productRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("상품 상세 조회 성공 테스트")
    void getProductById_Success() {
        // given
        when(productRepository.findByIdWithOptionsAndValues(1L)).thenReturn(Optional.of(product));

        // when
        ProductDto.Response response = productService.getProductById(1L);

        // then
        assertNotNull(response);
        assertEquals(product.getId(), response.getId());
        assertEquals(product.getName(), response.getName());
        assertEquals(product.getOptions().size(), response.getOptions().size());
        verify(productRepository, times(1)).findByIdWithOptionsAndValues(1L);
    }

    @Test
    @DisplayName("상품 상세 조회 실패 테스트 - 상품 없음")
    void getProductById_Fail_NotFound() {
        // given
        when(productRepository.findByIdWithOptionsAndValues(99L)).thenReturn(Optional.empty());

        // when & then
        assertThrows(CustomExceptions.ResourceNotFoundException.class, () -> {
            productService.getProductById(99L);
        });
        verify(productRepository, times(1)).findByIdWithOptionsAndValues(99L);
    }

    @Test
    @DisplayName("상품 수정 성공 테스트")
    void updateProduct_Success() {
        // given
        when(productRepository.findByIdWithOptionsAndValues(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        ProductDto.Request updateRequest = ProductDto.Request.builder()
                .name("수정된 상품")
                .description("수정된 설명")
                .price(new BigDecimal("15000"))
                .shippingFee(new BigDecimal("3000"))
                .options(new ArrayList<>())
                .build();

        // when
        ProductDto.Response response = productService.updateProduct(1L, updateRequest, user);

        // then
        assertNotNull(response);
        assertEquals(product.getId(), response.getId());
        verify(productRepository, times(1)).findByIdWithOptionsAndValues(1L);
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("상품 수정 실패 테스트 - 상품 없음")
    void updateProduct_Fail_NotFound() {
        // given
        when(productRepository.findByIdWithOptionsAndValues(99L)).thenReturn(Optional.empty());

        // when & then
        assertThrows(CustomExceptions.ResourceNotFoundException.class, () -> {
            productService.updateProduct(99L, productRequest, user);
        });
        verify(productRepository, times(1)).findByIdWithOptionsAndValues(99L);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("상품 삭제 성공 테스트")
    void deleteProduct_Success() {
        // given
        when(productRepository.findByIdWithOptionsAndValues(1L)).thenReturn(Optional.of(product));
        doNothing().when(productRepository).delete(any(Product.class));

        // DistributedLockTemplate mock 설정
        doAnswer(invocation -> {
            Supplier<?> supplier = invocation.getArgument(3);
            return supplier.get();
        }).when(lockTemplate).executeWithLock(anyString(), anyLong(), anyLong(), any(Supplier.class));

        // when
        productService.deleteProduct(1L, user);

        // then
        verify(lockTemplate, times(1)).executeWithLock(anyString(), anyLong(), anyLong(), any(Supplier.class));
        verify(productRepository, times(1)).delete(any(Product.class));
    }

    @Test
    @DisplayName("상품 삭제 실패 테스트 - 상품 없음")
    void deleteProduct_Fail_NotFound() {
        // given
        when(productRepository.findByIdWithOptionsAndValues(99L)).thenReturn(Optional.empty());

        // DistributedLockTemplate mock 설정
        doAnswer(invocation -> {
            Supplier<?> supplier = invocation.getArgument(3);
            assertThrows(CustomExceptions.ResourceNotFoundException.class, supplier::get);
            return null;
        }).when(lockTemplate).executeWithLock(anyString(), anyLong(), anyLong(), any(Supplier.class));

        // when
        productService.deleteProduct(99L, user);

        // then
        verify(lockTemplate, times(1)).executeWithLock(anyString(), anyLong(), anyLong(), any(Supplier.class));
    }

    @Test
    @DisplayName("상품 재고 업데이트 성공 테스트 - 재고 증가")
    void updateProductStock_Increase_Success() {
        // given
        when(productRepository.findByIdWithOptionsAndValues(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        // when
        ProductDto.Response response = productService.updateProductStock(1L, 5);

        // then
        assertNotNull(response);
        assertEquals(15, product.getStock()); // 기존 10 + 5 = 15
        verify(productRepository, times(1)).findByIdWithOptionsAndValues(1L);
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("상품 재고 업데이트 성공 테스트 - 재고 감소")
    void updateProductStock_Decrease_Success() {
        // given
        when(productRepository.findByIdWithOptionsAndValues(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        // when
        ProductDto.Response response = productService.updateProductStock(1L, -5);

        // then
        assertNotNull(response);
        assertEquals(5, product.getStock()); // 기존 10 - 5 = 5
        verify(productRepository, times(1)).findByIdWithOptionsAndValues(1L);
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("상품 재고 업데이트 실패 테스트 - 재고 부족")
    void updateProductStock_Fail_InsufficientStock() {
        // given
        when(productRepository.findByIdWithOptionsAndValues(1L)).thenReturn(Optional.of(product));

        // when & then
        assertThrows(CustomExceptions.CustomException.class, () -> {
            productService.updateProductStock(1L, -15); // 기존 10 - 15 = -5 (오류)
        });
        verify(productRepository, times(1)).findByIdWithOptionsAndValues(1L);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("상품 재고 업데이트 실패 테스트 - 상품 없음")
    void updateProductStock_Fail_NotFound() {
        // given
        when(productRepository.findByIdWithOptionsAndValues(99L)).thenReturn(Optional.empty());

        // when & then
        assertThrows(CustomExceptions.ResourceNotFoundException.class, () -> {
            productService.updateProductStock(99L, 5);
        });
        verify(productRepository, times(1)).findByIdWithOptionsAndValues(99L);
        verify(productRepository, never()).save(any(Product.class));
    }
}