package com.tistory.kmmoon.frankit.infrastructure.util;

import com.tistory.kmmoon.frankit.domain.exception.CustomExceptions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Redisson 분산락 처리
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DistributedLockTemplate {

    private final RedissonClient redissonClient;

    /**
     * 분산락을 획득하고 작업을 수행한 후 결과를 반환합니다.
     *
     * @param lockKey 락 키
     * @param waitTime 락 획득 대기 시간 (밀리초)
     * @param leaseTime 락 유지 시간 (밀리초)
     * @param supplier 락을 획득한 후 실행할 작업
     * @return 작업 실행 결과
     */
    public <T> T executeWithLock(String lockKey, long waitTime, long leaseTime, Supplier<T> supplier) {
        RLock lock = redissonClient.getLock(lockKey);
        boolean isLocked = false;

        try {
            log.debug("Trying to acquire lock: {}", lockKey);
            isLocked = lock.tryLock(waitTime, leaseTime, TimeUnit.MILLISECONDS);

            if (!isLocked) {
                log.warn("Failed to acquire lock: {}", lockKey);
                throw new CustomExceptions.LockAcquisitionException("분산 락 획득에 실패했습니다: " + lockKey);
            }

            log.debug("Lock acquired: {}", lockKey);
            return supplier.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Lock acquisition interrupted: {}", lockKey, e);
            throw new CustomExceptions.LockAcquisitionException("분산 락 획득 중 인터럽트 발생: " + lockKey, e);
        } finally {
            if (isLocked && lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.debug("Lock released: {}", lockKey);
            }
        }
    }

    /**
     * 분산락을 획득하고 작업을 수행합니다 (반환값 없음).
     *
     * @param lockKey 락 키
     * @param waitTime 락 획득 대기 시간 (밀리초)
     * @param leaseTime 락 유지 시간 (밀리초)
     * @param runnable 락을 획득한 후 실행할 작업
     */
    public void executeWithLock(String lockKey, long waitTime, long leaseTime, Runnable runnable) {
        executeWithLock(lockKey, waitTime, leaseTime, () -> {
            runnable.run();
            return null;
        });
    }
}