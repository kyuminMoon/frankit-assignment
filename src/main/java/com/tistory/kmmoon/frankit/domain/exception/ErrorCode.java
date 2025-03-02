package com.tistory.kmmoon.frankit.domain.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    LACK_OF_STOCK(HttpStatus.INTERNAL_SERVER_ERROR, 1, "남아있는 재고가 부족합니다."),
    INPUT_VALUE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 2, "입력 값을 확인해주세요.")
    ;

    private final HttpStatus httpStatus;
    private final int outCode;
    private final String message;
}
