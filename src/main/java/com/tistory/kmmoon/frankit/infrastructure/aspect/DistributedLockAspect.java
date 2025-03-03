package com.tistory.kmmoon.frankit.infrastructure.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.tistory.kmmoon.frankit.domain.exception.CustomExceptions;

import static com.tistory.kmmoon.frankit.application.service.parser.CustomSpELParser.getDynamicValue;

@Aspect
@Component
public class DistributedLockAspect {
    private static final String LOCK_PREFIX = "lock:"; // 상수 분리
    private final RedissonClient redissonClient;
    private final Logger log = LoggerFactory.getLogger(DistributedLockAspect.class);

    public DistributedLockAspect(final RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    @Around("@annotation(distributedLock)")
    public Object lock(final ProceedingJoinPoint joinPoint, final DistributedLock distributedLock) throws Throwable {
        final MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        final String lockName = LOCK_PREFIX + getDynamicValue(signature.getParameterNames(), joinPoint.getArgs(), distributedLock.key());
        final RLock lock = redissonClient.getLock(lockName);
        boolean isLockAcquired = false;

        try {
            // 락 획득 시도
            isLockAcquired = lock.tryLock(distributedLock.waitTime(), distributedLock.leaseTime(), distributedLock.timeUnit());
            if (!isLockAcquired) {
                log.warn("락을 획득하지 못했습니다. key: {}, currentThreadName : {}", lockName, Thread.currentThread().getName());
                throw new CustomExceptions.LockAcquisitionException("분산 락 획득 실패: " + lockName);
            }

            log.debug("락을 획득하였습니다. key: {}, currentThreadName : {}", lockName, Thread.currentThread().getName());
            return joinPoint.proceed();
        } finally {
            // 락 해제 로직 개선: 락을 획득한 경우에만 unlock() 호출
            if (isLockAcquired && lock.isHeldByCurrentThread()) {
                try {
                    lock.unlock();
                    log.debug("락 해제 완료: key: {}, thread: {}", lockName, Thread.currentThread().getName());
                } catch (Exception e) {
                    log.error("락 해제 중 오류 발생: key: {}", lockName, e);
                }
            }
        }
    }
}