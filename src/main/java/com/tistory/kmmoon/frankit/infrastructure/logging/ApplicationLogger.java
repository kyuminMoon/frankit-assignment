package com.tistory.kmmoon.frankit.infrastructure.logging;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.function.Supplier;

/**
 * 애플리케이션 로깅을 위한 유틸리티 클래스
 * - MDC(Mapped Diagnostic Context)를 사용한 구조화된 로깅 지원
 * - 보안 컨텍스트와 통합
 */
@Component
@RequiredArgsConstructor
public class ApplicationLogger {

    private static final Logger log = LoggerFactory.getLogger(ApplicationLogger.class);
    private static final String ACTIVE_USER_KEY = "userId";
    private static final String REQUEST_ID_KEY = "requestId";
    
    /**
     * INFO 레벨 로그 기록
     */
    public void logInfo(String message, Object... args) {
        setupMdc();
        log.info(message, args);
    }
    
    /**
     * WARN 레벨 로그 기록
     */
    public void logWarn(String message, Object... args) {
        setupMdc();
        log.warn(message, args);
    }
    
    /**
     * ERROR 레벨 로그 기록
     */
    public void logError(String message, Object... args) {
        setupMdc();
        
        // 마지막 인자가 Throwable인 경우 처리
        if (args.length > 0 && args[args.length - 1] instanceof Throwable) {
            Object[] messageArgs = new Object[args.length - 1];
            System.arraycopy(args, 0, messageArgs, 0, args.length - 1);
            
            Throwable throwable = (Throwable) args[args.length - 1];
            log.error(message, messageArgs, throwable);
        } else {
            log.error(message, args);
        }
    }
    
    /**
     * DEBUG 레벨 로그 기록
     */
    public void logDebug(String message, Object... args) {
        if (log.isDebugEnabled()) {
            setupMdc();
            log.debug(message, args);
        }
    }
    
    /**
     * 비즈니스 이벤트 로그 기록 (중요 이벤트)
     */
    public void logBusinessEvent(String event, String details, Object... args) {
        setupMdc();
        MDC.put("eventType", event);
        log.info("Business Event: {} - {}", event, String.format(details, args));
        MDC.remove("eventType");
    }
    
    /**
     * 성능 모니터링 로그 기록
     */
    public void logPerformance(String operation, long durationMs) {
        setupMdc();
        MDC.put("operation", operation);
        MDC.put("duration", String.valueOf(durationMs));
        
        if (durationMs > 1000) {  // 1초 이상 소요
            log.warn("Performance Alert: {} took {}ms", operation, durationMs);
        } else {
            log.info("Performance: {} took {}ms", operation, durationMs);
        }
        
        MDC.remove("operation");
        MDC.remove("duration");
    }
    
    /**
     * 로그 컨텍스트 추가와 함께 코드 블록 실행
     */
    public <T> T withContext(Map<String, String> context, Supplier<T> supplier) {
        Map<String, String> oldContext = MDC.getCopyOfContextMap();
        try {
            setupMdc();
            if (context != null) {
                context.forEach(MDC::put);
            }
            return supplier.get();
        } finally {
            if (oldContext != null) {
                MDC.setContextMap(oldContext);
            } else {
                MDC.clear();
            }
        }
    }
    
    /**
     * 성능 측정 및 로깅과 함께 코드 블록 실행
     */
    public <T> T withPerformanceLogging(String operation, Supplier<T> supplier) {
        long startTime = System.currentTimeMillis();
        try {
            return supplier.get();
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            logPerformance(operation, duration);
        }
    }
    
    /**
     * MDC 설정
     */
    private void setupMdc() {
        // 현재 인증된 사용자 정보 설정
        if (MDC.get(ACTIVE_USER_KEY) == null) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() && 
                    !"anonymousUser".equals(authentication.getPrincipal())) {
                MDC.put(ACTIVE_USER_KEY, authentication.getName());
            } else {
                MDC.put(ACTIVE_USER_KEY, "anonymous");
            }
        }
        
        // 요청 ID가 없는 경우 (비 HTTP 컨텍스트) 생성
        if (MDC.get(REQUEST_ID_KEY) == null) {
            MDC.put(REQUEST_ID_KEY, generateRequestId());
        }
    }
    
    /**
     * 요청 ID 생성
     */
    private String generateRequestId() {
        return java.util.UUID.randomUUID().toString().replace("-", "");
    }
}