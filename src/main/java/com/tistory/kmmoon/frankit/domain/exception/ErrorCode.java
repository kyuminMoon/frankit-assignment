package com.tistory.kmmoon.frankit.domain.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    BusinessException(HttpStatus.NO_CONTENT, 1, "비즈니스 로직 관련 예외");

    private final HttpStatus httpStatus;
    private final int outCode;
    private final String message;
}
