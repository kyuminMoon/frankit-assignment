package com.tistory.kmmoon.frankit.application.service;

import com.tistory.kmmoon.frankit.domain.entity.Product;
import com.tistory.kmmoon.frankit.domain.exception.CustomExceptions;
import com.tistory.kmmoon.frankit.domain.repository.ProductRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 실제 스프링 컨텍스트를 로드하는 통합 테스트 예시
 * 참고: 이 테스트는 실제 Redis 서버가 필요하므로 테스트 환경 설정이 필요합니다.
 */
@ActiveProfiles("test")
@SpringBootTest
public class ProductServiceIntegrationTest {

    @Autowired
    private ProductService productService;
    
    @Autowired
    private ProductRepository productRepository;
    
    private Product testProduct;
    private final int INITIAL_STOCK = 100;
    
    @BeforeEach
    void setUp() {
        // 테스트용 상품 생성 및 저장
        testProduct = Product.builder()
                .name("통합 테스트 상품")
                .description("재고 동시성 테스트용 상품")
                .price(new BigDecimal("10000"))
                .shippingFee(new BigDecimal("2500"))
                .stock((long) INITIAL_STOCK)
                .options(new HashSet<>())
                .build();
        testProduct.setCreatedBy(1L);
        testProduct.setCreatedAt(LocalDateTime.now());

        testProduct = productRepository.save(testProduct);
    }
    
    @AfterEach
    void tearDown() {
        // 테스트 종료 후 데이터 정리
        productRepository.deleteAll();
    }
    
    /**
     * 실제 분산 락이 적용된 환경에서 동시성 테스트
     * 참고: 이 테스트는 실행 환경에 따라 실패할 수 있으므로, 필요시 @Disabled 사용
     */
    @Test
    @DisplayName("실제 분산 락을 사용한 동시 재고 업데이트 테스트")
    void concurrentStockUpdateWithRealLock() throws InterruptedException {
        // given
        final int NUM_THREADS = 10;
        final int STOCK_DECREASE_PER_THREAD = 5;
        
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(NUM_THREADS);
        
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        List<Exception> exceptions = new ArrayList<>();
        
        // when - 여러 스레드가 동시에 재고 감소
        ExecutorService executorService = Executors.newFixedThreadPool(NUM_THREADS);
        
        for (int i = 0; i < NUM_THREADS; i++) {
            executorService.submit(() -> {
                try {
                    startLatch.await(); // 모든 스레드가 동시에 시작하도록 대기
                    productService.updateProductStock(testProduct.getId(), -STOCK_DECREASE_PER_THREAD);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                    synchronized (exceptions) {
                        exceptions.add(e);
                    }
                } finally {
                    finishLatch.countDown();
                }
            });
        }
        
        startLatch.countDown(); // 모든 스레드 시작
        finishLatch.await(5, TimeUnit.SECONDS); // 모든 스레드가 완료될 때까지 대기
        
        executorService.shutdown();
        executorService.awaitTermination(5, TimeUnit.SECONDS);
        
        // then
        // DB에서 최신 상태 조회
        Product updatedProduct = productRepository.findById(testProduct.getId()).orElseThrow();
        
        // 모든 요청이 성공했는지 확인
        assertEquals(NUM_THREADS, successCount.get(), "모든 요청이 성공해야 함");
        assertEquals(0, failCount.get(), "실패한 요청이 없어야 함");
        
        // 예상 재고량 계산
        int expectedFinalStock = INITIAL_STOCK - (NUM_THREADS * STOCK_DECREASE_PER_THREAD);
        assertEquals(expectedFinalStock, updatedProduct.getStock(), 
                "최종 재고량이 예상 값과 일치해야 함 (분산 락이 제대로 동작)");
    }
    
    @Test
    @DisplayName("재고 부족 시 예외 처리 테스트")
    void updateProductStock_InsufficientStock() {
        // given
        testProduct.setStock(10L);
        productRepository.save(testProduct);
        
        // when & then
        // 재고보다 많은 수량 감소 시도 - 예외 발생해야 함
        assertThrows(CustomExceptions.CustomException.class, () -> {
            productService.updateProductStock(testProduct.getId(), -20);
        });
        
        // DB에서 최신 상태 조회
        Product updatedProduct = productRepository.findById(testProduct.getId()).orElseThrow();
        assertEquals(10, updatedProduct.getStock(), "재고가 변경되지 않아야 함");
    }
    
    @Test
    @DisplayName("재고 증가 테스트")
    void updateProductStock_Increase() {
        // given
        testProduct.setStock(10L);
        productRepository.save(testProduct);
        
        // when
        productService.updateProductStock(testProduct.getId(), 5);
        
        // then
        Product updatedProduct = productRepository.findById(testProduct.getId()).orElseThrow();
        assertEquals(15, updatedProduct.getStock(), "재고가 5 증가해야 함");
    }
}