package com.tistory.kmmoon.frankit.domain.exception;

public class CustomExceptions {
    public static class CustomException extends RuntimeException {
        private final ErrorCode errorCode;

        public CustomException(ErrorCode errorCode) {
            super(errorCode.getMessage());
            this.errorCode = errorCode;
        }

        public ErrorCode getErrorCode() {
            return errorCode;
        }
    }

    // 리소스를 찾을 수 없는 경우의 예외
    public static class ResourceNotFoundException extends RuntimeException {
        public ResourceNotFoundException(String message) {
            super(message);
        }
    }

    // 인증 관련 예외
    public static class AuthenticationException extends RuntimeException {
        public AuthenticationException(String message) {
            super(message);
        }
    }

    // 권한 관련 예외
    public static class AuthorizationException extends RuntimeException {
        public AuthorizationException(String message) {
            super(message);
        }
    }

    // 비즈니스 로직 관련 예외
    public static class BusinessException extends RuntimeException {
        public BusinessException(String message) {
            super(message);
        }
    }
}