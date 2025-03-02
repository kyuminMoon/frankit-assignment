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

import static com.tistory.kmmoon.frankit.application.service.parser.CustomSpELParser.getDynamicValue;

@Aspect
@Component
public class DistributedLockAspect {
    private final RedissonClient redissonClient;
    private final Logger log = LoggerFactory.getLogger(DistributedLockAspect.class);

    public DistributedLockAspect(final RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    @Around("@annotation(distributedLock)")
    public Object lock(final ProceedingJoinPoint joinPoint, final DistributedLock distributedLock) throws Throwable {
        final MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        final String LOCK_PREFIX = "lock:";
        final String lockName = LOCK_PREFIX + getDynamicValue(signature.getParameterNames(), joinPoint.getArgs(), distributedLock.key());
        final RLock lock = redissonClient.getLock(lockName);
        try {
            boolean available = lock.tryLock(distributedLock.waitTime(), distributedLock.leaseTime(), distributedLock.timeUnit());
            if (!available) {
                log.error("락을 획득하지 못했습니다. key: {}, currentThreadName : {}", lockName, Thread.currentThread().getName());
                return false;
            }
            log.error("락을 획득하였습니다. key: {}, currentThreadName : {}", lockName, Thread.currentThread().getName());
            return joinPoint.proceed();
        } catch (InterruptedException e) {
            throw new InterruptedException();
        } finally {
            lock.unlock();
        }
    }
}
