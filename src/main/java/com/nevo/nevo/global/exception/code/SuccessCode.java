package com.nevo.nevo.global.exception.code;

import org.springframework.http.HttpStatus;

// 모든 도메인 성공코드 enum이 구현해야 하는 인터페이스
public interface SuccessCode {
    HttpStatus getStatus();
    String getCode();
    String getMessage();
}
