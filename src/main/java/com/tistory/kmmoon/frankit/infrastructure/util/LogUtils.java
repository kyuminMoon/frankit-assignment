package com.tistory.kmmoon.frankit.infrastructure.util;

import lombok.experimental.UtilityClass;
import org.slf4j.MDC;

import java.util.Map;
import java.util.function.Supplier;

/**
 * 로깅 관련 유틸리티 클래스
 */
@UtilityClass
public class LogUtils {

    /**
     * 로그에 추가 정보를 포함하여 작업을 실행합니다.
     *
     * @param context 로그에 추가할 MDC 컨텍스트
     * @param supplier 실행할 작업
     * @param <T> 반환 타입
     * @return 작업 결과
     */
    public <T> T withContext(Map<String, String> context, Supplier<T> supplier) {
        Map<String, String> oldContext = MDC.getCopyOfContextMap();
        
        try {
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
     * 로그에 추가 정보를 포함하여 작업을 실행합니다. (반환값 없음)
     *
     * @param context 로그에 추가할 MDC 컨텍스트
     * @param runnable 실행할 작업
     */
    public void withContext(Map<String, String> context, Runnable runnable) {
        withContext(context, () -> {
            runnable.run();
            return null;
        });
    }

    /**
     * 현재 스레드의 추적 ID를 반환합니다.
     *
     * @return 추적 ID
     */
    public String getTraceId() {
        return MDC.get("traceId");
    }

    /**
     * 주요 민감 정보를 마스킹하여 반환합니다.
     *
     * @param input 원본 문자열
     * @return 마스킹된 문자열
     */
    public String maskSensitiveData(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        
        // 이메일 마스킹
        if (input.contains("@")) {
            int atIndex = input.indexOf('@');
            if (atIndex > 1) {
                String username = input.substring(0, atIndex);
                String domain = input.substring(atIndex);
                String maskedUsername = username.substring(0, Math.min(3, username.length())) + 
                        "*".repeat(Math.max(0, username.length() - 3));
                return maskedUsername + domain;
            }
        }
        
        // 전화번호 마스킹
        if (input.matches("\\d{10,13}")) {
            return input.substring(0, 3) + "*".repeat(input.length() - 7) + input.substring(input.length() - 4);
        }
        
        // 카드번호 마스킹
        if (input.matches("\\d{13,19}")) {
            return input.substring(0, 6) + "*".repeat(input.length() - 10) + input.substring(input.length() - 4);
        }
        
        return input;
    }
}