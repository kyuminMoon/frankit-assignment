package com.tistory.kmmoon.frankit.infrastructure.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * 각 요청에 고유한 추적 ID를 부여하는 필터
 * 이를 통해 로그에서 특정 요청의 전체 흐름을 추적할 수 있음
 */
@Component
public class MDCLoggingFilter extends OncePerRequestFilter {

    private static final String TRACE_ID = "traceId";
    private static final String REQUEST_ID_HEADER = "X-Request-ID";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            setupMDC(request, response);
            filterChain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }

    private void setupMDC(HttpServletRequest request, HttpServletResponse response) {
        // 요청 헤더에 추적 ID가 있으면 사용, 없으면 새로 생성
        String traceId = request.getHeader(REQUEST_ID_HEADER);
        if (traceId == null || traceId.isEmpty()) {
            traceId = generateTraceId();
        }
        
        MDC.put(TRACE_ID, traceId);
        response.setHeader(REQUEST_ID_HEADER, traceId);
    }

    private String generateTraceId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}