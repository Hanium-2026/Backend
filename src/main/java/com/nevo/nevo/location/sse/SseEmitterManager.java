package com.nevo.nevo.location.sse;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SseEmitterManager {

    private static final long SSE_TIMEOUT = 60 * 60 * 1000L; // 1시간

    // wardId → 연결된 보호자 emitter 목록 (1명의 노약자를 여러 보호자가 구독 가능)
    private final Map<Long, Set<SseEmitter>> emitters = new ConcurrentHashMap<>();

    public SseEmitter subscribe(Long wardId) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);
        emitters.computeIfAbsent(wardId, k -> ConcurrentHashMap.newKeySet()).add(emitter);

        Runnable cleanup = () -> {
            Set<SseEmitter> set = emitters.get(wardId);
            if (set != null) {
                set.remove(emitter);
                if (set.isEmpty()) emitters.remove(wardId);
            }
        };
        emitter.onCompletion(cleanup);
        emitter.onTimeout(cleanup);
        emitter.onError(e -> cleanup.run());

        return emitter;
    }

    public void send(Long wardId, Object data) {
        Set<SseEmitter> set = emitters.get(wardId);
        if (set == null || set.isEmpty()) return;

        Set<SseEmitter> dead = ConcurrentHashMap.newKeySet();
        for (SseEmitter emitter : set) {
            try {
                emitter.send(SseEmitter.event().name("location").data(data));
            } catch (IOException e) {
                dead.add(emitter);
            }
        }
        set.removeAll(dead);
    }
}