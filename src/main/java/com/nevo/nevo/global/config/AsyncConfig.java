package com.nevo.nevo.global.config;

import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableAsync
@EnableScheduling
public class AsyncConfig implements AsyncConfigurer {

    // @Async 메서드에서 예외 발생 시 기본 동작은 예외를 무시
    // SMS 발송 실패가 무음으로 사라지지 않도록 에러 로그 기록
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (ex, method, params) ->
                LoggerFactory.getLogger(method.getDeclaringClass())
                        .error("[Async 오류] method={}, message={}", method.getName(), ex.getMessage());
    }
}
